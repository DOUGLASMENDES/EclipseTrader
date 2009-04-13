//$Id: CriteriaJoinWalker.java 14209 2007-11-29 01:36:04Z gbadner $
package org.hibernate.loader.criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.engine.CascadeStyle;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.loader.AbstractEntityJoinWalker;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.util.ArrayHelper;

/**
 * A <tt>JoinWalker</tt> for <tt>Criteria</tt> queries.
 *
 * @see CriteriaLoader
 * @author Gavin King
 */
public class CriteriaJoinWalker extends AbstractEntityJoinWalker {

	//TODO: add a CriteriaImplementor interface
	//      this class depends directly upon CriteriaImpl in the impl package...

	private final CriteriaQueryTranslator translator;
	private final Set querySpaces;
	private final Type[] resultTypes;
	//the user visible aliases, which are unknown to the superclass,
	//these are not the actual "physical" SQL aliases
	private final String[] userAliases;
	private final List userAliasList = new ArrayList();

	public Type[] getResultTypes() {
		return resultTypes;
	}

	public String[] getUserAliases() {
		return userAliases;
	}

	public CriteriaJoinWalker(
			final OuterJoinLoadable persister, 
			final CriteriaQueryTranslator translator,
			final SessionFactoryImplementor factory, 
			final CriteriaImpl criteria, 
			final String rootEntityName,
			final Map enabledFilters) {
		this(persister, translator, factory, criteria, rootEntityName, enabledFilters, null);
	}

	public CriteriaJoinWalker(
			final OuterJoinLoadable persister,
			final CriteriaQueryTranslator translator,
			final SessionFactoryImplementor factory,
			final CriteriaImpl criteria,
			final String rootEntityName,
			final Map enabledFilters,
			final String alias) {
		super(persister, factory, enabledFilters, alias);

		this.translator = translator;

		querySpaces = translator.getQuerySpaces();

		if ( translator.hasProjection() ) {
			resultTypes = translator.getProjectedTypes();
			
			initProjection( 
					translator.getSelect(), 
					translator.getWhereCondition(), 
					translator.getOrderBy(),
					translator.getGroupBy(),
					LockMode.NONE 
				);
		}
		else {
			resultTypes = new Type[] { TypeFactory.manyToOne( persister.getEntityName() ) };

			initAll( translator.getWhereCondition(), translator.getOrderBy(), LockMode.NONE );
		}
		
		userAliasList.add( criteria.getAlias() ); //root entity comes *last*
		userAliases = ArrayHelper.toStringArray(userAliasList);

	}

	protected int getJoinType(
			AssociationType type, 
			FetchMode config, 
			String path,
			String lhsTable,
			String[] lhsColumns,
			boolean nullable,
			int currentDepth, CascadeStyle cascadeStyle)
	throws MappingException {

		if ( translator.isJoin(path) ) {
			return translator.getJoinType( path );
		}
		else {
			if ( translator.hasProjection() ) {
				return -1;
			}
			else {
				FetchMode fetchMode = translator.getRootCriteria()
					.getFetchMode(path);
				if ( isDefaultFetchMode(fetchMode) ) {
					return super.getJoinType(
							type, 
							config, 
							path, 
							lhsTable, 
							lhsColumns, 
							nullable,
							currentDepth, cascadeStyle
						);
				}
				else {
					if ( fetchMode==FetchMode.JOIN ) {
						isDuplicateAssociation(lhsTable, lhsColumns, type); //deliberately ignore return value!
						return getJoinType(nullable, currentDepth);
					}
					else {
						return -1;
					}
				}
			}
		}
	}
	
	private static boolean isDefaultFetchMode(FetchMode fetchMode) {
		return fetchMode==null || fetchMode==FetchMode.DEFAULT;
	}

	/**
	 * Use the discriminator, to narrow the select to instances
	 * of the queried subclass, also applying any filters.
	 */
	protected String getWhereFragment() throws MappingException {
		return super.getWhereFragment() +
			( (Queryable) getPersister() ).filterFragment( getAlias(), getEnabledFilters() );
	}
	
	protected String generateTableAlias(int n, String path, Joinable joinable) {
		if ( joinable.consumesEntityAlias() ) {
			final Criteria subcriteria = translator.getCriteria(path);
			String sqlAlias = subcriteria==null ? null : translator.getSQLAlias(subcriteria);
			if (sqlAlias!=null) {
				userAliasList.add( subcriteria.getAlias() ); //alias may be null
				return sqlAlias; //EARLY EXIT
			}
			else {
				userAliasList.add(null);
			}
		}
		return super.generateTableAlias( n + translator.getSQLAliasCount(), path, joinable );
	}

	protected String generateRootAlias(String tableName) {
		return CriteriaQueryTranslator.ROOT_SQL_ALIAS;
	}

	public Set getQuerySpaces() {
		return querySpaces;
	}
	
	public String getComment() {
		return "criteria query";
	}

}
