//$Id: EntityIdentityInsertAction.java 14127 2007-10-20 00:06:51Z scottmarlownovell $
package org.hibernate.action;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.AssertionFailure;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.EntityKey;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.EventSource;
import org.hibernate.persister.entity.EntityPersister;

public final class EntityIdentityInsertAction extends EntityAction {
	private final Object[] state;
	private final boolean isDelayed;
	private final EntityKey delayedEntityKey;
	//private CacheEntry cacheEntry;
	private Serializable generatedId;

	public EntityIdentityInsertAction(
			Object[] state,
	        Object instance,
	        EntityPersister persister,
	        SessionImplementor session,
	        boolean isDelayed) throws HibernateException {
		super( session, null, instance, persister );
		this.state = state;
		this.isDelayed = isDelayed;
		delayedEntityKey = isDelayed ? generateDelayedEntityKey() : null;
	}

	public void execute() throws HibernateException {
		
		final EntityPersister persister = getPersister();
		final SessionImplementor session = getSession();
		final Object instance = getInstance();
        final boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();
        long startTime = 0;
        if ( stats ) startTime = System.currentTimeMillis();

		boolean veto = preInsert();

		// Don't need to lock the cache here, since if someone
		// else inserted the same pk first, the insert would fail

		if ( !veto ) {
			generatedId = persister.insert( state, instance, session );
			if ( persister.hasInsertGeneratedProperties() ) {
				persister.processInsertGeneratedProperties( generatedId, instance, state, session );
			}
			//need to do that here rather than in the save event listener to let
			//the post insert events to have a id-filled entity when IDENTITY is used (EJB3)
			persister.setIdentifier( instance, generatedId, session.getEntityMode() );
		}


		//TODO: this bit actually has to be called after all cascades!
		//      but since identity insert is called *synchronously*,
		//      instead of asynchronously as other actions, it isn't
		/*if ( persister.hasCache() && !persister.isCacheInvalidationRequired() ) {
			cacheEntry = new CacheEntry(object, persister, session);
			persister.getCache().insert(generatedId, cacheEntry);
		}*/
		
		postInsert();

		if ( stats && !veto ) {
			session.getFactory().getStatisticsImplementor()
					.insertEntity( getPersister().getEntityName(), System.currentTimeMillis() - startTime);
		}

	}

	private void postInsert() {
		if ( isDelayed ) {
			getSession().getPersistenceContext().replaceDelayedEntityIdentityInsertKeys( delayedEntityKey, generatedId );
		}
		PostInsertEventListener[] postListeners = getSession().getListeners()
				.getPostInsertEventListeners();
		if (postListeners.length>0) {
			PostInsertEvent postEvent = new PostInsertEvent(
					getInstance(),
					generatedId,
					state,
					getPersister(),
					(EventSource) getSession()
			);
			for ( int i = 0; i < postListeners.length; i++ ) {
				postListeners[i].onPostInsert(postEvent);
			}
		}
	}

	private void postCommitInsert() {
		PostInsertEventListener[] postListeners = getSession().getListeners()
				.getPostCommitInsertEventListeners();
		if (postListeners.length>0) {
			PostInsertEvent postEvent = new PostInsertEvent(
					getInstance(),
					generatedId,
					state,
					getPersister(),
					(EventSource) getSession() 
			);
			for ( int i = 0; i < postListeners.length; i++ ) {
				postListeners[i].onPostInsert(postEvent);
			}
		}
	}

	private boolean preInsert() {
		PreInsertEventListener[] preListeners = getSession().getListeners()
				.getPreInsertEventListeners();
		boolean veto = false;
		if (preListeners.length>0) {
			PreInsertEvent preEvent = new PreInsertEvent( getInstance(), null, state, getPersister(), getSession() );
			for ( int i = 0; i < preListeners.length; i++ ) {
				veto = preListeners[i].onPreInsert(preEvent) || veto;
			}
		}
		return veto;
	}

	//Make 100% certain that this is called before any subsequent ScheduledUpdate.afterTransactionCompletion()!!
	public void afterTransactionCompletion(boolean success) throws HibernateException {
		//TODO: reenable if we also fix the above todo
		/*EntityPersister persister = getEntityPersister();
		if ( success && persister.hasCache() && !persister.isCacheInvalidationRequired() ) {
			persister.getCache().afterInsert( getGeneratedId(), cacheEntry );
		}*/
		postCommitInsert();
	}

	public boolean hasAfterTransactionCompletion() {
		//TODO: simply remove this override
		//      if we fix the above todos
		return hasPostCommitEventListeners();
	}

	protected boolean hasPostCommitEventListeners() {
		return getSession().getListeners().getPostCommitInsertEventListeners().length>0;
	}
	
	public final Serializable getGeneratedId() {
		return generatedId;
	}

	public EntityKey getDelayedEntityKey() {
		return delayedEntityKey;
	}

	private synchronized EntityKey generateDelayedEntityKey() {
		if ( !isDelayed ) {
			throw new AssertionFailure( "cannot request delayed entity-key for non-delayed post-insert-id generation" );
		}
		return new EntityKey( new DelayedPostInsertIdentifier(), getPersister(), getSession().getEntityMode() );
	}
}
