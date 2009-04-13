//$Id: SessionFactoryImplementor.java 9908 2006-05-08 20:59:20Z max.andersen@jboss.com $
package org.hibernate.engine;

import java.util.Map;
import java.util.Set;
import java.sql.Connection;

import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.engine.query.QueryPlanCache;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.cache.Cache;
import org.hibernate.cache.QueryCache;
import org.hibernate.cache.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.exception.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.stat.StatisticsImplementor;
import org.hibernate.type.Type;

/**
 * Defines the internal contract between the <tt>SessionFactory</tt> and other parts of
 * Hibernate such as implementors of <tt>Type</tt>.
 *
 * @see org.hibernate.SessionFactory
 * @see org.hibernate.impl.SessionFactoryImpl
 * @author Gavin King
 */
public interface SessionFactoryImplementor extends Mapping, SessionFactory {

	/**
	 * Get the persister for the named entity
	 */
	public EntityPersister getEntityPersister(String entityName) throws MappingException;
	/**
	 * Get the persister object for a collection role
	 */
	public CollectionPersister getCollectionPersister(String role) throws MappingException;

	/**
	 * Get the SQL <tt>Dialect</tt>
	 */
	public Dialect getDialect();
	
	public Interceptor getInterceptor();

	public QueryPlanCache getQueryPlanCache();

	/**
	 * Get the return types of a query
	 */
	public Type[] getReturnTypes(String queryString) throws HibernateException;

	/**
	 * Get the return aliases of a query
	 */
	public String[] getReturnAliases(String queryString) throws HibernateException;

	/**
	 * Get the connection provider
	 */
	public ConnectionProvider getConnectionProvider();
	/**
	 * Get the names of all persistent classes that implement/extend the given interface/class
	 */
	public String[] getImplementors(String className) throws MappingException;
	/**
	 * Get a class name, using query language imports
	 */
	public String getImportedClassName(String name);


	/**
	 * Get the JTA transaction manager
	 */
	public TransactionManager getTransactionManager();


	/**
	 * Get the default query cache
	 */
	public QueryCache getQueryCache();
	/**
	 * Get a particular named query cache, or the default cache
	 * @param regionName the name of the cache region, or null for the default query cache
	 * @return the existing cache, or a newly created cache if none by that region name
	 */
	public QueryCache getQueryCache(String regionName) throws HibernateException;
	
	/**
	 * Get the cache of table update timestamps
	 */
	public UpdateTimestampsCache getUpdateTimestampsCache();
	/**
	 * Statistics SPI
	 */
	public StatisticsImplementor getStatisticsImplementor();
	
	public NamedQueryDefinition getNamedQuery(String queryName);
	public NamedSQLQueryDefinition getNamedSQLQuery(String queryName);
	public ResultSetMappingDefinition getResultSetMapping(String name);

	/**
	 * Get the identifier generator for the hierarchy
	 */
	public IdentifierGenerator getIdentifierGenerator(String rootEntityName);
	
	/**
	 * Get a named second-level cache region
	 */
	public Cache getSecondLevelCacheRegion(String regionName);
	
	public Map getAllSecondLevelCacheRegions();
	
	/**
	 * Retrieves the SQLExceptionConverter in effect for this SessionFactory.
	 *
	 * @return The SQLExceptionConverter for this SessionFactory.
	 */
	public SQLExceptionConverter getSQLExceptionConverter();

	public Settings getSettings();

	/**
	 * Get a nontransactional "current" session for Hibernate EntityManager
	 */
	public org.hibernate.classic.Session openTemporarySession() throws HibernateException;

	/**
	 * Open a session conforming to the given parameters.  Used mainly by
	 * {@link org.hibernate.context.JTASessionContext} for current session processing.
	 *
	 * @param connection The external jdbc connection to use, if one (i.e., optional).
	 * @param flushBeforeCompletionEnabled Should the session be auto-flushed
	 * prior to transaction completion?
	 * @param autoCloseSessionEnabled Should the session be auto-closed after
	 * transaction completion?
	 * @param connectionReleaseMode The release mode for managed jdbc connections.
	 * @return An appropriate session.
	 * @throws HibernateException
	 */
	public org.hibernate.classic.Session openSession(
			final Connection connection,
			final boolean flushBeforeCompletionEnabled,
			final boolean autoCloseSessionEnabled,
			final ConnectionReleaseMode connectionReleaseMode) throws HibernateException;

	/**
	 * Retrieves a set of all the collection roles in which the given entity
	 * is a participant, as either an index or an element.
	 *
	 * @param entityName The entity name for which to get the collection roles.
	 * @return set of all the collection roles in which the given entityName participates.
	 */
	public Set getCollectionRolesByEntityParticipant(String entityName);

	public EntityNotFoundDelegate getEntityNotFoundDelegate();

	public SQLFunctionRegistry getSqlFunctionRegistry();
		
}
