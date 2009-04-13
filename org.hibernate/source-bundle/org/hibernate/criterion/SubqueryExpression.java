//$Id: SubqueryExpression.java 14209 2007-11-29 01:36:04Z gbadner $
package org.hibernate.criterion;

import java.util.HashMap;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.TypedValue;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

/**
 * @author Gavin King
 */
public abstract class SubqueryExpression implements Criterion {
	
	private CriteriaImpl criteriaImpl;
	private String quantifier;
	private String op;
	private QueryParameters params;
	private Type[] types;
	private CriteriaQueryTranslator innerQuery;

	protected Type[] getTypes() {
		return types;
	}
	
	protected SubqueryExpression(String op, String quantifier, DetachedCriteria dc) {
		this.criteriaImpl = dc.getCriteriaImpl();
		this.quantifier = quantifier;
		this.op = op;
	}
	
	protected abstract String toLeftSqlString(Criteria criteria, CriteriaQuery outerQuery);

	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
	throws HibernateException {

		final SessionFactoryImplementor factory = criteriaQuery.getFactory();
		final OuterJoinLoadable persister = (OuterJoinLoadable) factory.getEntityPersister( criteriaImpl.getEntityOrClassName() );

		createAndSetInnerQuery( criteriaQuery, factory );
		
		CriteriaJoinWalker walker = new CriteriaJoinWalker(
				persister,
				innerQuery,
				factory,
				criteriaImpl,
				criteriaImpl.getEntityOrClassName(),
				new HashMap(),
				innerQuery.getRootSQLALias());

		String sql = walker.getSQLString();

		final StringBuffer buf = new StringBuffer()
			.append( toLeftSqlString(criteria, criteriaQuery) );
		if (op!=null) buf.append(' ').append(op).append(' ');
		if (quantifier!=null) buf.append(quantifier).append(' ');
		return buf.append('(').append(sql).append(')')
			.toString();
	}

	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) 
	throws HibernateException {
		//the following two lines were added to ensure that this.params is not null, which
		//can happen with two-deep nested subqueries
		SessionFactoryImplementor factory = criteriaQuery.getFactory();
		createAndSetInnerQuery(criteriaQuery, factory);
		
		Type[] ppTypes = params.getPositionalParameterTypes();
		Object[] ppValues = params.getPositionalParameterValues();
		TypedValue[] tv = new TypedValue[ppTypes.length];
		for ( int i=0; i<ppTypes.length; i++ ) {
			tv[i] = new TypedValue( ppTypes[i], ppValues[i], EntityMode.POJO );
		}
		return tv;
	}

	/**
	 * Creates the inner query used to extract some useful information about
	 * types, since it is needed in both methods.
	 * @param criteriaQuery
	 * @param factory
	 */
	private void createAndSetInnerQuery(CriteriaQuery criteriaQuery, final SessionFactoryImplementor factory) {
		if ( innerQuery == null ) {
			//with two-deep subqueries, the same alias would get generated for
			//both using the old method (criteriaQuery.generateSQLAlias()), so
			//that is now used as a fallback if the main criteria alias isn't set
			String alias;
			if ( this.criteriaImpl.getAlias() == null ) {
				alias = criteriaQuery.generateSQLAlias();
			}
			else {
				alias = this.criteriaImpl.getAlias() + "_";
			}

			innerQuery = new CriteriaQueryTranslator(
					factory,
					criteriaImpl,
					criteriaImpl.getEntityOrClassName(), //implicit polymorphism not supported (would need a union)
					alias,
					criteriaQuery
				);

			params = innerQuery.getQueryParameters();
			types = innerQuery.getProjectedTypes();
		}
	}
}
