//$Id: EntityUpdateAction.java 14127 2007-10-20 00:06:51Z scottmarlownovell $
package org.hibernate.action;

import java.io.Serializable;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheKey;
import org.hibernate.cache.CacheConcurrencyStrategy.SoftLock;
import org.hibernate.cache.entry.CacheEntry;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.Status;
import org.hibernate.engine.Versioning;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.EventSource;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.TypeFactory;

public final class EntityUpdateAction extends EntityAction {

	private final Object[] state;
	private final Object[] previousState;
	private final Object previousVersion;
	private Object nextVersion;
	private final int[] dirtyFields;
	private final boolean hasDirtyCollection;
	private final Object rowId;
	private Object cacheEntry;
	private SoftLock lock;

	public EntityUpdateAction(
	        final Serializable id,
	        final Object[] state,
	        final int[] dirtyProperties,
	        final boolean hasDirtyCollection,
	        final Object[] previousState,
	        final Object previousVersion,
	        final Object nextVersion,
	        final Object instance,
	        final Object rowId,
	        final EntityPersister persister,
	        final SessionImplementor session) throws HibernateException {
		super( session, id, instance, persister );
		this.state = state;
		this.previousState = previousState;
		this.previousVersion = previousVersion;
		this.nextVersion = nextVersion;
		this.dirtyFields = dirtyProperties;
		this.hasDirtyCollection = hasDirtyCollection;
		this.rowId = rowId;
	}

	public void execute() throws HibernateException {
		Serializable id = getId();
		EntityPersister persister = getPersister();
		SessionImplementor session = getSession();
		Object instance = getInstance();

		boolean veto = preUpdate();

		final SessionFactoryImplementor factory = getSession().getFactory();
        final boolean stats = factory.getStatistics().isStatisticsEnabled();
        long startTime = 0;
        if ( stats ) startTime = System.currentTimeMillis();

        Object previousVersion = this.previousVersion;
		if ( persister.isVersionPropertyGenerated() ) {
			// we need to grab the version value from the entity, otherwise
			// we have issues with generated-version entities that may have
			// multiple actions queued during the same flush
			previousVersion = persister.getVersion( instance, session.getEntityMode() );
		}
		
		final CacheKey ck;
		if ( persister.hasCache() ) {
			ck = new CacheKey( 
					id, 
					persister.getIdentifierType(), 
					persister.getRootEntityName(), 
					session.getEntityMode(), 
					session.getFactory() 
				);
			lock = persister.getCache().lock(ck, previousVersion);
		}
		else {
			ck = null;
		}

		if ( !veto ) {
			persister.update( 
					id, 
					state, 
					dirtyFields, 
					hasDirtyCollection, 
					previousState, 
					previousVersion, 
					instance, 
					rowId, 
					session 
			);
		}

		EntityEntry entry = getSession().getPersistenceContext().getEntry( instance );
		if ( entry == null ) {
			throw new AssertionFailure( "possible nonthreadsafe access to session" );
		}

		if ( entry.getStatus()==Status.MANAGED || persister.isVersionPropertyGenerated() ) {
			// get the updated snapshot of the entity state by cloning current state;
			// it is safe to copy in place, since by this time no-one else (should have)
			// has a reference  to the array
			TypeFactory.deepCopy(
					state,
					persister.getPropertyTypes(),
					persister.getPropertyCheckability(),
					state,
					session
			);
			if ( persister.hasUpdateGeneratedProperties() ) {
				// this entity defines proeprty generation, so process those generated
				// values...
				persister.processUpdateGeneratedProperties( id, instance, state, session );
				if ( persister.isVersionPropertyGenerated() ) {
					nextVersion = Versioning.getVersion( state, persister );
				}
			}
			// have the entity entry perform post-update processing, passing it the
			// update state and the new version (if one).
			entry.postUpdate( instance, state, nextVersion );
		}

		if ( persister.hasCache() ) {
			if ( persister.isCacheInvalidationRequired() || entry.getStatus()!=Status.MANAGED ) {
				persister.getCache().evict(ck);
			}
			else {
				//TODO: inefficient if that cache is just going to ignore the updated state!
				CacheEntry ce = new CacheEntry(
						state, 
						persister, 
						persister.hasUninitializedLazyProperties( instance, session.getEntityMode() ), 
						nextVersion,
						getSession(),
						instance
				);
				cacheEntry = persister.getCacheEntryStructure().structure(ce);
//				boolean put = persister.getCache().update(ck, cacheEntry);
				boolean put = persister.getCache().update( ck, cacheEntry, nextVersion, previousVersion );
				
				if ( put && factory.getStatistics().isStatisticsEnabled() ) {
					factory.getStatisticsImplementor()
							.secondLevelCachePut( getPersister().getCache().getRegionName() );
				}
			}
		}

		postUpdate();

		if ( stats && !veto ) {
			factory.getStatisticsImplementor()
					.updateEntity( getPersister().getEntityName(), System.currentTimeMillis() - startTime);
		}
	}

	private void postUpdate() {
		PostUpdateEventListener[] postListeners = getSession().getListeners()
				.getPostUpdateEventListeners();
		if (postListeners.length>0) {
			PostUpdateEvent postEvent = new PostUpdateEvent( 
					getInstance(), 
					getId(), 
					state, 
					previousState, 
					getPersister(),
					(EventSource) getSession() 
				);
			for ( int i = 0; i < postListeners.length; i++ ) {
				postListeners[i].onPostUpdate(postEvent);
			}
		}
	}

	private void postCommitUpdate() {
		PostUpdateEventListener[] postListeners = getSession().getListeners()
				.getPostCommitUpdateEventListeners();
		if (postListeners.length>0) {
			PostUpdateEvent postEvent = new PostUpdateEvent( 
					getInstance(), 
					getId(), 
					state, 
					previousState, 
					getPersister(),
					(EventSource) getSession()
				);
			for ( int i = 0; i < postListeners.length; i++ ) {
				postListeners[i].onPostUpdate(postEvent);
			}
		}
	}

	private boolean preUpdate() {
		PreUpdateEventListener[] preListeners = getSession().getListeners()
				.getPreUpdateEventListeners();
		boolean veto = false;
		if (preListeners.length>0) {
			PreUpdateEvent preEvent = new PreUpdateEvent( 
					getInstance(), 
					getId(), 
					state, 
					previousState, 
					getPersister(),
					getSession()
				);
			for ( int i = 0; i < preListeners.length; i++ ) {
				veto = preListeners[i].onPreUpdate(preEvent) || veto;
			}
		}
		return veto;
	}

	public void afterTransactionCompletion(boolean success) throws CacheException {
		EntityPersister persister = getPersister();
		if ( persister.hasCache() ) {
			
			final CacheKey ck = new CacheKey( 
					getId(), 
					persister.getIdentifierType(), 
					persister.getRootEntityName(), 
					getSession().getEntityMode(), 
					getSession().getFactory() 
				);
			
			if ( success && cacheEntry!=null /*!persister.isCacheInvalidationRequired()*/ ) {
				boolean put = persister.getCache().afterUpdate(ck, cacheEntry, nextVersion, lock );
				
				if ( put && getSession().getFactory().getStatistics().isStatisticsEnabled() ) {
					getSession().getFactory().getStatisticsImplementor()
							.secondLevelCachePut( getPersister().getCache().getRegionName() );
				}
			}
			else {
				persister.getCache().release(ck, lock );
			}
		}
		postCommitUpdate();
	}

	protected boolean hasPostCommitEventListeners() {
		return getSession().getListeners().getPostCommitUpdateEventListeners().length>0;
	}
	
}







