//$Id: BasicCollectionPersister.java 10040 2006-06-22 19:51:43Z steve.ebersole@jboss.com $
package org.hibernate.persister.collection;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.jdbc.Expectations;
import org.hibernate.jdbc.Expectation;
import org.hibernate.type.AssociationType;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.cache.CacheConcurrencyStrategy;
import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.SubselectFetch;
import org.hibernate.exception.JDBCExceptionHelper;
import org.hibernate.loader.collection.BatchingCollectionInitializer;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.loader.collection.SubselectCollectionLoader;
import org.hibernate.mapping.Collection;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.Delete;
import org.hibernate.sql.Insert;
import org.hibernate.sql.Update;
import org.hibernate.sql.SelectFragment;
import org.hibernate.util.ArrayHelper;

/**
 * Collection persister for collections of values and many-to-many associations.
 *
 * @author Gavin King
 */
public class BasicCollectionPersister extends AbstractCollectionPersister {

	public boolean isCascadeDeleteEnabled() {
		return false;
	}

	public BasicCollectionPersister(Collection collection,
									CacheConcurrencyStrategy cache,
									Configuration cfg,
									SessionFactoryImplementor factory)
			throws MappingException, CacheException {
		super( collection, cache, cfg, factory );
	}

	/**
	 * Generate the SQL DELETE that deletes all rows
	 */
	protected String generateDeleteString() {
		
		Delete delete = new Delete()
				.setTableName( qualifiedTableName )
				.setPrimaryKeyColumnNames( keyColumnNames );
		
		if ( hasWhere ) delete.setWhere( sqlWhereString );
		
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			delete.setComment( "delete collection " + getRole() );
		}
		
		return delete.toStatementString();
	}

	/**
	 * Generate the SQL INSERT that creates a new row
	 */
	protected String generateInsertRowString() {
		
		Insert insert = new Insert( getDialect() )
				.setTableName( qualifiedTableName )
				.addColumns( keyColumnNames );
		
		if ( hasIdentifier) insert.addColumn( identifierColumnName );
		
		if ( hasIndex /*&& !indexIsFormula*/ ) {
			insert.addColumns( indexColumnNames, indexColumnIsSettable );
		}
		
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			insert.setComment( "insert collection row " + getRole() );
		}
		
		//if ( !elementIsFormula ) {
			insert.addColumns( elementColumnNames, elementColumnIsSettable );
		//}
		
		return insert.toStatementString();
	}

	/**
	 * Generate the SQL UPDATE that updates a row
	 */
	protected String generateUpdateRowString() {
		
		Update update = new Update( getDialect() )
			.setTableName( qualifiedTableName );
		
		//if ( !elementIsFormula ) {
			update.addColumns( elementColumnNames, elementColumnIsSettable );
		//}
		
		if ( hasIdentifier ) {
			update.setPrimaryKeyColumnNames( new String[]{ identifierColumnName } );
		}
		else if ( hasIndex && !indexContainsFormula ) {
			update.setPrimaryKeyColumnNames( ArrayHelper.join( keyColumnNames, indexColumnNames ) );
		}
		else {
			update.setPrimaryKeyColumnNames( ArrayHelper.join( keyColumnNames, elementColumnNames, elementColumnIsInPrimaryKey ) );
		}
		
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			update.setComment( "update collection row " + getRole() );
		}
		
		return update.toStatementString();
	}

	/**
	 * Generate the SQL DELETE that deletes a particular row
	 */
	protected String generateDeleteRowString() {
		
		Delete delete = new Delete()
			.setTableName( qualifiedTableName );
		
		if ( hasIdentifier ) {
			delete.setPrimaryKeyColumnNames( new String[]{ identifierColumnName } );
		}
		else if ( hasIndex && !indexContainsFormula ) {
			delete.setPrimaryKeyColumnNames( ArrayHelper.join( keyColumnNames, indexColumnNames ) );
		}
		else {
			delete.setPrimaryKeyColumnNames( ArrayHelper.join( keyColumnNames, elementColumnNames, elementColumnIsInPrimaryKey ) );
		}
		
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			delete.setComment( "delete collection row " + getRole() );
		}
		
		return delete.toStatementString();
	}

	public boolean consumesEntityAlias() {
		return false;
	}

	public boolean consumesCollectionAlias() {
//		return !isOneToMany();
		return true;
	}

	public boolean isOneToMany() {
		return false;
	}

	public boolean isManyToMany() {
		return elementType.isEntityType(); //instanceof AssociationType;
	}

	protected int doUpdateRows(Serializable id, PersistentCollection collection, SessionImplementor session)
			throws HibernateException {
		
		if ( ArrayHelper.isAllFalse(elementColumnIsSettable) ) return 0;

		try {
			PreparedStatement st = null;
			Expectation expectation = Expectations.appropriateExpectation( getUpdateCheckStyle() );
			boolean callable = isUpdateCallable();
			boolean useBatch = expectation.canBeBatched();
			Iterator entries = collection.entries( this );
			String sql = getSQLUpdateRowString();
			int i = 0;
			int count = 0;
			while ( entries.hasNext() ) {
				Object entry = entries.next();
				if ( collection.needsUpdating( entry, i, elementType ) ) {
					int offset = 1;

					if ( useBatch ) {
						if ( st == null ) {
							if ( callable ) {
								st = session.getBatcher().prepareBatchCallableStatement( sql );
							}
							else {
								st = session.getBatcher().prepareBatchStatement( sql );
							}
						}
					}
					else {
						if ( callable ) {
							st = session.getBatcher().prepareCallableStatement( sql );
						}
						else {
							st = session.getBatcher().prepareStatement( sql );
						}
					}

					try {
						offset+= expectation.prepare( st );
						int loc = writeElement( st, collection.getElement( entry ), offset, session );
						if ( hasIdentifier ) {
							writeIdentifier( st, collection.getIdentifier( entry, i ), loc, session );
						}
						else {
							loc = writeKey( st, id, loc, session );
							if ( hasIndex && !indexContainsFormula ) {
								writeIndexToWhere( st, collection.getIndex( entry, i, this ), loc, session );
							}
							else {
								writeElementToWhere( st, collection.getSnapshotElement( entry, i ), loc, session );
							}
						}

						if ( useBatch ) {
							session.getBatcher().addToBatch( expectation );
						}
						else {
							expectation.verifyOutcome( st.executeUpdate(), st, -1 );
						}
					}
					catch ( SQLException sqle ) {
						if ( useBatch ) {
							session.getBatcher().abortBatch( sqle );
						}
						throw sqle;
					}
					finally {
						if ( !useBatch ) {
							session.getBatcher().closeStatement( st );
						}
					}
					count++;
				}
				i++;
			}
			return count;
		}
		catch ( SQLException sqle ) {
			throw JDBCExceptionHelper.convert(
					getSQLExceptionConverter(),
					sqle,
					"could not update collection rows: " + MessageHelper.collectionInfoString( this, id, getFactory() ),
					getSQLUpdateRowString()
				);
		}
	}

	public String selectFragment(
	        Joinable rhs,
	        String rhsAlias,
	        String lhsAlias,
	        String entitySuffix,
	        String collectionSuffix,
	        boolean includeCollectionColumns) {
		// we need to determine the best way to know that two joinables
		// represent a single many-to-many...
		if ( rhs != null && isManyToMany() && !rhs.isCollection() ) {
			AssociationType elementType = ( ( AssociationType ) getElementType() );
			if ( rhs.equals( elementType.getAssociatedJoinable( getFactory() ) ) ) {
				return manyToManySelectFragment( rhs, rhsAlias, lhsAlias, collectionSuffix );
			}
		}
		return includeCollectionColumns ? selectFragment( lhsAlias, collectionSuffix ) : "";
	}

	private String manyToManySelectFragment(
	        Joinable rhs,
	        String rhsAlias,
	        String lhsAlias,
	        String collectionSuffix) {
		SelectFragment frag = generateSelectFragment( lhsAlias, collectionSuffix );

		String[] elementColumnNames = rhs.getKeyColumnNames();
		frag.addColumns( rhsAlias, elementColumnNames, elementColumnAliases );
		appendIndexColumns( frag, lhsAlias );
		appendIdentifierColumns( frag, lhsAlias );

		return frag.toFragmentString()
				.substring( 2 ); //strip leading ','
	}

	/**
	 * Create the <tt>CollectionLoader</tt>
	 *
	 * @see org.hibernate.loader.collection.BasicCollectionLoader
	 */
	protected CollectionInitializer createCollectionInitializer(java.util.Map enabledFilters)
			throws MappingException {
		return BatchingCollectionInitializer.createBatchingCollectionInitializer( this, batchSize, getFactory(), enabledFilters );
	}

	public String fromJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
		return "";
	}

	public String whereJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
		return "";
	}

	protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
		return new SubselectCollectionLoader( 
				this,
				subselect.toSubselectString( getCollectionType().getLHSPropertyName() ),
				subselect.getResult(),
				subselect.getQueryParameters(),
				subselect.getNamedParameterLocMap(),
				session.getFactory(),
				session.getEnabledFilters() 
			);
	}

}
