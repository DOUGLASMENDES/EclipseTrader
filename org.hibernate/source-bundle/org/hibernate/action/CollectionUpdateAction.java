//$Id: CollectionUpdateAction.java 14313 2008-02-06 07:46:52Z gbadner $
package org.hibernate.action;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.event.PreCollectionUpdateEvent;
import org.hibernate.event.PreCollectionUpdateEventListener;
import org.hibernate.event.EventSource;
import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.cache.CacheException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;

import java.io.Serializable;

public final class CollectionUpdateAction extends CollectionAction {

	private final boolean emptySnapshot;

	public CollectionUpdateAction(
				final PersistentCollection collection,
				final CollectionPersister persister,
				final Serializable id,
				final boolean emptySnapshot,
				final SessionImplementor session)
			throws CacheException {
		super( persister, collection, id, session );
		this.emptySnapshot = emptySnapshot;
	}

	public void execute() throws HibernateException {
		final Serializable id = getKey();
		final SessionImplementor session = getSession();
		final CollectionPersister persister = getPersister();
		final PersistentCollection collection = getCollection();
		boolean affectedByFilters = persister.isAffectedByEnabledFilters(session);
		final boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();
		long startTime = 0;
		if ( stats ) startTime = System.currentTimeMillis();

		preUpdate();

		if ( !collection.wasInitialized() ) {
			if ( !collection.hasQueuedOperations() ) throw new AssertionFailure( "no queued adds" );
			//do nothing - we only need to notify the cache...
		}
		else if ( !affectedByFilters && collection.empty() ) {
			if ( !emptySnapshot ) persister.remove( id, session );
		}
		else if ( collection.needsRecreate(persister) ) {
			if (affectedByFilters) {
				throw new HibernateException(
					"cannot recreate collection while filter is enabled: " + 
					MessageHelper.collectionInfoString( persister, id, persister.getFactory() )
				);
			}
			if ( !emptySnapshot ) persister.remove( id, session );
			persister.recreate( collection, id, session );
		}
		else {
			persister.deleteRows( collection, id, session );
			persister.updateRows( collection, id, session );
			persister.insertRows( collection, id, session );
		}

		getSession().getPersistenceContext()
			.getCollectionEntry(collection)
			.afterAction(collection);

		evict();

		postUpdate();

		if ( stats ) {
			getSession().getFactory().getStatisticsImplementor().
					updateCollection( getPersister().getRole(), System.currentTimeMillis() - startTime);
		}
	}

	private void preUpdate() {
		PreCollectionUpdateEventListener[] preListeners = getSession().getListeners()
				.getPreCollectionUpdateEventListeners();
		if (preListeners.length > 0) {
			PreCollectionUpdateEvent preEvent = new PreCollectionUpdateEvent(
					getPersister(), getCollection(), ( EventSource ) getSession() );
			for ( int i = 0; i < preListeners.length; i++ ) {
				preListeners[i].onPreUpdateCollection( preEvent );
			}
		}
	}

	private void postUpdate() {
		PostCollectionUpdateEventListener[] postListeners = getSession().getListeners()
				.getPostCollectionUpdateEventListeners();
		if (postListeners.length > 0) {
			PostCollectionUpdateEvent postEvent = new PostCollectionUpdateEvent(
					getPersister(), getCollection(), ( EventSource ) getSession() );
			for ( int i = 0; i < postListeners.length; i++ ) {
				postListeners[i].onPostUpdateCollection( postEvent );
			}
		}
	}
}







