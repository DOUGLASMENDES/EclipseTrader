//$Id: CollectionRecreateAction.java 14313 2008-02-06 07:46:52Z gbadner $
package org.hibernate.action;

import org.hibernate.HibernateException;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.event.EventSource;
import org.hibernate.event.PreCollectionRecreateEvent;
import org.hibernate.event.PreCollectionRecreateEventListener;
import org.hibernate.cache.CacheException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

import java.io.Serializable;

public final class CollectionRecreateAction extends CollectionAction {

	public CollectionRecreateAction(
				final PersistentCollection collection, 
				final CollectionPersister persister, 
				final Serializable id, 
				final SessionImplementor session)
			throws CacheException {
		super( persister, collection, id, session );
	}

	public void execute() throws HibernateException {
		// this method is called when a new non-null collection is persisted
		// or when an existing (non-null) collection is moved to a new owner
		final boolean stats = getSession().getFactory().getStatistics().isStatisticsEnabled();
		long startTime = 0;
		if ( stats ) startTime = System.currentTimeMillis();

		final PersistentCollection collection = getCollection();
		
		preRecreate();

		getPersister().recreate( collection, getKey(), getSession() );
		
		getSession().getPersistenceContext()
				.getCollectionEntry(collection)
				.afterAction(collection);
		
		evict();

		postRecreate();

		if ( stats ) {
			getSession().getFactory().getStatisticsImplementor()
					.recreateCollection( getPersister().getRole(), System.currentTimeMillis() - startTime);
		}
	}

	private void preRecreate() {
		PreCollectionRecreateEventListener[] preListeners = getSession().getListeners()
				.getPreCollectionRecreateEventListeners();
		if (preListeners.length > 0) {
			PreCollectionRecreateEvent preEvent = new PreCollectionRecreateEvent(
					getPersister(), getCollection(), ( EventSource ) getSession() );
			for ( int i = 0; i < preListeners.length; i++ ) {
				preListeners[i].onPreRecreateCollection( preEvent );
			}
		}
	}

	private void postRecreate() {
		PostCollectionRecreateEventListener[] postListeners = getSession().getListeners()
				.getPostCollectionRecreateEventListeners();
		if (postListeners.length > 0) {
			PostCollectionRecreateEvent postEvent = new PostCollectionRecreateEvent(
					getPersister(), getCollection(), ( EventSource ) getSession() );
			for ( int i = 0; i < postListeners.length; i++ ) {
				postListeners[i].onPostRecreateCollection( postEvent );
			}
		}
	}
}







