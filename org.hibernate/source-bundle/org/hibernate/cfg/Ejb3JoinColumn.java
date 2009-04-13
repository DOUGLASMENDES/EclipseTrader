//$Id: Ejb3JoinColumn.java 14389 2008-03-04 21:22:12Z epbernard $
package org.hibernate.cfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.common.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;

/**
 * Wrap state of an EJB3 @JoinColumn annotation
 * and build the Hibernate column mapping element
 *
 * @author Emmanuel Bernard
 */
public class Ejb3JoinColumn extends Ejb3Column {
	/**
	 * property name repated to this column
	 */
	private String referencedColumn;
	private String mappedBy;
	//property name on the mapped by side if any
	private String mappedByPropertyName;
	//table name on the mapped by side if any
	private String mappedByTableName;
	private String mappedByEntityName;

	//FIXME hacky solution to get the information at proeprty ref resolution
	public String getManyToManyOwnerSideEntityName() {
		return manyToManyOwnerSideEntityName;
	}

	public void setManyToManyOwnerSideEntityName(String manyToManyOwnerSideEntityName) {
		this.manyToManyOwnerSideEntityName = manyToManyOwnerSideEntityName;
	}

	private String manyToManyOwnerSideEntityName;

	public void setReferencedColumn(String referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public void setMappedBy(String mappedBy) {
		this.mappedBy = mappedBy;
	}

	//Due to @AnnotationOverride overriding rules, I don't want the constructor to be public
	private Ejb3JoinColumn() {
		setMappedBy( BinderHelper.ANNOTATION_STRING_DEFAULT );
	}

	//Due to @AnnotationOverride overriding rules, I don't want the constructor to be public
	//TODO get rid of it and use setters
	private Ejb3JoinColumn(
			String sqlType,
			String name,
			boolean nullable,
			boolean unique,
			boolean insertable,
			boolean updatable,
			String referencedColumn,
			String secondaryTable,
			Map<String, Join> joins,
			PropertyHolder propertyHolder,
			String propertyName,
			String mappedBy,
			boolean isImplicit,
			ExtendedMappings mappings
	) {
		super();
		setImplicit( isImplicit );
		setSqlType( sqlType );
		setLogicalColumnName( name );
		setNullable( nullable );
		setUnique( unique );
		setInsertable( insertable );
		setUpdatable( updatable );
		setSecondaryTableName( secondaryTable );
		setPropertyHolder( propertyHolder );
		setJoins( joins );
		setMappings( mappings );
		setPropertyName( BinderHelper.getRelativePath( propertyHolder, propertyName ) );
		bind();
		this.referencedColumn = referencedColumn;
		this.mappedBy = mappedBy;
	}

	public String getReferencedColumn() {
		return referencedColumn;
	}

	public static Ejb3JoinColumn[] buildJoinColumns(
			JoinColumn[] anns,
			String mappedBy, Map<String, Join> joins,
			PropertyHolder propertyHolder,
			String propertyName,
			ExtendedMappings mappings
	) {
		JoinColumn[] actualColumns = propertyHolder.getOverriddenJoinColumn(
				StringHelper.qualify( propertyHolder.getPath(), propertyName )
		);
		if ( actualColumns == null ) actualColumns = anns;
		if ( actualColumns == null || actualColumns.length == 0 ) {
			return new Ejb3JoinColumn[] {
					buildJoinColumn( (JoinColumn) null, mappedBy, joins, propertyHolder, propertyName, mappings )
			};
		}
		else {
			int size = actualColumns.length;
			Ejb3JoinColumn[] result = new Ejb3JoinColumn[size];
			for (int index = 0; index < size; index++) {
				result[index] = buildJoinColumn(
						actualColumns[index],
						mappedBy,
						joins,
						propertyHolder,
						propertyName,
						mappings
				);
			}
			return result;
		}
	}

	/**
	 * build join column for SecondaryTables
	 */
	private static Ejb3JoinColumn buildJoinColumn(
			JoinColumn ann,
			String mappedBy, Map<String, Join> joins,
			PropertyHolder propertyHolder,
			String propertyName,
			ExtendedMappings mappings
	) {
		if ( ann != null ) {
			if ( BinderHelper.isDefault( mappedBy ) ) {
				throw new AnnotationException(
						"Illegal attempt to define a @JoinColumn with a mappedBy association: "
								+ BinderHelper.getRelativePath( propertyHolder, propertyName )
				);
			}
			Ejb3JoinColumn joinColumn = new Ejb3JoinColumn();
			joinColumn.setJoinAnnotation( ann, null );
			joinColumn.setJoins( joins );
			joinColumn.setPropertyHolder( propertyHolder );
			joinColumn.setPropertyName( BinderHelper.getRelativePath( propertyHolder, propertyName ) );
			joinColumn.setImplicit( false );
			joinColumn.setMappings( mappings );
			joinColumn.bind();
			return joinColumn;
		}
		else {
			Ejb3JoinColumn joinColumn = new Ejb3JoinColumn();
			joinColumn.setMappedBy( mappedBy );
			joinColumn.setJoins( joins );
			joinColumn.setPropertyHolder( propertyHolder );
			joinColumn.setPropertyName( BinderHelper.getRelativePath( propertyHolder, propertyName ) );
			joinColumn.setImplicit( true );
			joinColumn.setMappings( mappings );
			joinColumn.bind();
			return joinColumn;
		}
	}


	//FIXME default name still useful in association table
	public void setJoinAnnotation(JoinColumn annJoin, String defaultName) {
		if ( annJoin == null ) {
			setImplicit( true );
		}
		else {
			setImplicit( false );
			if ( !BinderHelper.isDefault( annJoin.columnDefinition() ) ) setSqlType( annJoin.columnDefinition() );
			if ( !BinderHelper.isDefault( annJoin.name() ) ) setLogicalColumnName( annJoin.name() );
			setNullable( annJoin.nullable() );
			setUnique( annJoin.unique() );
			setInsertable( annJoin.insertable() );
			setUpdatable( annJoin.updatable() );
			setReferencedColumn( annJoin.referencedColumnName() );
			setSecondaryTableName( annJoin.table() );
		}
	}

	/**
	 * Build JoinColumn for a JOINED hierarchy
	 */
	public static Ejb3JoinColumn buildJoinColumn(
			PrimaryKeyJoinColumn pkJoinAnn,
			JoinColumn joinAnn,
			Value identifier,
			Map<String, Join> joins,
			PropertyHolder propertyHolder, ExtendedMappings mappings
	) {

		Column col = (Column) identifier.getColumnIterator().next();
		String defaultName = mappings.getLogicalColumnName( col.getName(), identifier.getTable() );
		if ( pkJoinAnn != null || joinAnn != null ) {
			String colName;
			String columnDefinition;
			String referencedColumnName;
			if ( pkJoinAnn != null ) {
				colName = pkJoinAnn.name();
				columnDefinition = pkJoinAnn.columnDefinition();
				referencedColumnName = pkJoinAnn.referencedColumnName();
			}
			else {
				colName = joinAnn.name();
				columnDefinition = joinAnn.columnDefinition();
				referencedColumnName = joinAnn.referencedColumnName();
			}
			String sqlType = "".equals( columnDefinition ) ? null : columnDefinition;
			String name = "".equals( colName ) ? defaultName : colName;
			return new Ejb3JoinColumn(
					sqlType,
					name, false, false,
					true, true,
					referencedColumnName,
					null, joins,
					propertyHolder, null, null, false, mappings
			);
		}
		else {
			return new Ejb3JoinColumn(
					(String) null, defaultName,
					false, false, true, true, null, (String) null,
					joins, propertyHolder, null, null, true, mappings
			);
		}
	}

	/**
	 * Override persistent class on oneToMany Cases for late settings
	 * Must only be used on second level pass binding
	 */
	public void setPersistentClass(PersistentClass persistentClass, Map<String, Join> joins) {
		//FIXME shouldn't we deduce the classname from the persistentclasS?
		this.propertyHolder = PropertyHolderBuilder.buildPropertyHolder( persistentClass, joins, getMappings() );
	}

	public static void checkIfJoinColumn(Object columns, PropertyHolder holder, PropertyData property) {
		if ( !( columns instanceof Ejb3JoinColumn[] ) ) {
			throw new AnnotationException(
					"@Column cannot be used on an association property: "
							+ holder.getEntityName()
							+ "."
							+ property.getPropertyName()
			);
		}
	}

	public void linkValueUsingDefaultColumnNaming(
			Column referencedColumn, PersistentClass referencedEntity, SimpleValue value
	) {
		String columnName;
		String logicalReferencedColumn = getMappings().getLogicalColumnName(
				referencedColumn.getName(), referencedEntity.getTable()
		);
		boolean mappedBySide = mappedByTableName != null || mappedByPropertyName != null;
		boolean ownerSide = getPropertyName() != null;

		Boolean isRefColumnQuoted = StringHelper.isQuoted( logicalReferencedColumn );
		String unquotedLogicalReferenceColumn = isRefColumnQuoted ?
				StringHelper.unquote( logicalReferencedColumn ) :
				logicalReferencedColumn;

		if ( mappedBySide ) {
			String unquotedMappedbyTable = StringHelper.unquote( mappedByTableName );
			columnName = getMappings().getNamingStrategy().foreignKeyColumnName(
					mappedByPropertyName,
					mappedByEntityName,
					unquotedMappedbyTable,
					unquotedLogicalReferenceColumn
			);
			//one element was quoted so we quote
			if ( isRefColumnQuoted || StringHelper.isQuoted( mappedByTableName ) ) {
				columnName = StringHelper.quote( columnName );
			}
		}
		else if ( ownerSide ) {
			String logicalTableName = getMappings().getLogicalTableName( referencedEntity.getTable() );
			String unquotedLogicalTableName = StringHelper.unquote( logicalTableName );
			columnName = getMappings().getNamingStrategy().foreignKeyColumnName(
					getPropertyName(),
					referencedEntity.getEntityName(),
					unquotedLogicalTableName,
					unquotedLogicalReferenceColumn
			);
			//one element was quoted so we quote
			if ( isRefColumnQuoted || StringHelper.isQuoted( logicalTableName ) ) {
				columnName = StringHelper.quote( columnName );
			}
		}
		else {
			//is an intra-entity hierarchy table join so copy the name by default
			String logicalTableName = getMappings().getLogicalTableName( referencedEntity.getTable() );
			String unquotedLogicalTableName = StringHelper.unquote( logicalTableName );
			columnName = getMappings().getNamingStrategy().joinKeyColumnName(
					unquotedLogicalReferenceColumn,
					unquotedLogicalTableName
			);
			//one element was quoted so we quote
			if ( isRefColumnQuoted || StringHelper.isQuoted( logicalTableName ) ) {
				columnName = StringHelper.quote( columnName );
			}
		}
		//yuk side effect on an implicit column
		setLogicalColumnName( columnName );
		setReferencedColumn( logicalReferencedColumn );
		initMappingColumn(
				columnName,
				null, referencedColumn.getLength(),
				referencedColumn.getPrecision(),
				referencedColumn.getScale(),
				getMappingColumn().isNullable(),
				referencedColumn.getSqlType(),
				getMappingColumn().isUnique(), false
		);
		linkWithValue( value );
	}

	/**
	 * used for mappedBy cases
	 */
	public void linkValueUsingAColumnCopy(Column column, SimpleValue value) {
		initMappingColumn(
				//column.getName(),
				column.getQuotedName(),
				null, column.getLength(),
				column.getPrecision(),
				column.getScale(),
				getMappingColumn().isNullable(),
				column.getSqlType(),
				getMappingColumn().isUnique(),
				false //We do copy no strategy here
		);
		linkWithValue( value );
	}

	protected void addColumnBinding(SimpleValue value) {
		if ( StringHelper.isEmpty( mappedBy ) ) {
			String unquotedLogColName = StringHelper.unquote( getLogicalColumnName() );
			String unquotedRefColumn = StringHelper.unquote( getReferencedColumn() );
			String logicalColumnName = getMappings().getNamingStrategy()
					.logicalCollectionColumnName( unquotedLogColName, getPropertyName(), unquotedRefColumn );
			if ( StringHelper.isQuoted( getLogicalColumnName() ) || StringHelper.isQuoted( getLogicalColumnName() ) ) {
				logicalColumnName = StringHelper.quote( logicalColumnName );
			}
			getMappings().addColumnBinding( logicalColumnName, getMappingColumn(), value.getTable() );
		}
	}

	//keep it JDK 1.4 compliant
	//implicit way
	public static final int NO_REFERENCE = 0;
	//reference to the pk in an explicit order
	public static final int PK_REFERENCE = 1;
	//reference to non pk columns
	public static final int NON_PK_REFERENCE = 2;

	public static int checkReferencedColumnsType(
			Ejb3JoinColumn[] columns, PersistentClass referencedEntity,
			ExtendedMappings mappings
	) {
		//convenient container to find whether a column is an id one or not
		Set<Column> idColumns = new HashSet<Column>();
		Iterator idColumnsIt = referencedEntity.getKey().getColumnIterator();
		while ( idColumnsIt.hasNext() ) {
			idColumns.add( (Column) idColumnsIt.next() );
		}

		boolean isFkReferencedColumnName = false;
		boolean noReferencedColumn = true;
		//build the list of potential tables
		if ( columns.length == 0 ) return NO_REFERENCE; //shortcut
		Object columnOwner = BinderHelper.findColumnOwner(
				referencedEntity, columns[0].getReferencedColumn(), mappings
		);
		if ( columnOwner == null ) {
			try {
				throw new MappingException(
						"Unable to find column with logical name: "
								+ columns[0].getReferencedColumn() + " in " + referencedEntity.getTable() + " and its related "
								+ "supertables and secondary tables"
				);
			}
			catch (MappingException e) {
				throw new RecoverableException(e);
			}
		}
		Table matchingTable = columnOwner instanceof PersistentClass ?
				( (PersistentClass) columnOwner ).getTable() :
				( (Join) columnOwner ).getTable();
		//check each referenced column
		for (Ejb3JoinColumn ejb3Column : columns) {
			String logicalReferencedColumnName = ejb3Column.getReferencedColumn();
			if ( StringHelper.isNotEmpty( logicalReferencedColumnName ) ) {
				String referencedColumnName = null;
				try {
					referencedColumnName = mappings.getPhysicalColumnName( logicalReferencedColumnName, matchingTable );
				}
				catch (MappingException me) {
					//rewrite the exception
					throw new MappingException(
							"Unable to find column with logical name: "
									+ logicalReferencedColumnName + " in " + matchingTable.getName()
					);
				}
				noReferencedColumn = false;
				Column refCol = new Column( referencedColumnName );
				boolean contains = idColumns.contains( refCol );
				if ( !contains ) {
					isFkReferencedColumnName = true;
					break; //we know the state
				}
			}
		}
		if ( isFkReferencedColumnName ) {
			return NON_PK_REFERENCE;
		}
		else if ( noReferencedColumn ) {
			return NO_REFERENCE;
		}
		else if ( idColumns.size() != columns.length ) {
			//reference use PK but is a subset or a superset
			return NON_PK_REFERENCE;
		}
		else {
			return PK_REFERENCE;
		}
	}

	public void overrideSqlTypeIfNecessary(org.hibernate.mapping.Column column) {
		if ( StringHelper.isEmpty( sqlType ) ) {
			sqlType = column.getSqlType();
			if ( getMappingColumn() != null ) getMappingColumn().setSqlType( sqlType );
		}
	}

	@Override
	public void redefineColumnName(String columnName, String propertyName, boolean applyNamingStrategy) {
		if ( StringHelper.isNotEmpty( columnName ) ) {
			getMappingColumn().setName(
					applyNamingStrategy ?
							getMappings().getNamingStrategy().columnName( columnName ) :
							columnName
			);
		}
	}

	public static Ejb3JoinColumn[] buildJoinTableJoinColumns(
			JoinColumn[] annJoins, Map<String, Join> secondaryTables,
			PropertyHolder propertyHolder, String propertyName, String mappedBy, ExtendedMappings mappings
	) {
		Ejb3JoinColumn[] joinColumns;
		if ( annJoins == null ) {
			Ejb3JoinColumn currentJoinColumn = new Ejb3JoinColumn();
			currentJoinColumn.setImplicit( true );
			currentJoinColumn.setNullable( false ); //I break the spec, but it's for good
			currentJoinColumn.setPropertyHolder( propertyHolder );
			currentJoinColumn.setJoins( secondaryTables );
			currentJoinColumn.setMappings( mappings );
			currentJoinColumn.setPropertyName(
					BinderHelper.getRelativePath( propertyHolder, propertyName )
			);
			currentJoinColumn.setMappedBy( mappedBy );
			currentJoinColumn.bind();

			joinColumns = new Ejb3JoinColumn[] {
					currentJoinColumn

			};
		}
		else {
			joinColumns = new Ejb3JoinColumn[annJoins.length];
			JoinColumn annJoin;
			int length = annJoins.length;
			for (int index = 0; index < length; index++) {
				annJoin = annJoins[index];
				Ejb3JoinColumn currentJoinColumn = new Ejb3JoinColumn();
				currentJoinColumn.setImplicit( true );
				currentJoinColumn.setPropertyHolder( propertyHolder );
				currentJoinColumn.setJoins( secondaryTables );
				currentJoinColumn.setMappings( mappings );
				currentJoinColumn.setPropertyName( BinderHelper.getRelativePath( propertyHolder, propertyName ) );
				currentJoinColumn.setMappedBy( mappedBy );
				currentJoinColumn.setJoinAnnotation( annJoin, propertyName );
				currentJoinColumn.setNullable( false ); //I break the spec, but it's for good
				//done after the annotation to override it
				currentJoinColumn.bind();
				joinColumns[index] = currentJoinColumn;
			}
		}
		return joinColumns;
	}

	public void setMappedBy(String entityName, String logicalTableName, String mappedByProperty) {
		this.mappedByEntityName = entityName;
		this.mappedByTableName = logicalTableName;
		this.mappedByPropertyName = mappedByProperty;
	}
}