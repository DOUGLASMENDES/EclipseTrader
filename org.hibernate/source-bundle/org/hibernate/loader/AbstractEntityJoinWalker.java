//$Id: AbstractEntityJoinWalker.java 14209 2007-11-29 01:36:04Z gbadner $
package org.hibernate.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.engine.CascadeStyle;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.Select;
import org.hibernate.type.AssociationType;
import org.hibernate.util.CollectionHelper;

/**
 * Abstract walker for walkers which begin at an entity (criteria
 * queries and entity loaders).
 *
 * @author Gavin King
 */
public abstract class AbstractEntityJoinWalker extends JoinWalker {

	private final OuterJoinLoadable persister;
	private final String alias;

	public AbstractEntityJoinWalker(OuterJoinLoadable persister, SessionFactoryImplementor factory, Map enabledFilters) {
		this( persister, factory, enabledFilters, null );
	}

	public AbstractEntityJoinWalker(OuterJoinLoadable persister, SessionFactoryImplementor factory, Map enabledFilters, String alias) {
		super( factory, enabledFilters );
		this.persister = persister;
		this.alias = ( alias == null ) ? generateRootAlias( persister.getEntityName() ) : alias;
	}

	protected final void initAll(
		final String whereString,
		final String orderByString,
		final LockMode lockMode)
	throws MappingException {
		walkEntityTree( persister, getAlias() );
		List allAssociations = new ArrayList();
		allAssociations.addAll(associations);
		allAssociations.add( new OuterJoinableAssociation(
				persister.getEntityType(),
				null,
				null,
				alias,
				JoinFragment.LEFT_OUTER_JOIN,
				getFactory(),
				CollectionHelper.EMPTY_MAP
			) );

		initPersisters(allAssociations, lockMode);
		initStatementString( whereString, orderByString, lockMode);
	}

	protected final void initProjection(
		final String projectionString,
		final String whereString,
		final String orderByString,
		final String groupByString,
		final LockMode lockMode)
	throws MappingException {
		walkEntityTree( persister, getAlias() );
		persisters = new Loadable[0];
		initStatementString(projectionString, whereString, orderByString, groupByString, lockMode);
	}

	private void initStatementString(
		final String condition,
		final String orderBy,
		final LockMode lockMode)
	throws MappingException {
		initStatementString(null, condition, orderBy, "", lockMode);
	}

	private void initStatementString(
			final String projection,
			final String condition,
			final String orderBy,
			final String groupBy,
			final LockMode lockMode) throws MappingException {

		final int joins = countEntityPersisters( associations );
		suffixes = BasicLoader.generateSuffixes( joins + 1 );

		JoinFragment ojf = mergeOuterJoins( associations );

		Select select = new Select( getDialect() )
				.setLockMode( lockMode )
				.setSelectClause(
						projection == null ?
								persister.selectFragment( alias, suffixes[joins] ) + selectString( associations ) :
								projection
				)
				.setFromClause(
						getDialect().appendLockHint( lockMode, persister.fromTableFragment( alias ) ) +
								persister.fromJoinFragment( alias, true, true )
				)
				.setWhereClause( condition )
				.setOuterJoins(
						ojf.toFromFragmentString(),
						ojf.toWhereFragmentString() + getWhereFragment()
				)
				.setOrderByClause( orderBy( associations, orderBy ) )
				.setGroupByClause( groupBy );

		if ( getFactory().getSettings().isCommentsEnabled() ) {
			select.setComment( getComment() );
		}
		sql = select.toStatementString();
	}

	protected String getWhereFragment() throws MappingException {
		// here we do not bother with the discriminator.
		return persister.whereJoinFragment(alias, true, true);
	}

	/**
	 * The superclass deliberately excludes collections
	 */
	protected boolean isJoinedFetchEnabled(AssociationType type, FetchMode config, CascadeStyle cascadeStyle) {
		return isJoinedFetchEnabledInMapping(config, type);
	}

	public abstract String getComment();

	protected final Loadable getPersister() {
		return persister;
	}

	protected final String getAlias() {
		return alias;
	}

	public String toString() {
		return getClass().getName() + '(' + getPersister().getEntityName() + ')';
	}
}
