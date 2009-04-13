//$Id: TwoPhaseLoad.java 14127 2007-10-20 00:06:51Z scottmarlownovell $
package org.hibernate.engine;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AssertionFailure;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.cache.CacheKey;
import org.hibernate.cache.entry.CacheEntry;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PreLoadEvent;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.intercept.LazyPropertyInitializer;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.property.BackrefPropertyAccessor;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;

/**
 * Functionality relating to Hibernate's two-phase loading process,
 * that may be reused by persisters that do not use the Loader
 * framework
 * 
 * @author Gavin King
 */
public final class TwoPhaseLoad {

	private static final Log log = LogFactory.getLog(TwoPhaseLoad.class);
	
	private TwoPhaseLoad() {}

	/**
	 * Register the "hydrated" state of an entity instance, after the first step of 2-phase loading.
	 * 
	 * Add the "hydrated state" (an array) of an uninitialized entity to the session. We don't try
	 * to resolve any associations yet, because there might be other entities waiting to be
	 * read from the JDBC result set we are currently processing
	 */
	public static void postHydrate(
		final EntityPersister persister, 
		final Serializable id, 
		final Object[] values, 
		final Object rowId,
		final Object object, 
		final LockMode lockMode,
		final boolean lazyPropertiesAreUnfetched, 
		final SessionImplementor session) 
	throws HibernateException {
		
		Object version = Versioning.getVersion(values, persister);
		session.getPersistenceContext().addEntry( 
				object, 
				Status.LOADING,
				values, 
				rowId, 
				id, 
				version, 
				lockMode, 
				true, 
				persister, 
				false, 
				lazyPropertiesAreUnfetched 
			);
	
		if ( log.isTraceEnabled() && version!=null ) {
			String versionStr = persister.isVersioned()
					? persister.getVersionType().toLoggableString( version, session.getFactory() )
			        : "null";
			log.trace( "Version: " + versionStr );
		}
	
	}

	/**
	 * Perform the second step of 2-phase load. Fully initialize the entity 
	 * instance.
	 *
	 * After processing a JDBC result set, we "resolve" all the associations
	 * between the entities which were instantiated and had their state
	 * "hydrated" into an array
	 */
	public static void initializeEntity(
			final Object entity, 
			final boolean readOnly,
			final SessionImplementor session,
			final PreLoadEvent preLoadEvent,
			final PostLoadEvent postLoadEvent) throws HibernateException {
		
		//TODO: Should this be an InitializeEntityEventListener??? (watch out for performance!)
        final SessionFactoryImplementor factory = session.getFactory();
        final boolean stats = factory.getStatistics().isStatisticsEnabled();
        long startTime = 0;
        if ( stats ) startTime = System.currentTimeMillis();

		final PersistenceContext persistenceContext = session.getPersistenceContext();
		EntityEntry entityEntry = persistenceContext.getEntry(entity);
		if ( entityEntry == null ) {
			throw new AssertionFailure( "possible non-threadsafe access to the session" );
		}
		EntityPersister persister = entityEntry.getPersister();
		Serializable id = entityEntry.getId();
		Object[] hydratedState = entityEntry.getLoadedState();
	
		if ( log.isDebugEnabled() )
			log.debug(
					"resolving associations for " +
					MessageHelper.infoString(persister, id, session.getFactory())
				);
	
		Type[] types = persister.getPropertyTypes();
		for ( int i = 0; i < hydratedState.length; i++ ) {
			final Object value = hydratedState[i];
			if ( value!=LazyPropertyInitializer.UNFETCHED_PROPERTY && value!=BackrefPropertyAccessor.UNKNOWN ) {
				hydratedState[i] = types[i].resolve( value, session, entity );
			}
		}
	
		//Must occur after resolving identifiers!
		if ( session.isEventSource() ) {
			preLoadEvent.setEntity(entity).setState(hydratedState).setId(id).setPersister(persister);
			PreLoadEventListener[] listeners = session.getListeners().getPreLoadEventListeners();
			for ( int i = 0; i < listeners.length; i++ ) {
				listeners[i].onPreLoad(preLoadEvent);
			}
		}
	
		persister.setPropertyValues( entity, hydratedState, session.getEntityMode() );
	
		if ( persister.hasCache() && session.getCacheMode().isPutEnabled() ) {
			
			if ( log.isDebugEnabled() )
				log.debug(
						"adding entity to second-level cache: " +
						MessageHelper.infoString( persister, id, session.getFactory() )
					);

			Object version = Versioning.getVersion(hydratedState, persister);
			CacheEntry entry = new CacheEntry(
					hydratedState, 
					persister, 
					entityEntry.isLoadedWithLazyPropertiesUnfetched(), 
					version, 
					session, 
					entity
				);
			CacheKey cacheKey = new CacheKey( 
					id, 
					persister.getIdentifierType(), 
					persister.getRootEntityName(), 
					session.getEntityMode(), 
					session.getFactory() 
				);
			boolean put = persister.getCache().put( 
					cacheKey, 
					persister.getCacheEntryStructure().structure(entry), 
					session.getTimestamp(),
					version, 
					persister.isVersioned() ?
							persister.getVersionType().getComparator() : 
							null,
					useMinimalPuts(session, entityEntry)
				); //we could use persister.hasLazyProperties() instead of true
	
			if ( put && factory.getStatistics().isStatisticsEnabled() ) {
				factory.getStatisticsImplementor().secondLevelCachePut( persister.getCache().getRegionName() );
			}
		}
	
		if ( readOnly || !persister.isMutable() ) {
			//no need to take a snapshot - this is a 
			//performance optimization, but not really
			//important, except for entities with huge 
			//mutable property values
			persistenceContext.setEntryStatus(entityEntry, Status.READ_ONLY);
		}
		else {
			//take a snapshot
			TypeFactory.deepCopy( 
					hydratedState, 
					persister.getPropertyTypes(), 
					persister.getPropertyUpdateability(), 
					hydratedState,  //after setting values to object, entityMode
					session
				);
			persistenceContext.setEntryStatus(entityEntry, Status.MANAGED);
		}
		
		persister.afterInitialize(
				entity, 
				entityEntry.isLoadedWithLazyPropertiesUnfetched(), 
				session
			);
		
		if ( session.isEventSource() ) {
			postLoadEvent.setEntity(entity).setId(id).setPersister(persister);
			PostLoadEventListener[] listeners = session.getListeners().getPostLoadEventListeners();
			for ( int i = 0; i < listeners.length; i++ ) {
				listeners[i].onPostLoad(postLoadEvent);
			}
		}
		
		if ( log.isDebugEnabled() )
			log.debug(
					"done materializing entity " +
					MessageHelper.infoString( persister, id, session.getFactory() )
				);
		
		if ( stats) {
			factory.getStatisticsImplementor().loadEntity( persister.getEntityName(), System.currentTimeMillis() - startTime);
		}
	
	}

	private static boolean useMinimalPuts(SessionImplementor session, EntityEntry entityEntry) {
		return ( session.getFactory().getSettings().isMinimalPutsEnabled() && 
						session.getCacheMode()!=CacheMode.REFRESH ) ||
				( entityEntry.getPersister().hasLazyProperties() && 
						entityEntry.isLoadedWithLazyPropertiesUnfetched() && 
						entityEntry.getPersister().isLazyPropertiesCacheable() );
	}

	/**
	 * Add an uninitialized instance of an entity class, as a placeholder to ensure object 
	 * identity. Must be called before <tt>postHydrate()</tt>.
	 *
	 * Create a "temporary" entry for a newly instantiated entity. The entity is uninitialized,
	 * but we need the mapping from id to instance in order to guarantee uniqueness.
	 */
	public static void addUninitializedEntity(
			final EntityKey key, 
			final Object object, 
			final EntityPersister persister, 
			final LockMode lockMode,
			final boolean lazyPropertiesAreUnfetched, 
			final SessionImplementor session
	) {
		session.getPersistenceContext().addEntity(
				object, 
				Status.LOADING, 
				null, 
				key, 
				null, 
				lockMode, 
				true, 
				persister, 
				false, 
				lazyPropertiesAreUnfetched
			);
	}

	public static void addUninitializedCachedEntity(
			final EntityKey key, 
			final Object object, 
			final EntityPersister persister, 
			final LockMode lockMode,
			final boolean lazyPropertiesAreUnfetched,
			final Object version,
			final SessionImplementor session
	) {
		session.getPersistenceContext().addEntity(
				object, 
				Status.LOADING, 
				null, 
				key, 
				version, 
				lockMode, 
				true, 
				persister, 
				false, 
				lazyPropertiesAreUnfetched
			);
	}
}
