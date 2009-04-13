//$Id: ManyToOneType.java 10020 2006-06-15 16:38:03Z steve.ebersole@jboss.com $
package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.ForeignKeys;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;

/**
 * A many-to-one association to an entity.
 *
 * @author Gavin King
 */
public class ManyToOneType extends EntityType {
	
	private final boolean ignoreNotFound;

	public ManyToOneType(String className) {
		this( className, false );
	}

	public ManyToOneType(String className, boolean lazy) {
		super( className, null, !lazy, true, false );
		this.ignoreNotFound = false;
	}

	public ManyToOneType(
			String entityName,
			String uniqueKeyPropertyName,
			boolean lazy,
			boolean unwrapProxy,
			boolean isEmbeddedInXML,
			boolean ignoreNotFound) {
		super( entityName, uniqueKeyPropertyName, !lazy, isEmbeddedInXML, unwrapProxy );
		this.ignoreNotFound = ignoreNotFound;
	}

	protected boolean isNullable() {
		return ignoreNotFound;
	}

	public boolean isAlwaysDirtyChecked() {
		// If we have <tt>not-found="ignore"</tt> association mapped to a
		// formula, we always need to dirty check it, so we can update the
		// second-level cache
		return ignoreNotFound;
	}

	public boolean isOneToOne() {
		return false;
	}
	
	public int getColumnSpan(Mapping mapping) throws MappingException {
		// our column span is the number of columns in the PK
		return getIdentifierOrUniqueKeyType( mapping ).getColumnSpan( mapping );
	}

	public int[] sqlTypes(Mapping mapping) throws MappingException {
		return getIdentifierOrUniqueKeyType( mapping ).sqlTypes( mapping );
	}

	public void nullSafeSet(
			PreparedStatement st,
			Object value,
			int index,
			boolean[] settable,
			SessionImplementor session) throws HibernateException, SQLException {
		getIdentifierOrUniqueKeyType( session.getFactory() )
				.nullSafeSet( st, getIdentifier( value, session ), index, settable, session );
	}

	public void nullSafeSet(
			PreparedStatement st,
			Object value,
			int index,
			SessionImplementor session) throws HibernateException, SQLException {
		getIdentifierOrUniqueKeyType( session.getFactory() )
				.nullSafeSet( st, getIdentifier( value, session ), index, session );
	}

	public ForeignKeyDirection getForeignKeyDirection() {
		return ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT;
	}

	public Object hydrate(
			ResultSet rs,
			String[] names,
			SessionImplementor session,
			Object owner) throws HibernateException, SQLException {
		// return the (fully resolved) identifier value, but do not resolve
		// to the actual referenced entity instance
		// NOTE: the owner of the association is not really the owner of the id!
		Serializable id = (Serializable) getIdentifierOrUniqueKeyType( session.getFactory() )
				.nullSafeGet( rs, names, session, null );
		scheduleBatchLoadIfNeeded( id, session );
		return id;
	}

	/**
	 * Register the entity as batch loadable, if enabled
	 */
	private void scheduleBatchLoadIfNeeded(
			Serializable id,
			SessionImplementor session) throws MappingException {
		//cannot batch fetch by unique key (property-ref associations)
		if ( uniqueKeyPropertyName == null && id != null ) {
			EntityPersister persister = session.getFactory().getEntityPersister( getAssociatedEntityName() );
			EntityKey entityKey = new EntityKey( id, persister, session.getEntityMode() );
			if ( !session.getPersistenceContext().containsEntity( entityKey ) ) {
				session.getPersistenceContext()
						.getBatchFetchQueue()
						.addBatchLoadableEntityKey( entityKey );
			}
		}
	}
	
	public boolean useLHSPrimaryKey() {
		return false;
	}

	public boolean isModified(
			Object old,
			Object current,
			boolean[] checkable,
			SessionImplementor session) throws HibernateException {
		if ( current == null ) {
			return old!=null;
		}
		if ( old == null ) {
			// we already know current is not null...
			return true;
		}
		// the ids are fully resolved, so compare them with isDirty(), not isModified()
		return getIdentifierOrUniqueKeyType( session.getFactory() )
				.isDirty( old, getIdentifier( current, session ), session );
	}

	public Serializable disassemble(
			Object value,
			SessionImplementor session,
			Object owner) throws HibernateException {

		if ( isNotEmbedded( session ) ) {
			return getIdentifierType( session ).disassemble( value, session, owner );
		}
		
		if ( value == null ) {
			return null;
		}
		else {
			// cache the actual id of the object, not the value of the
			// property-ref, which might not be initialized
			Object id = ForeignKeys.getEntityIdentifierIfNotUnsaved( 
					getAssociatedEntityName(), 
					value, 
					session
			);
			if ( id == null ) {
				throw new AssertionFailure(
						"cannot cache a reference to an object with a null id: " + 
						getAssociatedEntityName()
				);
			}
			return getIdentifierType( session ).disassemble( id, session, owner );
		}
	}

	public Object assemble(
			Serializable oid,
			SessionImplementor session,
			Object owner) throws HibernateException {
		
		//TODO: currently broken for unique-key references (does not detect
		//      change to unique key property of the associated object)
		
		Serializable id = assembleId( oid, session );

		if ( isNotEmbedded( session ) ) {
			return id;
		}
		
		if ( id == null ) {
			return null;
		}
		else {
			return resolveIdentifier( id, session );
		}
	}

	private Serializable assembleId(Serializable oid, SessionImplementor session) {
		//the owner of the association is not the owner of the id
		return ( Serializable ) getIdentifierType( session ).assemble( oid, session, null );
	}

	public void beforeAssemble(Serializable oid, SessionImplementor session) {
		scheduleBatchLoadIfNeeded( assembleId( oid, session ), session );
	}
	
	public boolean[] toColumnNullness(Object value, Mapping mapping) {
		boolean[] result = new boolean[ getColumnSpan( mapping ) ];
		if ( value != null ) {
			Arrays.fill( result, true );
		}
		return result;
	}
	
	public boolean isDirty(
			Object old,
			Object current,
			SessionImplementor session) throws HibernateException {
		if ( isSame( old, current, session.getEntityMode() ) ) {
			return false;
		}
		Object oldid = getIdentifier( old, session );
		Object newid = getIdentifier( current, session );
		return getIdentifierType( session ).isDirty( oldid, newid, session );
	}

	public boolean isDirty(
			Object old,
			Object current,
			boolean[] checkable,
			SessionImplementor session) throws HibernateException {
		if ( isAlwaysDirtyChecked() ) {
			return isDirty( old, current, session );
		}
		else {
			if ( isSame( old, current, session.getEntityMode() ) ) {
				return false;
			}
			Object oldid = getIdentifier( old, session );
			Object newid = getIdentifier( current, session );
			return getIdentifierType( session ).isDirty( oldid, newid, checkable, session );
		}
		
	}

}
