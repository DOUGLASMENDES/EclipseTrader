//$Id: DefaultLoadEventListener.java 14127 2007-10-20 00:06:51Z scottmarlownovell $
package org.hibernate.event.def;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.PersistentObjectException;
import org.hibernate.TypeMismatchException;
import org.hibernate.EntityMode;
import org.hibernate.cache.CacheConcurrencyStrategy;
import org.hibernate.cache.CacheKey;
import org.hibernate.cache.entry.CacheEntry;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.Status;
import org.hibernate.engine.TwoPhaseLoad;
import org.hibernate.engine.Versioning;
import org.hibernate.event.EventSource;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;

/**
 * Defines the default load event listeners used by hibernate for loading entities
 * in response to generated load events.
 *
 * @author Steve Ebersole
 */
public class DefaultLoadEventListener extends AbstractLockUpgradeEventListener implements LoadEventListener {

	public static final Object REMOVED_ENTITY_MARKER = new Object();
	public static final Object INCONSISTENT_RTN_CLASS_MARKER = new Object();
	public static final LockMode DEFAULT_LOCK_MODE = LockMode.NONE;

	private static final Log log = LogFactory.getLog(DefaultLoadEventListener.class);


	/** 
	 * Handle the given load event.
	 *
	 * @param event The load event to be handled.
	 * @throws HibernateException
	 */
	public void onLoad(LoadEvent event, LoadEventListener.LoadType loadType) throws HibernateException {

		final SessionImplementor source = event.getSession();

		EntityPersister persister;
		if ( event.getInstanceToLoad() != null ) {
			persister = source.getEntityPersister( null, event.getInstanceToLoad() ); //the load() which takes an entity does not pass an entityName
			event.setEntityClassName( event.getInstanceToLoad().getClass().getName() );
		}
		else {
			persister = source.getFactory().getEntityPersister( event.getEntityClassName() );
		}

		if ( persister == null ) {
			throw new HibernateException( 
					"Unable to locate persister: " + 
					event.getEntityClassName() 
				);
		}

		if ( persister.getIdentifierType().isComponentType() && EntityMode.DOM4J == event.getSession().getEntityMode() ) {
			// skip this check for composite-ids relating to dom4j entity-mode;
			// alternatively, we could add a check to make sure the incoming id value is
			// an instance of Element...
		}
		else {
			Class idClass = persister.getIdentifierType().getReturnedClass();
			if ( idClass != null && ! idClass.isInstance( event.getEntityId() ) ) {
				throw new TypeMismatchException(
						"Provided id of the wrong type. Expected: " + idClass + ", got " + event.getEntityId().getClass()
				);
			}
		}

		EntityKey keyToLoad = new EntityKey( event.getEntityId(), persister, source.getEntityMode()  );

		try {
			if ( loadType.isNakedEntityReturned() ) {
				//do not return a proxy!
				//(this option indicates we are initializing a proxy)
				event.setResult( load(event, persister, keyToLoad, loadType) );
			}
			else {
				//return a proxy if appropriate
				if ( event.getLockMode() == LockMode.NONE ) {
					event.setResult( proxyOrLoad(event, persister, keyToLoad, loadType) );
				}
				else {
					event.setResult( lockAndLoad(event, persister, keyToLoad, loadType, source) );
				}
			}
		}
		catch(HibernateException e) {
			log.info("Error performing load command", e);
			throw e;
		}
	}

	/**
	 * Perfoms the load of an entity.
	 *
	 * @return The loaded entity.
	 * @throws HibernateException
	 */
	protected Object load(
		final LoadEvent event, 
		final EntityPersister persister, 
		final EntityKey keyToLoad, 
		final LoadEventListener.LoadType options)
	throws HibernateException {
	
		if ( event.getInstanceToLoad() != null ) {
			if ( event.getSession().getPersistenceContext().getEntry( event.getInstanceToLoad() ) != null ) {
				throw new PersistentObjectException(
						"attempted to load into an instance that was already associated with the session: " +
						MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory() )
					);
			}
			persister.setIdentifier( event.getInstanceToLoad(), event.getEntityId(), event.getSession().getEntityMode() );
		}

		Object entity = doLoad(event, persister, keyToLoad, options);
		
		boolean isOptionalInstance = event.getInstanceToLoad() != null;
		
		if ( !options.isAllowNulls() || isOptionalInstance ) {
			if ( entity == null ) {
				event.getSession().getFactory().getEntityNotFoundDelegate().handleEntityNotFound( event.getEntityClassName(), event.getEntityId() );
			}
		}

		if ( isOptionalInstance && entity != event.getInstanceToLoad() ) {
			throw new NonUniqueObjectException( event.getEntityId(), event.getEntityClassName() );
		}

		return entity;
	}

	/** 
	 * Based on configured options, will either return a pre-existing proxy,
	 * generate a new proxy, or perform an actual load.
	 *
	 * @return The result of the proxy/load operation.
	 * @throws HibernateException
	 */
	protected Object proxyOrLoad(
		final LoadEvent event, 
		final EntityPersister persister,
		final EntityKey keyToLoad, 
		final LoadEventListener.LoadType options) 
	throws HibernateException {
		
		if ( log.isTraceEnabled() ) {
			log.trace(
					"loading entity: " + 
					MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory() )
				);
		}

		if ( !persister.hasProxy() ) {
			// this class has no proxies (so do a shortcut)
			return load(event, persister, keyToLoad, options);
		}
		else {
			final PersistenceContext persistenceContext = event.getSession().getPersistenceContext();

			// look for a proxy
			Object proxy = persistenceContext.getProxy(keyToLoad);
			if ( proxy != null ) {
				return returnNarrowedProxy( event, persister, keyToLoad, options, persistenceContext, proxy );
			}
			else {
				if ( options.isAllowProxyCreation() ) {
					return createProxyIfNecessary( event, persister, keyToLoad, options, persistenceContext );
				}
				else {
					// return a newly loaded object
					return load(event, persister, keyToLoad, options);
				}
			}
			
		}
	}

	/**
	 * Given that there is a pre-existing proxy.
	 * Initialize it if necessary; narrow if necessary.
	 */
	private Object returnNarrowedProxy(
			final LoadEvent event, 
			final EntityPersister persister, 
			final EntityKey keyToLoad, 
			final LoadEventListener.LoadType options, 
			final PersistenceContext persistenceContext, 
			final Object proxy
	) {
		log.trace("entity proxy found in session cache");
		LazyInitializer li = ( (HibernateProxy) proxy ).getHibernateLazyInitializer();
		if ( li.isUnwrap() ) {
			return li.getImplementation();
		}
		Object impl = null;
		if ( !options.isAllowProxyCreation() ) {
			impl = load( event, persister, keyToLoad, options );
			if ( impl == null ) {
				event.getSession().getFactory().getEntityNotFoundDelegate().handleEntityNotFound( persister.getEntityName(), keyToLoad.getIdentifier());
			}
		}
		return persistenceContext.narrowProxy( proxy, persister, keyToLoad, impl );
	}
	
	/**
	 * Given that there is no pre-existing proxy.
	 * Check if the entity is already loaded. If it is, return the entity,
	 * otherwise create and return a proxy.
	 */
	private Object createProxyIfNecessary(
			final LoadEvent event, 
			final EntityPersister persister, 
			final EntityKey keyToLoad, 
			final LoadEventListener.LoadType options,
			final PersistenceContext persistenceContext
	) {
		Object existing = persistenceContext.getEntity( keyToLoad );
		if ( existing != null ) {
			// return existing object or initialized proxy (unless deleted)
			log.trace( "entity found in session cache" );
			if ( options.isCheckDeleted() ) {
				EntityEntry entry = persistenceContext.getEntry( existing );
				Status status = entry.getStatus();
				if ( status == Status.DELETED || status == Status.GONE ) {
					return null;
				}
			}
			return existing;
		}
		else {
			log.trace( "creating new proxy for entity" );
			// return new uninitialized proxy
			Object proxy = persister.createProxy( event.getEntityId(), event.getSession() );
			persistenceContext.getBatchFetchQueue().addBatchLoadableEntityKey(keyToLoad);
			persistenceContext.addProxy(keyToLoad, proxy);
			return proxy;
		}
	}

	/** 
	 * If the class to be loaded has been configured with a cache, then lock
	 * given id in that cache and then perform the load.
	 *
	 * @return The loaded entity
	 * @throws HibernateException
	 */
	protected Object lockAndLoad(
		final LoadEvent event, 
		final EntityPersister persister,
		final EntityKey keyToLoad, 
		final LoadEventListener.LoadType options,
		final SessionImplementor source) 
	throws HibernateException {
		
		CacheConcurrencyStrategy.SoftLock lock = null;
		final CacheKey ck;
		if ( persister.hasCache() ) {
			ck = new CacheKey( 
					event.getEntityId(), 
					persister.getIdentifierType(), 
					persister.getRootEntityName(), 
					source.getEntityMode(), 
					source.getFactory() 
				);
			lock = persister.getCache().lock(ck, null );
		}
		else {
			ck = null;
		}

		Object entity;
		try {
			entity = load(event, persister, keyToLoad, options);
		}
		finally {
			if ( persister.hasCache() ) {
				persister.getCache().release(ck, lock );
			}
		}

		Object proxy = event.getSession().getPersistenceContext()
				.proxyFor( persister, keyToLoad, entity );
		
		return proxy;
	}


	/**
	 * Coordinates the efforts to load a given entity.  First, an attempt is
	 * made to load the entity from the session-level cache.  If not found there,
	 * an attempt is made to locate it in second-level cache.  Lastly, an
	 * attempt is made to load it directly from the datasource.
	 *
	 * @param event The load event
	 * @param persister The persister for the entity being requested for load
	 * @param keyToLoad The EntityKey representing the entity to be loaded.
	 * @param options The load options.
	 * @return The loaded entity, or null.
	 * @throws HibernateException
	 */
	protected Object doLoad(
			final LoadEvent event,
			final EntityPersister persister,
			final EntityKey keyToLoad,
			final LoadEventListener.LoadType options) throws HibernateException {
		
		if ( log.isTraceEnabled() ) {
			log.trace(
					"attempting to resolve: " + 
					MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory() )
				);
		}

		Object entity = loadFromSessionCache( event, keyToLoad, options );
		if ( entity == REMOVED_ENTITY_MARKER ) {
			log.debug( "load request found matching entity in context, but it is scheduled for removal; returning null" );
			return null;
		}
		if ( entity == INCONSISTENT_RTN_CLASS_MARKER ) {
			log.debug( "load request found matching entity in context, but the matched entity was of an inconsistent return type; returning null" );
			return null;
		}
		if ( entity != null ) {
			if ( log.isTraceEnabled() ) {
				log.trace(
						"resolved object in session cache: " +
						MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory()  )
					);
			}
			return entity;
		}

		entity = loadFromSecondLevelCache(event, persister, options);
		if ( entity != null ) {
			if ( log.isTraceEnabled() ) {
				log.trace(
						"resolved object in second-level cache: " +
						MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory() )
					);
			}
			return entity;
		}

		if ( log.isTraceEnabled() ) {
			log.trace(
					"object not resolved in any cache: " +
					MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory() )
				);
		}

		return loadFromDatasource(event, persister, keyToLoad, options);
	}

	/**
	 * Performs the process of loading an entity from the configured
	 * underlying datasource.
	 *
	 * @param event The load event
	 * @param persister The persister for the entity being requested for load
	 * @param keyToLoad The EntityKey representing the entity to be loaded.
	 * @param options The load options.
	 * @return The object loaded from the datasource, or null if not found.
	 * @throws HibernateException
	 */
	protected Object loadFromDatasource(
			final LoadEvent event,
			final EntityPersister persister,
			final EntityKey keyToLoad,
			final LoadEventListener.LoadType options) throws HibernateException {
		final SessionImplementor source = event.getSession();
        final boolean stats = source.getFactory().getStatistics().isStatisticsEnabled();
        long startTime = 0;
        if ( stats ) startTime = System.currentTimeMillis();

        Object entity = persister.load(
				event.getEntityId(),
				event.getInstanceToLoad(),
				event.getLockMode(),
				source
		);

		if ( event.isAssociationFetch() && stats) {
			source.getFactory().getStatisticsImplementor().fetchEntity( event.getEntityClassName(), System.currentTimeMillis() - startTime);
		}

		return entity;
	}

	/**
	 * Attempts to locate the entity in the session-level cache.
	 * <p/>
	 * If allowed to return nulls, then if the entity happens to be found in
	 * the session cache, we check the entity type for proper handling
	 * of entity hierarchies.
	 * <p/>
	 * If checkDeleted was set to true, then if the entity is found in the
	 * session-level cache, it's current status within the session cache
	 * is checked to see if it has previously been scheduled for deletion.
	 *
	 * @param event The load event
	 * @param keyToLoad The EntityKey representing the entity to be loaded.
	 * @param options The load options.
	 * @return The entity from the session-level cache, or null.
	 * @throws HibernateException Generally indicates problems applying a lock-mode.
	 */
	protected Object loadFromSessionCache(
			final LoadEvent event,
			final EntityKey keyToLoad,
			final LoadEventListener.LoadType options) throws HibernateException {
		
		SessionImplementor session = event.getSession();
		Object old = session.getEntityUsingInterceptor( keyToLoad );

		if ( old != null ) {
			// this object was already loaded
			EntityEntry oldEntry = session.getPersistenceContext().getEntry( old );
			if ( options.isCheckDeleted() ) {
				Status status = oldEntry.getStatus();
				if ( status == Status.DELETED || status == Status.GONE ) {
					return REMOVED_ENTITY_MARKER;
				}
			}
			if ( options.isAllowNulls() ) {
				EntityPersister persister = event.getSession().getFactory().getEntityPersister( event.getEntityClassName() );
				if ( ! persister.isInstance( old, event.getSession().getEntityMode() ) ) {
					return INCONSISTENT_RTN_CLASS_MARKER;
				}
			}
			upgradeLock( old, oldEntry, event.getLockMode(), session );
		}

		return old;
	}

	/**
	 * Attempts to load the entity from the second-level cache.
	 *
	 * @param event The load event
	 * @param persister The persister for the entity being requested for load
	 * @param options The load options.
	 * @return The entity from the second-level cache, or null.
	 * @throws HibernateException
	 */
	protected Object loadFromSecondLevelCache(
			final LoadEvent event,
			final EntityPersister persister,
			final LoadEventListener.LoadType options) throws HibernateException {
		
		final SessionImplementor source = event.getSession();
		
		final boolean useCache = persister.hasCache() && 
			source.getCacheMode().isGetEnabled() && 
			event.getLockMode().lessThan(LockMode.READ);
		
		if (useCache) {
			
			final SessionFactoryImplementor factory = source.getFactory();
			
			final CacheKey ck = new CacheKey( 
					event.getEntityId(), 
					persister.getIdentifierType(), 
					persister.getRootEntityName(),
					source.getEntityMode(), 
					source.getFactory()
				);
			Object ce = persister.getCache()
				.get( ck, source.getTimestamp() );
			
			if ( factory.getStatistics().isStatisticsEnabled() ) {
				if (ce==null) {
					factory.getStatisticsImplementor().secondLevelCacheMiss( 
						persister.getCache().getRegionName() 
					);
				}
				else {
					factory.getStatisticsImplementor().secondLevelCacheHit( 
						persister.getCache().getRegionName() 
					);
				}
			}

			if ( ce != null ) {

				CacheEntry entry = (CacheEntry) persister.getCacheEntryStructure()
						.destructure(ce, factory);
			
				// Entity was found in second-level cache...
				return assembleCacheEntry(
						entry,
						event.getEntityId(),
						persister,
						event
					);
			}
		}
		
		return null;
	}

	private Object assembleCacheEntry(
			final CacheEntry entry,
			final Serializable id,
			final EntityPersister persister,
			final LoadEvent event) throws HibernateException {
		
		final Object optionalObject = event.getInstanceToLoad();
		final EventSource session = event.getSession();
		final SessionFactoryImplementor factory = session.getFactory();
		
		if ( log.isTraceEnabled() ) {
			log.trace(
					"assembling entity from second-level cache: " +
					MessageHelper.infoString( persister, id, factory )
				);
		}

		EntityPersister subclassPersister = factory.getEntityPersister( entry.getSubclass() );
		Object result = optionalObject == null ? 
				session.instantiate( subclassPersister, id ) : optionalObject;
				
		// make it circular-reference safe
		TwoPhaseLoad.addUninitializedCachedEntity( 
				new EntityKey( id, subclassPersister, session.getEntityMode() ), 
				result, 
				subclassPersister, 
				LockMode.NONE, 
				entry.areLazyPropertiesUnfetched(),
				entry.getVersion(),
				session
			);

		Type[] types = subclassPersister.getPropertyTypes();
		Object[] values = entry.assemble( result, id, subclassPersister, session.getInterceptor(), session ); // intializes result by side-effect
		TypeFactory.deepCopy( 
				values, 
				types, 
				subclassPersister.getPropertyUpdateability(), 
				values, 
				session
			);
		
		Object version = Versioning.getVersion( values, subclassPersister );
		if ( log.isTraceEnabled() ) log.trace( "Cached Version: " + version );
		
		final PersistenceContext persistenceContext = session.getPersistenceContext();
		persistenceContext.addEntry( 
				result, 
				Status.MANAGED, 
				values, 
				null, 
				id, 
				version, 
				LockMode.NONE, 
				true, 
				subclassPersister, 
				false, 
				entry.areLazyPropertiesUnfetched() 
			);
		subclassPersister.afterInitialize( result, entry.areLazyPropertiesUnfetched(), session );
		persistenceContext.initializeNonLazyCollections();
		// upgrade the lock if necessary:
		//lock(result, lockMode);

		//PostLoad is needed for EJB3
		//TODO: reuse the PostLoadEvent...
		PostLoadEvent postLoadEvent = new PostLoadEvent(session).setEntity(result)
				.setId(id).setPersister(persister);
		PostLoadEventListener[] listeners = session.getListeners().getPostLoadEventListeners();
		for ( int i = 0; i < listeners.length; i++ ) {
			listeners[i].onPostLoad(postLoadEvent);
		}
		
		return result;
	}

}
