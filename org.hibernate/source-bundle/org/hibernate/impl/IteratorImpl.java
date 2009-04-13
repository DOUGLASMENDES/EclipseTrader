//$Id: IteratorImpl.java 13961 2007-08-29 23:00:10Z gbadner $
package org.hibernate.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.HibernateIterator;
import org.hibernate.event.EventSource;
import org.hibernate.exception.JDBCExceptionHelper;
import org.hibernate.hql.HolderInstantiator;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * An implementation of <tt>java.util.Iterator</tt> that is
 * returned by <tt>iterate()</tt> query execution methods.
 * @author Gavin King
 */
public final class IteratorImpl implements HibernateIterator {

	private static final Log log = LogFactory.getLog(IteratorImpl.class);

	private ResultSet rs;
	private final EventSource session;
	private final Type[] types;
	private final boolean single;
	private Object currentResult;
	private boolean hasNext;
	private final String[][] names;
	private PreparedStatement ps;
	private HolderInstantiator holderInstantiator;

	public IteratorImpl(
	        ResultSet rs,
	        PreparedStatement ps,
	        EventSource sess,
	        Type[] types,
	        String[][] columnNames,
	        HolderInstantiator holderInstantiator)
	throws HibernateException, SQLException {

		this.rs=rs;
		this.ps=ps;
		this.session = sess;
		this.types = types;
		this.names = columnNames;
		this.holderInstantiator = holderInstantiator;

		single = types.length==1;

		postNext();
	}

	public void close() throws JDBCException {
		if (ps!=null) {
			try {
				log.debug("closing iterator");
				session.getBatcher().closeQueryStatement(ps, rs);
				ps = null;
				rs = null;
				hasNext = false;
			}
			catch (SQLException e) {
				log.info( "Unable to close iterator", e );
				throw JDBCExceptionHelper.convert(
				        session.getFactory().getSQLExceptionConverter(),
				        e,
				        "Unable to close iterator"
					);
			}
			finally {
				try {
					session.getPersistenceContext().getLoadContexts().cleanup( rs );
				}
				catch( Throwable ignore ) {
					// ignore this error for now
					log.trace( "exception trying to cleanup load context : " + ignore.getMessage() );
				}
			}
		}
	}

	private void postNext() throws SQLException {
		log.debug("attempting to retrieve next results");
		this.hasNext = rs.next();
		if (!hasNext) {
			log.debug("exhausted results");
			close();
		}
		else {
			log.debug("retrieved next results");
		}
	}

	public boolean hasNext() {
		return hasNext;
	}

	public Object next() throws HibernateException {
		if ( !hasNext ) throw new NoSuchElementException("No more results");
		try {
			boolean isHolder = holderInstantiator.isRequired();

			log.debug("assembling results");
			if ( single && !isHolder ) {
				currentResult = types[0].nullSafeGet( rs, names[0], session, null );
			}
			else {
				Object[] currentResults = new Object[types.length];
				for (int i=0; i<types.length; i++) {
					currentResults[i] = types[i].nullSafeGet( rs, names[i], session, null );
				}

				if (isHolder) {
					currentResult = holderInstantiator.instantiate(currentResults);
				}
				else {
					currentResult = currentResults;
				}
			}

			postNext();
			log.debug("returning current results");
			return currentResult;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					session.getFactory().getSQLExceptionConverter(),
					sqle,
					"could not get next iterator result"
				);
		}
	}

	public void remove() {
		if (!single) {
			throw new UnsupportedOperationException("Not a single column hibernate query result set");
		}
		if (currentResult==null) {
			throw new IllegalStateException("Called Iterator.remove() before next()");
		}
		if ( !( types[0] instanceof EntityType ) ) {
			throw new UnsupportedOperationException("Not an entity");
		}
		
		session.delete( 
				( (EntityType) types[0] ).getAssociatedEntityName(), 
				currentResult,
				false,
		        null
			);
	}

}
