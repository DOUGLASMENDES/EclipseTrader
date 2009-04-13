//$Id: SessionFactoryImpl.java 10863 2006-11-22 12:53:53Z steve.ebersole@jboss.com $
package org.hibernate.impl;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AssertionFailure;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.QueryException;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.engine.query.sql.NativeSQLQuerySpecification;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheConcurrencyStrategy;
import org.hibernate.cache.CacheFactory;
import org.hibernate.cache.CacheKey;
import org.hibernate.cache.OptimisticCache;
import org.hibernate.cache.QueryCache;
import org.hibernate.cache.UpdateTimestampsCache;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Settings;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.context.CurrentSessionContext;
import org.hibernate.context.JTASessionContext;
import org.hibernate.context.ManagedSessionContext;
import org.hibernate.context.ThreadLocalSessionContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.NamedQueryDefinition;
import org.hibernate.engine.NamedSQLQueryDefinition;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.query.QueryPlanCache;
import org.hibernate.event.EventListeners;
import org.hibernate.exception.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.jdbc.BatcherFactory;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.PersisterFactory;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.StatisticsImpl;
import org.hibernate.stat.StatisticsImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.hibernate.transaction.TransactionFactory;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.util.CollectionHelper;
import org.hibernate.util.ReflectHelper;


/**
 * Concrete implementation of the <tt>SessionFactory</tt> interface. Has the following
 * responsibilites
 * <ul>
 * <li>caches configuration settings (immutably)
 * <li>caches "compiled" mappings ie. <tt>EntityPersister</tt>s and
 *     <tt>CollectionPersister</tt>s (immutable)
 * <li>caches "compiled" queries (memory sensitive cache)
 * <li>manages <tt>PreparedStatement</tt>s
 * <li> delegates JDBC <tt>Connection</tt> management to the <tt>ConnectionProvider</tt>
 * <li>factory for instances of <tt>SessionImpl</tt>
 * </ul>
 * This class must appear immutable to clients, even if it does all kinds of caching
 * and pooling under the covers. It is crucial that the class is not only thread
 * safe, but also highly concurrent. Synchronization must be used extremely sparingly.
 *
 * @see org.hibernate.connection.ConnectionProvider
 * @see org.hibernate.classic.Session
 * @see org.hibernate.hql.QueryTranslator
 * @see org.hibernate.persister.entity.EntityPersister
 * @see org.hibernate.persister.collection.CollectionPersister
 * @author Gavin King
 */
public final class SessionFactoryImpl implements SessionFactory, SessionFactoryImplementor {

	private final String name;
	private final String uuid;

	private final transient Map entityPersisters;
	private final transient Map classMetadata;
	private final transient Map collectionPersisters;
	private final transient Map collectionMetadata;
	private final transient Map collectionRolesByEntityParticipant;
	private final transient Map identifierGenerators;
	private final transient Map namedQueries;
	private final transient Map namedSqlQueries;
	private final transient Map sqlResultSetMappings;
	private final transient Map filters;
	private final transient Map imports;
	private final transient Interceptor interceptor;
	private final transient Settings settings;
	private final transient Properties properties;
	private transient SchemaExport schemaExport;
	private final transient TransactionManager transactionManager;
	private final transient QueryCache queryCache;
	private final transient UpdateTimestampsCache updateTimestampsCache;
	private final transient Map queryCaches;
	private final transient Map allCacheRegions = new HashMap();
	private final transient StatisticsImpl statistics = new StatisticsImpl(this);
	private final transient EventListeners eventListeners;
	private final transient CurrentSessionContext currentSessionContext;
	private final transient EntityNotFoundDelegate entityNotFoundDelegate;
	private final transient SQLFunctionRegistry sqlFunctionRegistry;
	
	private final QueryPlanCache queryPlanCache = new QueryPlanCache( this );

	private transient boolean isClosed = false;
	

	private static final IdentifierGenerator UUID_GENERATOR = new UUIDHexGenerator();

	private static final Log log = LogFactory.getLog(SessionFactoryImpl.class);

	public SessionFactoryImpl(
			Configuration cfg,
	        Mapping mapping,
	        Settings settings,
	        EventListeners listeners)
	throws HibernateException {

		log.info("building session factory");

		this.properties = new Properties();
		this.properties.putAll( cfg.getProperties() );
		this.interceptor = cfg.getInterceptor();
		this.settings = settings;
		this.sqlFunctionRegistry = new SQLFunctionRegistry(settings.getDialect(), cfg.getSqlFunctions());
        this.eventListeners = listeners;
        this.filters = new HashMap();
		this.filters.putAll( cfg.getFilterDefinitions() );

		if ( log.isDebugEnabled() ) {
			log.debug("Session factory constructed with filter configurations : " + filters);
		}

		if ( log.isDebugEnabled() ) {
			log.debug(
					"instantiating session factory with properties: " + properties
			);
		}

		// Caches
		settings.getCacheProvider().start( properties );

		//Generators:

		identifierGenerators = new HashMap();
		Iterator classes = cfg.getClassMappings();
		while ( classes.hasNext() ) {
			PersistentClass model = (PersistentClass) classes.next();
			if ( !model.isInherited() ) {
				IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(
						settings.getDialect(),
				        settings.getDefaultCatalogName(),
				        settings.getDefaultSchemaName(),
				        (RootClass) model
				);
				identifierGenerators.put( model.getEntityName(), generator );
			}
		}

		//Persisters:

		Map caches = new HashMap();
		entityPersisters = new HashMap();
		Map classMeta = new HashMap();
		classes = cfg.getClassMappings();
		while ( classes.hasNext() ) {
			PersistentClass model = (PersistentClass) classes.next();
			model.prepareTemporaryTables( mapping, settings.getDialect() );
			String cacheRegion = model.getRootClass().getCacheRegionName();
			CacheConcurrencyStrategy cache = (CacheConcurrencyStrategy) caches.get(cacheRegion);
			if (cache==null) {
				cache = CacheFactory.createCache(
						model.getCacheConcurrencyStrategy(),
				        cacheRegion,
				        model.isMutable(),
				        settings,
				        properties
					);
				if (cache!=null) {
					caches.put(cacheRegion, cache);
					allCacheRegions.put( cache.getRegionName(), cache.getCache() );
				}
			}
			EntityPersister cp = PersisterFactory.createClassPersister(model, cache, this, mapping);
			if ( cache != null && cache.getCache() instanceof OptimisticCache ) {
				( ( OptimisticCache ) cache.getCache() ).setSource( cp );
			}
			entityPersisters.put( model.getEntityName(), cp );
			classMeta.put( model.getEntityName(), cp.getClassMetadata() );
		}
		classMetadata = Collections.unmodifiableMap(classMeta);

		Map tmpEntityToCollectionRoleMap = new HashMap();
		collectionPersisters = new HashMap();
		Iterator collections = cfg.getCollectionMappings();
		while ( collections.hasNext() ) {
			Collection model = (Collection) collections.next();
			CacheConcurrencyStrategy cache = CacheFactory.createCache(
					model.getCacheConcurrencyStrategy(),
			    	model.getCacheRegionName(),
			    	model.isMutable(),
			    	settings,
			    	properties
			);
			if ( cache != null ) {
				allCacheRegions.put( cache.getRegionName(), cache.getCache() );
			}
			CollectionPersister persister = PersisterFactory.createCollectionPersister(cfg, model, cache, this);
			collectionPersisters.put( model.getRole(), persister.getCollectionMetadata() );
			Type indexType = persister.getIndexType();
			if ( indexType != null && indexType.isAssociationType() && !indexType.isAnyType() ) {
				String entityName = ( ( AssociationType ) indexType ).getAssociatedEntityName( this );
				Set roles = ( Set ) tmpEntityToCollectionRoleMap.get( entityName );
				if ( roles == null ) {
					roles = new HashSet();
					tmpEntityToCollectionRoleMap.put( entityName, roles );
				}
				roles.add( persister.getRole() );
			}
			Type elementType = persister.getElementType();
			if ( elementType.isAssociationType() && !elementType.isAnyType() ) {
				String entityName = ( ( AssociationType ) elementType ).getAssociatedEntityName( this );
				Set roles = ( Set ) tmpEntityToCollectionRoleMap.get( entityName );
				if ( roles == null ) {
					roles = new HashSet();
					tmpEntityToCollectionRoleMap.put( entityName, roles );
				}
				roles.add( persister.getRole() );
			}
		}
		collectionMetadata = Collections.unmodifiableMap(collectionPersisters);
		Iterator itr = tmpEntityToCollectionRoleMap.entrySet().iterator();
		while ( itr.hasNext() ) {
			final Map.Entry entry = ( Map.Entry ) itr.next();
			entry.setValue( Collections.unmodifiableSet( ( Set ) entry.getValue() ) );
		}
		collectionRolesByEntityParticipant = Collections.unmodifiableMap( tmpEntityToCollectionRoleMap );

		//Named Queries:
		namedQueries = new HashMap( cfg.getNamedQueries() );
		namedSqlQueries = new HashMap( cfg.getNamedSQLQueries() );
		sqlResultSetMappings = new HashMap( cfg.getSqlResultSetMappings() );
		imports = new HashMap( cfg.getImports() );

		// after *all* persisters and named queries are registered
		Iterator iter = entityPersisters.values().iterator();
		while ( iter.hasNext() ) {
			( (EntityPersister) iter.next() ).postInstantiate();
		}
		iter = collectionPersisters.values().iterator();
		while ( iter.hasNext() ) {
			( (CollectionPersister) iter.next() ).postInstantiate();
		}

		//JNDI + Serialization:

		name = settings.getSessionFactoryName();
		try {
			uuid = (String) UUID_GENERATOR.generate(null, null);
		}
		catch (Exception e) {
			throw new AssertionFailure("Could not generate UUID");
		}
		SessionFactoryObjectFactory.addInstance(uuid, name, this, properties);

		log.debug("instantiated session factory");

		if ( settings.isAutoCreateSchema() ) {
			new SchemaExport( cfg, settings ).create( false, true );
		}
		if ( settings.isAutoUpdateSchema() ) {
			new SchemaUpdate( cfg, settings ).execute( false, true );
		}
		if ( settings.isAutoValidateSchema() ) {
			new SchemaValidator( cfg, settings ).validate();
		}
		if ( settings.isAutoDropSchema() ) {
			schemaExport = new SchemaExport( cfg, settings );
		}

		if ( settings.getTransactionManagerLookup()!=null ) {
			log.debug("obtaining JTA TransactionManager");
			transactionManager = settings.getTransactionManagerLookup().getTransactionManager(properties);
		}
		else {
			if ( settings.getTransactionFactory().isTransactionManagerRequired() ) {
				throw new HibernateException("The chosen transaction strategy requires access to the JTA TransactionManager");
			}
			transactionManager = null;
		}

		currentSessionContext = buildCurrentSessionContext();

		if ( settings.isQueryCacheEnabled() ) {
			updateTimestampsCache = new UpdateTimestampsCache(settings, properties);
			queryCache = settings.getQueryCacheFactory()
			        .getQueryCache(null, updateTimestampsCache, settings, properties);
			queryCaches = new HashMap();
			allCacheRegions.put( updateTimestampsCache.getRegionName(), updateTimestampsCache.getCache() );
			allCacheRegions.put( queryCache.getRegionName(), queryCache.getCache() );
		}
		else {
			updateTimestampsCache = null;
			queryCache = null;
			queryCaches = null;
		}

		//checking for named queries
		if ( settings.isNamedQueryStartupCheckingEnabled() ) {
			Map errors = checkNamedQueries();
			if ( !errors.isEmpty() ) {
				Set keys = errors.keySet();
				StringBuffer failingQueries = new StringBuffer( "Errors in named queries: " );
				for ( Iterator iterator = keys.iterator() ; iterator.hasNext() ; ) {
					String queryName = ( String ) iterator.next();
					HibernateException e = ( HibernateException ) errors.get( queryName );
					failingQueries.append( queryName );
					if ( iterator.hasNext() ) {
						failingQueries.append( ", " );
					}
					log.error( "Error in named query: " + queryName, e );
				}
				throw new HibernateException( failingQueries.toString() );
			}
		}

		//stats
		getStatistics().setStatisticsEnabled( settings.isStatisticsEnabled() );

		// EntityNotFoundDelegate
		EntityNotFoundDelegate entityNotFoundDelegate = cfg.getEntityNotFoundDelegate();
		if ( entityNotFoundDelegate == null ) {
			entityNotFoundDelegate = new EntityNotFoundDelegate() {
				public void handleEntityNotFound(String entityName, Serializable id) {
					throw new ObjectNotFoundException( id, entityName );
				}
			};
		}
		this.entityNotFoundDelegate = entityNotFoundDelegate;
	}

	public QueryPlanCache getQueryPlanCache() {
		return queryPlanCache;
	}

	private Map checkNamedQueries() throws HibernateException {
		Map errors = new HashMap();

		// Check named HQL queries
		log.debug("Checking " + namedQueries.size() + " named HQL queries");
		Iterator itr = namedQueries.entrySet().iterator();
		while ( itr.hasNext() ) {
			final Map.Entry entry = ( Map.Entry ) itr.next();
			final String queryName = ( String ) entry.getKey();
			final NamedQueryDefinition qd = ( NamedQueryDefinition ) entry.getValue();
			// this will throw an error if there's something wrong.
			try {
				log.debug("Checking named query: " + queryName);
				//TODO: BUG! this currently fails for named queries for non-POJO entities
				queryPlanCache.getHQLQueryPlan( qd.getQueryString(), false, CollectionHelper.EMPTY_MAP );
			}
			catch ( QueryException e ) {
				errors.put( queryName, e );
			}
			catch ( MappingException e ) {
				errors.put( queryName, e );
			}
		}

		log.debug("Checking " + namedSqlQueries.size() + " named SQL queries");
		itr = namedSqlQueries.entrySet().iterator();
		while ( itr.hasNext() ) {
			final Map.Entry entry = ( Map.Entry ) itr.next();
			final String queryName = ( String ) entry.getKey();
			final NamedSQLQueryDefinition qd = ( NamedSQLQueryDefinition ) entry.getValue();
			// this will throw an error if there's something wrong.
			try {
				log.debug("Checking named SQL query: " + queryName);
				// TODO : would be really nice to cache the spec on the query-def so as to not have to re-calc the hash;
				// currently not doable though because of the resultset-ref stuff...
				NativeSQLQuerySpecification spec = null;
				if ( qd.getResultSetRef() != null ) {
					ResultSetMappingDefinition definition = ( ResultSetMappingDefinition )
							sqlResultSetMappings.get( qd.getResultSetRef() );
					if ( definition == null ) {
						throw new MappingException(
								"Unable to find resultset-ref definition: " + qd.getResultSetRef()
							);
					}
					spec = new NativeSQLQuerySpecification(
							qd.getQueryString(),
					        definition.getQueryReturns(),
					        qd.getQuerySpaces()
					);
				}
				else
				{
					spec =  new NativeSQLQuerySpecification(
							qd.getQueryString(),
					        qd.getQueryReturns(),
					        qd.getQuerySpaces()
					);
				}
				queryPlanCache.getNativeSQLQueryPlan( spec );
			}
			catch ( QueryException e ) {
				errors.put( queryName, e );
			}
			catch ( MappingException e ) {
				errors.put( queryName, e );
			}
		}

		return errors;
	}

	public StatelessSession openStatelessSession() {
		return new StatelessSessionImpl( null, this );
	}

	public StatelessSession openStatelessSession(Connection connection) {
		return new StatelessSessionImpl( connection, this );
	}

	private SessionImpl openSession(
		Connection connection,
	    boolean autoClose,
	    long timestamp,
	    Interceptor sessionLocalInterceptor
	) {
		return new SessionImpl(
		        connection,
		        this,
		        autoClose,
		        timestamp,
		        sessionLocalInterceptor == null ? interceptor : sessionLocalInterceptor,
		        settings.getDefaultEntityMode(),
		        settings.isFlushBeforeCompletionEnabled(),
		        settings.isAutoCloseSessionEnabled(),
		        settings.getConnectionReleaseMode()
			);
	}

	public org.hibernate.classic.Session openSession(Connection connection, Interceptor sessionLocalInterceptor) {
		return openSession(connection, false, Long.MIN_VALUE, sessionLocalInterceptor);
	}

	public org.hibernate.classic.Session openSession(Interceptor sessionLocalInterceptor)
	throws HibernateException {
		// note that this timestamp is not correct if the connection provider
		// returns an older JDBC connection that was associated with a
		// transaction that was already begun before openSession() was called
		// (don't know any possible solution to this!)
		long timestamp = settings.getCacheProvider().nextTimestamp();
		return openSession( null, true, timestamp, sessionLocalInterceptor );
	}

	public org.hibernate.classic.Session openSession(Connection connection) {
		return openSession(connection, interceptor); //prevents this session from adding things to cache
	}

	public org.hibernate.classic.Session openSession() throws HibernateException {
		return openSession(interceptor);
	}

	public org.hibernate.classic.Session openTemporarySession() throws HibernateException {
		return new SessionImpl(
				null,
		        this,
		        true,
		        settings.getCacheProvider().nextTimestamp(),
		        interceptor,
		        settings.getDefaultEntityMode(),
		        false,
		        false,
		        ConnectionReleaseMode.AFTER_STATEMENT
			);
	}

	public org.hibernate.classic.Session openSession(
			final Connection connection,
	        final boolean flushBeforeCompletionEnabled,
	        final boolean autoCloseSessionEnabled,
	        final ConnectionReleaseMode connectionReleaseMode) throws HibernateException {
		return new SessionImpl(
				connection,
		        this,
		        true,
		        settings.getCacheProvider().nextTimestamp(),
		        interceptor,
		        settings.getDefaultEntityMode(),
		        flushBeforeCompletionEnabled,
		        autoCloseSessionEnabled,
		        connectionReleaseMode
			);
	}

	public org.hibernate.classic.Session getCurrentSession() throws HibernateException {
		if ( currentSessionContext == null ) {
			throw new HibernateException( "No CurrentSessionContext configured!" );
		}
		return currentSessionContext.currentSession();
	}

	public EntityPersister getEntityPersister(String entityName) throws MappingException {
		EntityPersister result = (EntityPersister) entityPersisters.get(entityName);
		if (result==null) {
			throw new MappingException( "Unknown entity: " + entityName );
		}
		return result;
	}

	public CollectionPersister getCollectionPersister(String role) throws MappingException {
		CollectionPersister result = (CollectionPersister) collectionPersisters.get(role);
		if (result==null) {
			throw new MappingException( "Unknown collection role: " + role );
		}
		return result;
	}

	public Settings getSettings() {
		return settings;
	}

	public Dialect getDialect() {
		return settings.getDialect();
	}

	public Interceptor getInterceptor()
	{
		return interceptor;
	}

	public TransactionFactory getTransactionFactory() {
		return settings.getTransactionFactory();
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public SQLExceptionConverter getSQLExceptionConverter() {
		return settings.getSQLExceptionConverter();
	}

	public Set getCollectionRolesByEntityParticipant(String entityName) {
		return ( Set ) collectionRolesByEntityParticipant.get( entityName );
	}

	// from javax.naming.Referenceable
	public Reference getReference() throws NamingException {
		log.debug("Returning a Reference to the SessionFactory");
		return new Reference(
			SessionFactoryImpl.class.getName(),
		    new StringRefAddr("uuid", uuid),
		    SessionFactoryObjectFactory.class.getName(),
		    null
		);
	}

	private Object readResolve() throws ObjectStreamException {
		log.trace("Resolving serialized SessionFactory");
		// look for the instance by uuid
		Object result = SessionFactoryObjectFactory.getInstance(uuid);
		if (result==null) {
			// in case we were deserialized in a different JVM, look for an instance with the same name
			// (alternatively we could do an actual JNDI lookup here....)
			result = SessionFactoryObjectFactory.getNamedInstance(name);
			if (result==null) {
				throw new InvalidObjectException("Could not find a SessionFactory named: " + name);
			}
			else {
				log.debug("resolved SessionFactory by name");
			}
		}
		else {
			log.debug("resolved SessionFactory by uid");
		}
		return result;
	}

	public NamedQueryDefinition getNamedQuery(String queryName) {
		return (NamedQueryDefinition) namedQueries.get(queryName);
	}

	public NamedSQLQueryDefinition getNamedSQLQuery(String queryName) {
		return (NamedSQLQueryDefinition) namedSqlQueries.get(queryName);
	}

	public ResultSetMappingDefinition getResultSetMapping(String resultSetName) {
		return (ResultSetMappingDefinition) sqlResultSetMappings.get(resultSetName);
	}

	public Type getIdentifierType(String className) throws MappingException {
		return getEntityPersister(className).getIdentifierType();
	}
	public String getIdentifierPropertyName(String className) throws MappingException {
		return getEntityPersister(className).getIdentifierPropertyName();
	}

	private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		log.trace("deserializing");
		in.defaultReadObject();
		log.debug("deserialized: " + uuid);
	}
	private final void writeObject(ObjectOutputStream out) throws IOException {
		log.debug("serializing: " + uuid);
		out.defaultWriteObject();
		log.trace("serialized");
	}

	public Type[] getReturnTypes(String queryString) throws HibernateException {
		return queryPlanCache.getHQLQueryPlan( queryString, false, CollectionHelper.EMPTY_MAP ).getReturnMetadata().getReturnTypes();
	}

	public String[] getReturnAliases(String queryString) throws HibernateException {
		return queryPlanCache.getHQLQueryPlan( queryString, false, CollectionHelper.EMPTY_MAP ).getReturnMetadata().getReturnAliases();
	}

	public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
		return getClassMetadata( persistentClass.getName() );
	}

	public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
		return (CollectionMetadata) collectionMetadata.get(roleName);
	}

	public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
		return (ClassMetadata) classMetadata.get(entityName);
	}

	/**
	 * Return the names of all persistent (mapped) classes that extend or implement the
	 * given class or interface, accounting for implicit/explicit polymorphism settings
	 * and excluding mapped subclasses/joined-subclasses of other classes in the result.
	 */
	public String[] getImplementors(String className) throws MappingException {

		final Class clazz;
		try {
			clazz = ReflectHelper.classForName(className);
		}
		catch (ClassNotFoundException cnfe) {
			return new String[] { className }; //for a dynamic-class
		}

		ArrayList results = new ArrayList();
		Iterator iter = entityPersisters.values().iterator();
		while ( iter.hasNext() ) {
			//test this entity to see if we must query it
			EntityPersister testPersister = (EntityPersister) iter.next();
			if ( testPersister instanceof Queryable ) {
				Queryable testQueryable = (Queryable) testPersister;
				String testClassName = testQueryable.getEntityName();
				boolean isMappedClass = className.equals(testClassName);
				if ( testQueryable.isExplicitPolymorphism() ) {
					if ( isMappedClass ) {
						return new String[] {className}; //NOTE EARLY EXIT
					}
				}
				else {
					if (isMappedClass) {
						results.add(testClassName);
					}
					else {
						final Class mappedClass = testQueryable.getMappedClass( EntityMode.POJO );
						if ( mappedClass!=null && clazz.isAssignableFrom( mappedClass ) ) {
							final boolean assignableSuperclass;
							if ( testQueryable.isInherited() ) {
								Class mappedSuperclass = getEntityPersister( testQueryable.getMappedSuperclass() ).getMappedClass( EntityMode.POJO);
								assignableSuperclass = clazz.isAssignableFrom(mappedSuperclass);
							}
							else {
								assignableSuperclass = false;
							}
							if ( !assignableSuperclass ) {
								results.add( testClassName );
							}
						}
					}
				}
			}
		}
		return (String[]) results.toArray( new String[ results.size() ] );
	}

	public String getImportedClassName(String className) {
		String result = (String) imports.get(className);
		if (result==null) {
			try {
				ReflectHelper.classForName(className);
				return className;
			}
			catch (ClassNotFoundException cnfe) {
				return null;
			}
		}
		else {
			return result;
		}
	}

	public Map getAllClassMetadata() throws HibernateException {
		return classMetadata;
	}

	public Map getAllCollectionMetadata() throws HibernateException {
		return collectionMetadata;
	}

	/**
	 * Closes the session factory, releasing all held resources.
	 *
	 * <ol>
	 * <li>cleans up used cache regions and "stops" the cache provider.
	 * <li>close the JDBC connection
	 * <li>remove the JNDI binding
	 * </ol>
	 *
	 * Note: Be aware that the sessionfactory instance still can
	 * be a "heavy" object memory wise after close() has been called.  Thus
	 * it is important to not keep referencing the instance to let the garbage
	 * collector release the memory.
	 */
	public void close() throws HibernateException {

		log.info("closing");

		isClosed = true;

		Iterator iter = entityPersisters.values().iterator();
		while ( iter.hasNext() ) {
			EntityPersister p = (EntityPersister) iter.next();
			if ( p.hasCache() ) {
				p.getCache().destroy();
			}
		}

		iter = collectionPersisters.values().iterator();
		while ( iter.hasNext() ) {
			CollectionPersister p = (CollectionPersister) iter.next();
			if ( p.hasCache() ) {
				p.getCache().destroy();
			}
		}

		if ( settings.isQueryCacheEnabled() )  {
			queryCache.destroy();

			iter = queryCaches.values().iterator();
			while ( iter.hasNext() ) {
				QueryCache cache = (QueryCache) iter.next();
				cache.destroy();
			}
			updateTimestampsCache.destroy();
		}

		settings.getCacheProvider().stop();

		try {
			settings.getConnectionProvider().close();
		}
		finally {
			SessionFactoryObjectFactory.removeInstance(uuid, name, properties);
		}

		if ( settings.isAutoDropSchema() ) {
			schemaExport.drop( false, true );
		}

	}

	public void evictEntity(String entityName, Serializable id) throws HibernateException {
		EntityPersister p = getEntityPersister(entityName);
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + MessageHelper.infoString(p, id, this) );
			}
			CacheKey cacheKey = new CacheKey( id, p.getIdentifierType(), p.getRootEntityName(), EntityMode.POJO, this );
			p.getCache().remove( cacheKey );
		}
	}

	public void evictEntity(String entityName) throws HibernateException {
		EntityPersister p = getEntityPersister(entityName);
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + p.getEntityName() );
			}
			p.getCache().clear();
		}
	}

	public void evict(Class persistentClass, Serializable id) throws HibernateException {
		EntityPersister p = getEntityPersister( persistentClass.getName() );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + MessageHelper.infoString(p, id, this) );
			}
			CacheKey cacheKey = new CacheKey( id, p.getIdentifierType(), p.getRootEntityName(), EntityMode.POJO, this );
			p.getCache().remove( cacheKey );
		}
	}

	public void evict(Class persistentClass) throws HibernateException {
		EntityPersister p = getEntityPersister( persistentClass.getName() );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + p.getEntityName() );
			}
			p.getCache().clear();
		}
	}

	public void evictCollection(String roleName, Serializable id) throws HibernateException {
		CollectionPersister p = getCollectionPersister(roleName);
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + MessageHelper.collectionInfoString(p, id, this) );
			}
			CacheKey cacheKey = new CacheKey( id, p.getKeyType(), p.getRole(), EntityMode.POJO, this );
			p.getCache().remove( cacheKey );
		}
	}

	public void evictCollection(String roleName) throws HibernateException {
		CollectionPersister p = getCollectionPersister(roleName);
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + p.getRole() );
			}
			p.getCache().clear();
		}
	}

	public Type getReferencedPropertyType(String className, String propertyName)
		throws MappingException {
		return getEntityPersister(className).getPropertyType(propertyName);
	}

	public ConnectionProvider getConnectionProvider() {
		return settings.getConnectionProvider();
	}

	public UpdateTimestampsCache getUpdateTimestampsCache() {
		return updateTimestampsCache;
	}

	public QueryCache getQueryCache() {
		return queryCache;
	}

	public QueryCache getQueryCache(String cacheRegion) throws HibernateException {
		if (cacheRegion==null) {
			return getQueryCache();
		}

		if ( !settings.isQueryCacheEnabled() ) {
			return null;
		}

		synchronized (allCacheRegions) {
			QueryCache currentQueryCache = (QueryCache) queryCaches.get(cacheRegion);
			if (currentQueryCache==null) {
				currentQueryCache = settings.getQueryCacheFactory()
					.getQueryCache(cacheRegion, updateTimestampsCache, settings, properties);
				queryCaches.put(cacheRegion, currentQueryCache);
				allCacheRegions.put( currentQueryCache.getRegionName(), currentQueryCache.getCache() );
			}
			return currentQueryCache;
		}
	}

	public Cache getSecondLevelCacheRegion(String regionName) {
		synchronized (allCacheRegions) {
			return (Cache) allCacheRegions.get(regionName);
		}
	}

	public Map getAllSecondLevelCacheRegions() {
		synchronized (allCacheRegions) {
			return new HashMap(allCacheRegions);
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public StatisticsImplementor getStatisticsImplementor() {
		return statistics;
	}

	public void evictQueries() throws HibernateException {
		if ( settings.isQueryCacheEnabled() ) {
			queryCache.clear();
		}
	}

	public void evictQueries(String cacheRegion) throws HibernateException {
		if (cacheRegion==null) {
			throw new NullPointerException("use the zero-argument form to evict the default query cache");
		}
		else {
			synchronized (allCacheRegions) {
				if ( settings.isQueryCacheEnabled() ) {
					QueryCache currentQueryCache = (QueryCache) queryCaches.get(cacheRegion);
					if ( currentQueryCache != null ) {
						currentQueryCache.clear();
					}
				}
			}
		}
	}

	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
		FilterDefinition def = ( FilterDefinition ) filters.get( filterName );
		if ( def == null ) {
			throw new HibernateException( "No such filter configured [" + filterName + "]" );
		}
		return def;
	}

	public Set getDefinedFilterNames() {
		return filters.keySet();
	}

	public BatcherFactory getBatcherFactory() {
		return settings.getBatcherFactory();
	}

	public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
		return (IdentifierGenerator) identifierGenerators.get(rootEntityName);
	}

	private CurrentSessionContext buildCurrentSessionContext() {
		String impl = properties.getProperty( Environment.CURRENT_SESSION_CONTEXT_CLASS );
		// for backward-compatability
		if ( impl == null && transactionManager != null ) {
			impl = "jta";
		}

		if ( impl == null ) {
			return null;
		}
		else if ( "jta".equals( impl ) ) {
			if ( settings.getTransactionFactory().areCallbacksLocalToHibernateTransactions() ) {
				log.warn( "JTASessionContext being used with JDBCTransactionFactory; auto-flush will not operate correctly with getCurrentSession()" );
			}
			return new JTASessionContext( this );
		}
		else if ( "thread".equals( impl ) ) {
			return new ThreadLocalSessionContext( this );
		}
		else if ( "managed".equals( impl ) ) {
			return new ManagedSessionContext( this );
		}
		else {
			try {
				Class implClass = ReflectHelper.classForName( impl );
				return ( CurrentSessionContext ) implClass
						.getConstructor( new Class[] { SessionFactoryImplementor.class } )
						.newInstance( new Object[] { this } );
			}
			catch( Throwable t ) {
				log.error( "Unable to construct current session context [" + impl + "]", t );
				return null;
			}
		}
	}

	public EventListeners getEventListeners()
	{
		return eventListeners;
	}

	public EntityNotFoundDelegate getEntityNotFoundDelegate() {
		return entityNotFoundDelegate;
	}

	/**
	 * Custom serialization hook used during Session serialization.
	 *
	 * @param oos The stream to which to write the factory
	 * @throws IOException
	 */
	void serialize(ObjectOutputStream oos) throws IOException {
		oos.writeUTF( uuid );
		oos.writeBoolean( name != null );
		if ( name != null ) {
			oos.writeUTF( name );
		}
	}

	/**
	 * Custom deserialization hook used during Session deserialization.
	 *
	 * @param ois The stream from which to "read" the factory
	 * @throws IOException
	 */
	static SessionFactoryImpl deserialize(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		String uuid = ois.readUTF();
		boolean isNamed = ois.readBoolean();
		String name = null;
		if ( isNamed ) {
			name = ois.readUTF();
		}
		Object result = SessionFactoryObjectFactory.getInstance( uuid );
		if ( result == null ) {
			log.trace( "could not locate session factory by uuid [" + uuid + "] during session deserialization; trying name" );
			if ( isNamed ) {
				result = SessionFactoryObjectFactory.getNamedInstance( name );
			}
			if ( result == null ) {
				throw new InvalidObjectException( "could not resolve session factory during session deserialization [uuid=" + uuid + ", name=" + name + "]" );
			}
		}
		return ( SessionFactoryImpl ) result;
	}

	public SQLFunctionRegistry getSqlFunctionRegistry() {
		return sqlFunctionRegistry;
	}
}
