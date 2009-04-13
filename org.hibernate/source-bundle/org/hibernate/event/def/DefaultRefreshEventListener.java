//$Id: DefaultRefreshEventListener.java 7785 2005-08-08 23:24:44Z oneovthafew $
package org.hibernate.event.def;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.PersistentObjectException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cache.CacheKey;
import org.hibernate.engine.Cascade;
import org.hibernate.engine.CascadingAction;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.event.RefreshEvent;
import org.hibernate.event.RefreshEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.hibernate.util.IdentityMap;

/**
 * Defines the default refresh event listener used by hibernate for refreshing entities
 * in response to generated refresh events.
 *
 * @author Steve Ebersole
 */
public class DefaultRefreshEventListener implements RefreshEventListener {

	private static final Log log = LogFactory.getLog(DefaultRefreshEventListener.class);
	
	public void onRefresh(RefreshEvent event) throws HibernateException {
		onRefresh( event, IdentityMap.instantiate(10) );
	}

	/** 
	 * Handle the given refresh event.
	 *
	 * @param event The refresh event to be handled.
	 * @throws HibernateException
	 */
	public void onRefresh(RefreshEvent event, Map refreshedAlready) throws HibernateException {

		final EventSource source = event.getSession();
		
		if ( source.getPersistenceContext().reassociateIfUninitializedProxy( event.getObject() ) ) return;

		final Object object = source.getPersistenceContext().unproxyAndReassociate( event.getObject() );

		if ( refreshedAlready.containsKey(object) ) {
			log.trace("already refreshed");
			return;
		}

		final EntityEntry e = source.getPersistenceContext().getEntry( object );
		final EntityPersister persister;
		final Serializable id;
		
		if ( e == null ) {
			persister = source.getEntityPersister(null, object); //refresh() does not pass an entityName
			id = persister.getIdentifier( object, event.getSession().getEntityMode() );
			if ( log.isTraceEnabled() ) {
				log.trace(
						"refreshing transient " +
						MessageHelper.infoString( persister, id, source.getFactory() )
					);
			}
			EntityKey key = new EntityKey( id, persister, source.getEntityMode() );
			if ( source.getPersistenceContext().getEntry(key) != null ) {
				throw new PersistentObjectException(
						"attempted to refresh transient instance when persistent instance was already associated with the Session: " +
						MessageHelper.infoString(persister, id, source.getFactory() )
					);
			}
		}
		else {
			if ( log.isTraceEnabled() ) {
				log.trace(
						"refreshing " +
						MessageHelper.infoString( e.getPersister(), e.getId(), source.getFactory()  )
					);
			}
			if ( !e.isExistsInDatabase() ) {
				throw new HibernateException( "this instance does not yet exist as a row in the database" );
			}

			persister = e.getPersister();
			id = e.getId();
		}

		// cascade the refresh prior to refreshing this entity
		refreshedAlready.put(object, object);
		new Cascade(CascadingAction.REFRESH, Cascade.BEFORE_REFRESH, source)
				.cascade( persister, object, refreshedAlready );

		if ( e != null ) {
			EntityKey key = new EntityKey( id, persister, source.getEntityMode() );
			source.getPersistenceContext().removeEntity(key);
			if ( persister.hasCollections() ) new EvictVisitor( source ).process(object, persister);
		}

		if ( persister.hasCache() ) {
			final CacheKey ck = new CacheKey(
					id,
					persister.getIdentifierType(),
					persister.getRootEntityName(),
					source.getEntityMode(), 
					source.getFactory()
				);
			persister.getCache().remove(ck);
		}
		
		evictCachedCollections( persister, id, source.getFactory() );
		
		String previousFetchProfile = source.getFetchProfile();
		source.setFetchProfile("refresh");
		Object result = persister.load( id, object, event.getLockMode(), source );
		source.setFetchProfile(previousFetchProfile);
		
		UnresolvableObjectException.throwIfNull( result, id, persister.getEntityName() );

	}

	/**
	 * Evict collections from the factory-level cache
	 */
	private void evictCachedCollections(EntityPersister persister, Serializable id, SessionFactoryImplementor factory)
	throws HibernateException {
		evictCachedCollections( persister.getPropertyTypes(), id, factory );
	}

	private void evictCachedCollections(Type[] types, Serializable id, SessionFactoryImplementor factory)
	throws HibernateException {
		for ( int i = 0; i < types.length; i++ ) {
			if ( types[i].isCollectionType() ) {
				factory.evictCollection( ( (CollectionType) types[i] ).getRole(), id );
			}
			else if ( types[i].isComponentType() ) {
				AbstractComponentType actype = (AbstractComponentType) types[i];
				evictCachedCollections( actype.getSubtypes(), id, factory );
			}
		}
	}

}
