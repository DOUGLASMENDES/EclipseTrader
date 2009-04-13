//$Id: QueryCache.java 7743 2005-08-03 22:50:19Z oneovthafew $
package org.hibernate.cache;

import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;

/**
 * Defines the contract for caches capable of storing query results.  These
 * caches should only concern themselves with storing the matching result ids.
 * The transactional semantics are necessarily less strict than the semantics
 * of an item cache.
 * 
 * @author Gavin King
 */
public interface QueryCache {

	public void clear() throws CacheException;
	
	public boolean put(QueryKey key, Type[] returnTypes, List result, boolean isNaturalKeyLookup, SessionImplementor session) throws HibernateException;

	public List get(QueryKey key, Type[] returnTypes, boolean isNaturalKeyLookup, Set spaces, SessionImplementor session) 
	throws HibernateException;

	public void destroy();

	public Cache getCache();

	public String getRegionName();
}
