// $Id: AbstractStatementExecutor.java 11287 2007-03-15 11:38:25Z steve.ebersole@jboss.com $
package org.hibernate.hql.ast.exec;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Statement;

import org.hibernate.HibernateException;
import org.hibernate.action.BulkOperationCleanupAction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.transaction.Isolater;
import org.hibernate.engine.transaction.IsolatedWork;
import org.hibernate.event.EventSource;
import org.hibernate.hql.ast.HqlSqlWalker;
import org.hibernate.hql.ast.SqlGenerator;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.InsertSelect;
import org.hibernate.sql.Select;
import org.hibernate.sql.SelectFragment;
import org.hibernate.util.StringHelper;

import antlr.RecognitionException;
import antlr.collections.AST;

import org.apache.commons.logging.Log;

/**
 * Implementation of AbstractStatementExecutor.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractStatementExecutor implements StatementExecutor {

	private final Log log;
	private final HqlSqlWalker walker;

	public AbstractStatementExecutor(HqlSqlWalker walker, Log log) {
		this.walker = walker;
		this.log = log;
	}

	protected HqlSqlWalker getWalker() {
		return walker;
	}

	protected SessionFactoryImplementor getFactory() {
		return walker.getSessionFactoryHelper().getFactory();
	}

	protected abstract Queryable[] getAffectedQueryables();

	protected String generateIdInsertSelect(Queryable persister, String tableAlias, AST whereClause) {
		Select select = new Select( getFactory().getDialect() );
		SelectFragment selectFragment = new SelectFragment()
				.addColumns( tableAlias, persister.getIdentifierColumnNames(), persister.getIdentifierColumnNames() );
		select.setSelectClause( selectFragment.toFragmentString().substring( 2 ) );

		String rootTableName = persister.getTableName();
		String fromJoinFragment = persister.fromJoinFragment( tableAlias, true, false );
		String whereJoinFragment = persister.whereJoinFragment( tableAlias, true, false );

		select.setFromClause( rootTableName + ' ' + tableAlias + fromJoinFragment );

		if ( whereJoinFragment == null ) {
			whereJoinFragment = "";
		}
		else {
			whereJoinFragment = whereJoinFragment.trim();
			if ( whereJoinFragment.startsWith( "and" ) ) {
				whereJoinFragment = whereJoinFragment.substring( 4 );
			}
		}

		String userWhereClause = "";
		if ( whereClause.getNumberOfChildren() != 0 ) {
			// If a where clause was specified in the update/delete query, use it to limit the
			// returned ids here...
			try {
				SqlGenerator sqlGenerator = new SqlGenerator( getFactory() );
				sqlGenerator.whereClause( whereClause );
				userWhereClause = sqlGenerator.getSQL().substring( 7 );  // strip the " where "
			}
			catch ( RecognitionException e ) {
				throw new HibernateException( "Unable to generate id select for DML operation", e );
			}
			if ( whereJoinFragment.length() > 0 ) {
				whereJoinFragment += " and ";
			}
		}

		select.setWhereClause( whereJoinFragment + userWhereClause );

		InsertSelect insert = new InsertSelect( getFactory().getDialect() );
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			insert.setComment( "insert-select for " + persister.getEntityName() + " ids" );
		}
		insert.setTableName( persister.getTemporaryIdTableName() );
		insert.setSelect( select );
		return insert.toStatementString();
	}

	protected String generateIdSubselect(Queryable persister) {
		return "select " + StringHelper.join( ", ", persister.getIdentifierColumnNames() ) +
			        " from " + persister.getTemporaryIdTableName();
	}

	protected void createTemporaryTableIfNecessary(final Queryable persister, final SessionImplementor session) {
		// Don't really know all the codes required to adequately decipher returned jdbc exceptions here.
		// simply allow the failure to be eaten and the subsequent insert-selects/deletes should fail
		IsolatedWork work = new IsolatedWork() {
			public void doWork(Connection connection) throws HibernateException {
				Statement stmnt = null;
				try {
					stmnt = connection.createStatement();
					stmnt.executeUpdate( persister.getTemporaryIdTableDDL() );
				}
				catch( Throwable t ) {
					log.debug( "unable to create temporary id table [" + t.getMessage() + "]" );
				}
				finally {
					if ( stmnt != null ) {
						try {
							stmnt.close();
						}
						catch( Throwable ignore ) {
							// ignore
						}
					}
				}
			}
		};
		if ( shouldIsolateTemporaryTableDDL() ) {
			if ( getFactory().getSettings().isDataDefinitionInTransactionSupported() ) {
				Isolater.doIsolatedWork( work, session );
			}
			else {
				Isolater.doNonTransactedWork( work, session );
			}
		}
		else {
			work.doWork( session.getJDBCContext().getConnectionManager().getConnection() );
			session.getJDBCContext().getConnectionManager().afterStatement();
		}
	}

	protected void dropTemporaryTableIfNecessary(final Queryable persister, final SessionImplementor session) {
		if ( getFactory().getDialect().dropTemporaryTableAfterUse() ) {
			IsolatedWork work = new IsolatedWork() {
				public void doWork(Connection connection) throws HibernateException {
					Statement stmnt = null;
					try {
						stmnt = connection.createStatement();
						stmnt.executeUpdate( "drop table " + persister.getTemporaryIdTableName() );
					}
					catch( Throwable t ) {
						log.warn( "unable to drop temporary id table after use [" + t.getMessage() + "]" );
					}
					finally {
						if ( stmnt != null ) {
							try {
								stmnt.close();
							}
							catch( Throwable ignore ) {
								// ignore
							}
						}
					}
				}
			};

			if ( shouldIsolateTemporaryTableDDL() ) {
				if ( getFactory().getSettings().isDataDefinitionInTransactionSupported() ) {
					Isolater.doIsolatedWork( work, session );
				}
				else {
					Isolater.doNonTransactedWork( work, session );
				}
			}
			else {
				work.doWork( session.getJDBCContext().getConnectionManager().getConnection() );
				session.getJDBCContext().getConnectionManager().afterStatement();
			}
		}
		else {
			// at the very least cleanup the data :)
			PreparedStatement ps = null;
			try {
				ps = session.getBatcher().prepareStatement( "delete from " + persister.getTemporaryIdTableName() );
				ps.executeUpdate();
			}
			catch( Throwable t ) {
				log.warn( "unable to cleanup temporary id table after use [" + t + "]" );
			}
			finally {
				if ( ps != null ) {
					try {
						session.getBatcher().closeStatement( ps );
					}
					catch( Throwable ignore ) {
						// ignore
					}
				}
			}
		}
	}

	protected void coordinateSharedCacheCleanup(SessionImplementor session) {
		BulkOperationCleanupAction action = new BulkOperationCleanupAction( session, getAffectedQueryables() );

		action.init();

		if ( session.isEventSource() ) {
			( ( EventSource ) session ).getActionQueue().addAction( action );
		}
	}

	protected boolean shouldIsolateTemporaryTableDDL() {
		Boolean dialectVote = getFactory().getDialect().performTemporaryTableDDLInIsolation();
		if ( dialectVote != null ) {
			return dialectVote.booleanValue();
		}
		else {
			return getFactory().getSettings().isDataDefinitionImplicitCommit();
		}
	}
}
