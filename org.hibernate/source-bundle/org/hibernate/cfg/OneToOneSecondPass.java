//$Id: OneToOneSecondPass.java 12781 2007-07-19 22:28:14Z epbernard $
package org.hibernate.cfg;

import java.util.Iterator;
import java.util.Map;

import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.annotations.PropertyBinder;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.util.StringHelper;

/**
 * We have to handle OneToOne in a second pass because:
 * -
 */
public class OneToOneSecondPass implements SecondPass {
	private String mappedBy;
	private ExtendedMappings mappings;
	private String ownerEntity;
	private String ownerProperty;
	private PropertyHolder propertyHolder;
	private boolean ignoreNotFound;
	private PropertyData inferredData;
	private XClass targetEntity;
	private boolean cascadeOnDelete;
	private boolean optional;
	private String cascadeStrategy;
	private Ejb3JoinColumn[] joinColumns;

	//that suck, we should read that from the property mainly
	public OneToOneSecondPass(
			String mappedBy, String ownerEntity, String ownerProperty,
			PropertyHolder propertyHolder, PropertyData inferredData, XClass targetEntity, boolean ignoreNotFound,
			boolean cascadeOnDelete, boolean optional, String cascadeStrategy, Ejb3JoinColumn[] columns,
			ExtendedMappings mappings
	) {
		this.ownerEntity = ownerEntity;
		this.ownerProperty = ownerProperty;
		this.mappedBy = mappedBy;
		this.propertyHolder = propertyHolder;
		this.mappings = mappings;
		this.ignoreNotFound = ignoreNotFound;
		this.inferredData = inferredData;
		this.targetEntity = targetEntity;
		this.cascadeOnDelete = cascadeOnDelete;
		this.optional = optional;
		this.cascadeStrategy = cascadeStrategy;
		this.joinColumns = columns;
	}

	//TODO refactor this code, there is a lot of duplication in this method
	public void doSecondPass(Map persistentClasses) throws MappingException {
		org.hibernate.mapping.OneToOne value = new org.hibernate.mapping.OneToOne(
				propertyHolder.getTable(), propertyHolder.getPersistentClass()
		);
		final String propertyName = inferredData.getPropertyName();
		value.setPropertyName( propertyName );
		String referencedEntityName;
		if ( AnnotationBinder.isDefault( targetEntity, mappings ) ) {
			referencedEntityName = inferredData.getClassOrElementName();
		}
		else {
			referencedEntityName = targetEntity.getName();
		}
		value.setReferencedEntityName( referencedEntityName );
		AnnotationBinder.defineFetchingStrategy( value, inferredData.getProperty() );
		//value.setFetchMode( fetchMode );
		value.setCascadeDeleteEnabled( cascadeOnDelete );
		//value.setLazy( fetchMode != FetchMode.JOIN );

		if ( !optional ) value.setConstrained( true );
		value.setForeignKeyType(
				value.isConstrained() ?
						ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT :
						ForeignKeyDirection.FOREIGN_KEY_TO_PARENT
		);
		PropertyBinder binder = new PropertyBinder();
		binder.setName( propertyName );
		binder.setValue( value );
		binder.setCascade( cascadeStrategy );
		binder.setPropertyAccessorName( inferredData.getDefaultAccess() );
		Property prop = binder.make();
		if ( BinderHelper.isDefault( mappedBy ) ) {
			/*
			 * we need to check if the columns are in the right order
			 * if not, then we need to create a many to one and formula
			 * but actually, since entities linked by a one to one need
			 * to share the same composite id class, this cannot happen in hibernate
			 */
			boolean rightOrder = true;

			if ( rightOrder ) {
				String path = StringHelper.qualify( propertyHolder.getPath(), propertyName );
				( new ToOneFkSecondPass(
						value, joinColumns,
						!optional, //cannot have nullabe and unique on certain DBs
						propertyHolder.getEntityOwnerClassName(),
						path, mappings
				) ).doSecondPass( persistentClasses );
				//no column associated since its a one to one
				propertyHolder.addProperty( prop );
			}
			else {
				//this is a many to one with Formula

			}
		}
		else {
			PersistentClass otherSide = (PersistentClass) persistentClasses.get( value.getReferencedEntityName() );
			Property otherSideProperty;
			try {
				if ( otherSide == null ) {
					throw new MappingException( "Unable to find entity: " + value.getReferencedEntityName() );
				}
				otherSideProperty = BinderHelper.findPropertyByName( otherSide, mappedBy );
			}
			catch (MappingException e) {
				throw new AnnotationException(
						"Unknown mappedBy in: " + StringHelper.qualify( ownerEntity, ownerProperty )
								+ ", referenced property unknown: "
								+ StringHelper.qualify( value.getReferencedEntityName(), mappedBy )
				);
			}
			if ( otherSideProperty == null ) {
				throw new AnnotationException(
						"Unknown mappedBy in: " + StringHelper.qualify( ownerEntity, ownerProperty )
								+ ", referenced property unknown: "
								+ StringHelper.qualify( value.getReferencedEntityName(), mappedBy )
				);
			}
			if ( otherSideProperty.getValue() instanceof OneToOne ) {
				propertyHolder.addProperty( prop );
			}
			else if ( otherSideProperty.getValue() instanceof ManyToOne ) {
				Iterator it = otherSide.getJoinIterator();
				Join otherSideJoin = null;
				while ( it.hasNext() ) {
					otherSideJoin = (Join) it.next();
					if ( otherSideJoin.containsProperty( otherSideProperty ) ) {
						break;
					}
				}
				if ( otherSideJoin != null ) {
					//@OneToOne @JoinTable
					Join mappedByJoin = buildJoin(
							(PersistentClass) persistentClasses.get( ownerEntity ), otherSideProperty, otherSideJoin
					);
					ManyToOne manyToOne = new ManyToOne( mappedByJoin.getTable() );
					//FIXME use ignore not found here
					manyToOne.setIgnoreNotFound( ignoreNotFound );
					manyToOne.setCascadeDeleteEnabled( value.isCascadeDeleteEnabled() );
					manyToOne.setEmbedded( value.isEmbedded() );
					manyToOne.setFetchMode( value.getFetchMode() );
					manyToOne.setLazy( value.isLazy() );
					manyToOne.setReferencedEntityName( value.getReferencedEntityName() );
					manyToOne.setUnwrapProxy( value.isUnwrapProxy() );
					prop.setValue( manyToOne );
					Iterator otherSideJoinKeyColumns = otherSideJoin.getKey().getColumnIterator();
					while ( otherSideJoinKeyColumns.hasNext() ) {
						Column column = (Column) otherSideJoinKeyColumns.next();
						Column copy = new Column();
						copy.setLength( column.getLength() );
						copy.setScale( column.getScale() );
						copy.setValue( manyToOne );
						copy.setName( column.getQuotedName() );
						copy.setNullable( column.isNullable() );
						copy.setPrecision( column.getPrecision() );
						copy.setUnique( column.isUnique() );
						copy.setSqlType( column.getSqlType() );
						copy.setCheckConstraint( column.getCheckConstraint() );
						copy.setComment( column.getComment() );
						copy.setDefaultValue( column.getDefaultValue() );
						manyToOne.addColumn( copy );
					}
					mappedByJoin.addProperty( prop );
				}
				else {
					propertyHolder.addProperty( prop );
				}

				value.setReferencedPropertyName( mappedBy );

				String propertyRef = value.getReferencedPropertyName();
				if ( propertyRef != null ) {
					mappings.addUniquePropertyReference(
							value.getReferencedEntityName(),
							propertyRef
					);
				}
			}
			else {
				throw new AnnotationException(
						"Referenced property not a (One|Many)ToOne: "
								+ StringHelper.qualify(
								otherSide.getEntityName(), mappedBy
						)
								+ " in mappedBy of "
								+ StringHelper.qualify( ownerEntity, ownerProperty )
				);
			}
		}
		ForeignKey fk = inferredData.getProperty().getAnnotation( ForeignKey.class );
		String fkName = fk != null ? fk.name() : "";
		if ( !BinderHelper.isDefault( fkName ) ) value.setForeignKeyName( fkName );
	}

	//dirty dupe of EntityBinder.bindSecondaryTable
	private Join buildJoin(PersistentClass persistentClass, Property otherSideProperty, Join originalJoin) {
		Join join = new Join();
		join.setPersistentClass( persistentClass );

		//no check constraints available on joins
		join.setTable( originalJoin.getTable() );
		join.setInverse( true );
		SimpleValue key = new DependantValue( join.getTable(), persistentClass.getIdentifier() );
		//TODO support @ForeignKey
		join.setKey( key );
		join.setSequentialSelect( false );
		//TODO support for inverse and optional
		join.setOptional( true ); //perhaps not quite per-spec, but a Good Thing anyway
		key.setCascadeDeleteEnabled( false );
		Iterator mappedByColumns = otherSideProperty.getValue().getColumnIterator();
		while ( mappedByColumns.hasNext() ) {
			Column column = (Column) mappedByColumns.next();
			Column copy = new Column();
			copy.setLength( column.getLength() );
			copy.setScale( column.getScale() );
			copy.setValue( key );
			copy.setName( column.getQuotedName() );
			copy.setNullable( column.isNullable() );
			copy.setPrecision( column.getPrecision() );
			copy.setUnique( column.isUnique() );
			copy.setSqlType( column.getSqlType() );
			copy.setCheckConstraint( column.getCheckConstraint() );
			copy.setComment( column.getComment() );
			copy.setDefaultValue( column.getDefaultValue() );
			key.addColumn( copy );
		}
		join.createPrimaryKey();
		join.createForeignKey();
		persistentClass.addJoin( join );
		return join;
	}
}

