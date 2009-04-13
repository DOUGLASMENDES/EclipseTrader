//$Id: CollectionAction.java 9673 2006-03-22 14:57:59Z steve.ebersole@jboss.com $
package org.hibernate.action;

import org.hibernate.cache.CacheConcurrencyStrategy.SoftLock;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheKey;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.util.StringHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Any action relating to insert/update/delete of a collection
 * @author Gavin King
 */
public abstract class CollectionAction implements Executable, Serializable, Comparable {

	private transient CollectionPersister persister;
	private final Serializable key;
	private Serializable finalKey;
	private final SessionImplementor session;
	private SoftLock lock;
	private final String collectionRole;
	private final PersistentCollection collection;

	public CollectionAction(
			final CollectionPersister persister, 
			final PersistentCollection collection, 
			final Serializable key, 
			final SessionImplementor session)
	throws CacheException {
		this.persister = persister;
		this.session = session;
		this.key = key;
		this.collectionRole = persister.getRole();
		this.collection = collection;
	}
	
	protected PersistentCollection getCollection() {
		return collection;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		persister = session.getFactory().getCollectionPersister( collectionRole );
	}

	public void afterTransactionCompletion(boolean success) throws CacheException {
		if ( persister.hasCache() ) {
			final CacheKey ck = new CacheKey( 
					key, 
					persister.getKeyType(), 
					persister.getRole(), 
					session.getEntityMode(), 
					session.getFactory() 
				);
			persister.getCache().release(ck, lock);
		}
	}

	public boolean hasAfterTransactionCompletion() {
		return persister.hasCache();
	}

	public Serializable[] getPropertySpaces() {
		return persister.getCollectionSpaces();
	}

	protected final CollectionPersister getPersister() {
		return persister;
	}

	protected final Serializable getKey() {
		finalKey = key;
		if ( key instanceof DelayedPostInsertIdentifier ) {
			// need to look it up from the persistence-context
			finalKey = session.getPersistenceContext().getEntry( collection.getOwner() ).getId();
			if ( finalKey == key ) {
				// we may be screwed here since the collection action is about to execute
				// and we do not know the final owner key value
			}
		}
		return finalKey;
	}

	protected final SessionImplementor getSession() {
		return session;
	}

	public final void beforeExecutions() throws CacheException {
		// we need to obtain the lock before any actions are
		// executed, since this may be an inverse="true"
		// bidirectional association and it is one of the
		// earlier entity actions which actually updates
		// the database (this action is resposible for
		// second-level cache invalidation only)
		if ( persister.hasCache() ) {
			final CacheKey ck = new CacheKey( 
					key, 
					persister.getKeyType(), 
					persister.getRole(), 
					session.getEntityMode(), 
					session.getFactory() 
				);
			lock = persister.getCache().lock(ck, null);
		}
	}

	protected final void evict() throws CacheException {
		if ( persister.hasCache() ) {
			CacheKey ck = new CacheKey( 
					key, 
					persister.getKeyType(), 
					persister.getRole(), 
					session.getEntityMode(), 
					session.getFactory() 
				);
			persister.getCache().evict(ck);
		}
	}

	public String toString() {
		return StringHelper.unqualify( getClass().getName() ) + 
				MessageHelper.infoString( collectionRole, key );
	}

	public int compareTo(Object other) {
		CollectionAction action = ( CollectionAction ) other;
		//sort first by role name
		int roleComparison = collectionRole.compareTo( action.collectionRole );
		if ( roleComparison != 0 ) {
			return roleComparison;
		}
		else {
			//then by fk
			return persister.getKeyType()
					.compare( key, action.key, session.getEntityMode() );
		}
	}
}






