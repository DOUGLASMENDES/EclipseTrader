//$Id: Ejb3Column.java 12781 2007-07-19 22:28:14Z epbernard $
package org.hibernate.cfg;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.annotations.Index;
import org.hibernate.cfg.annotations.Nullability;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.util.StringHelper;

/**
 * Wrap state of an EJB3 @Column annotation
 * and build the Hibernate column mapping element
 *
 * @author Emmanuel Bernard
 */
public class Ejb3Column {
	private static final Log log = LogFactory.getLog( Ejb3Column.class );
	private Column mappingColumn;
	private boolean insertable = true;
	private boolean updatable = true;
	private String secondaryTableName;
	protected Map<String, Join> joins;
	protected PropertyHolder propertyHolder;
	private ExtendedMappings mappings;
	private boolean isImplicit;
	public static final int DEFAULT_COLUMN_LENGTH = 255;
	public String sqlType;
	private int length = DEFAULT_COLUMN_LENGTH;
	private int precision;
	private int scale;
	private String logicalColumnName;
	private String propertyName;
	private boolean unique;
	private boolean nullable = true;
	private String formulaString;
	private Formula formula;
	private Table table;

	public void setTable(Table table) {
		this.table = table;
	}

	public String getLogicalColumnName() {
		return logicalColumnName;
	}

	public String getSqlType() {
		return sqlType;
	}

	public int getLength() {
		return length;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}

	public boolean isUnique() {
		return unique;
	}

	public String getFormulaString() {
		return formulaString;
	}

	public String getSecondaryTableName() {
		return secondaryTableName;
	}

	public void setFormula(String formula) {
		this.formulaString = formula;
	}

	public boolean isImplicit() {
		return isImplicit;
	}

	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}

	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}

	protected ExtendedMappings getMappings() {
		return mappings;
	}

	public void setMappings(ExtendedMappings mappings) {
		this.mappings = mappings;
	}

	public void setImplicit(boolean implicit) {
		isImplicit = implicit;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public void setLogicalColumnName(String logicalColumnName) {
		this.logicalColumnName = logicalColumnName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isNullable() {
		return mappingColumn.isNullable();
	}

	public Ejb3Column() {
	}

	public void bind() {
		if ( StringHelper.isNotEmpty( formulaString ) ) {
			log.debug( "binding formula " + formulaString );
			formula = new Formula();
			formula.setFormula( formulaString );
		}
		else {
			initMappingColumn(
					logicalColumnName, propertyName, length, precision, scale, nullable, sqlType, unique, true
			);
			log.debug( "Binding column " + mappingColumn.getName() + " unique " + unique );
		}
	}

	protected void initMappingColumn(
			String columnName, String propertyName, int length, int precision, int scale, boolean nullable,
			String sqlType, boolean unique, boolean applyNamingStrategy
	) {
		this.mappingColumn = new Column();
		redefineColumnName( columnName, propertyName, applyNamingStrategy );
		this.mappingColumn.setLength( length );
		if ( precision > 0 ) {  //revelent precision
			this.mappingColumn.setPrecision( precision );
			this.mappingColumn.setScale( scale );
		}
		this.mappingColumn.setNullable( nullable );
		this.mappingColumn.setSqlType( sqlType );
		this.mappingColumn.setUnique( unique );
	}

	public boolean isNameDeferred() {
		return mappingColumn == null || StringHelper.isEmpty( mappingColumn.getName() );
	}

	public void redefineColumnName(String columnName, String propertyName, boolean applyNamingStrategy) {
		if ( applyNamingStrategy ) {
			if ( StringHelper.isEmpty( columnName ) ) {
				if ( propertyName != null ) {
					mappingColumn.setName( mappings.getNamingStrategy().propertyToColumnName( propertyName ) );
				}
				//Do nothing otherwise
			}
			else {
				mappingColumn.setName( mappings.getNamingStrategy().columnName( columnName ) );
			}
		}
		else {
			if ( StringHelper.isNotEmpty( columnName ) ) mappingColumn.setName( columnName );
		}
	}

	public String getName() {
		return mappingColumn.getName();
	}

	public Column getMappingColumn() {
		return mappingColumn;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public void setNullable(boolean nullable) {
		if ( mappingColumn != null ) {
			mappingColumn.setNullable( nullable );
		}
		else {
			this.nullable = nullable;
		}
	}

	public void setJoins(Map<String, Join> joins) {
		this.joins = joins;
	}

	public PropertyHolder getPropertyHolder() {
		return propertyHolder;
	}

	public void setPropertyHolder(PropertyHolder propertyHolder) {
		this.propertyHolder = propertyHolder;
	}

	protected void setMappingColumn(Column mappingColumn) {
		this.mappingColumn = mappingColumn;
	}

	public void linkWithValue(SimpleValue value) {
		if ( formula != null ) {
			value.addFormula( formula );
		}
		else {
			getMappingColumn().setValue( value );
			value.addColumn( getMappingColumn() );
			value.getTable().addColumn( getMappingColumn() );
			addColumnBinding( value );
			table = value.getTable();
		}
	}

	protected void addColumnBinding(SimpleValue value) {
		String logicalColumnName = mappings.getNamingStrategy()
				.logicalColumnName( this.logicalColumnName, propertyName );
		mappings.addColumnBinding( logicalColumnName, getMappingColumn(), value.getTable() );
	}

	/**
	 * Find appropriate table of the column.
	 * It can come from a secondary table or from the main table of the persistent class
	 *
	 * @return appropriate table
	 * @throws AnnotationException missing secondary table
	 */
	public Table getTable() {
		if ( table != null ) return table; //association table
		if ( isSecondary() ) {
			return getJoin().getTable();
		}
		else {
			return propertyHolder.getTable();
		}
	}

	public boolean isSecondary() {
		if ( propertyHolder == null ) {
			throw new AssertionFailure( "Should not call getTable() on column wo persistent class defined" );
		}
		if ( StringHelper.isNotEmpty( secondaryTableName ) ) {
			return true;
		}
		// else {
		return false;
	}

	public Join getJoin() {
		Join join = joins.get( secondaryTableName );
		if ( join == null ) {
			throw new AnnotationException(
					"Cannot find the expected secondary table: no "
							+ secondaryTableName + " available for " + propertyHolder.getClassName()
			);
		}
		else {
			return join;
		}
	}

	public void forceNotNull() {
		mappingColumn.setNullable( false );
	}

	public void setSecondaryTableName(String secondaryTableName) {
		this.secondaryTableName = secondaryTableName;
	}

	public static Ejb3Column[] buildColumnFromAnnotation(
			javax.persistence.Column[] anns,
			org.hibernate.annotations.Formula formulaAnn, Nullability nullability, PropertyHolder propertyHolder,
			PropertyData inferredData,
			Map<String, Join> secondaryTables,
			ExtendedMappings mappings
	) {
		Ejb3Column[] columns;
		if ( formulaAnn != null ) {
			Ejb3Column formulaColumn = new Ejb3Column();
			formulaColumn.setFormula( formulaAnn.value() );
			formulaColumn.setImplicit( false );
			formulaColumn.setMappings( mappings );
			formulaColumn.setPropertyHolder( propertyHolder );
			formulaColumn.bind();
			columns = new Ejb3Column[] { formulaColumn };
		}
		else {
			javax.persistence.Column[] actualCols = anns;
			javax.persistence.Column[] overriddenCols = propertyHolder.getOverriddenColumn(
					StringHelper.qualify( propertyHolder.getPath(), inferredData.getPropertyName() )
			);
			if ( overriddenCols != null ) {
				//check for overridden first
				if ( anns != null && overriddenCols.length != anns.length ) {
					throw new AnnotationException( "AttributeOverride.column() should override all columns for now" );
				}
				actualCols = overriddenCols.length == 0 ? null : overriddenCols;
				log.debug( "Column(s) overridden for property " + inferredData.getPropertyName() );
			}
			if ( actualCols == null ) {
				columns = buildImplicitColumn( inferredData, secondaryTables, propertyHolder, nullability, mappings );
			}
			else {
				final int length = actualCols.length;
				columns = new Ejb3Column[length];
				for (int index = 0; index < length; index++) {
					javax.persistence.Column col = actualCols[index];
					String sqlType = col.columnDefinition().equals( "" ) ? null : col.columnDefinition();
					Ejb3Column column = new Ejb3Column();
					column.setImplicit( false );
					column.setSqlType( sqlType );
					column.setLength( col.length() );
					column.setPrecision( col.precision() );
					column.setScale( col.scale() );
					column.setLogicalColumnName( col.name() );
					column.setPropertyName(
							BinderHelper.getRelativePath( propertyHolder, inferredData.getPropertyName() )
					);
					column.setNullable(
							col.nullable()
					); //TODO force to not null if available? This is a (bad) user choice.
					column.setUnique( col.unique() );
					column.setInsertable( col.insertable() );
					column.setUpdatable( col.updatable() );
					column.setSecondaryTableName( col.table() );
					column.setPropertyHolder( propertyHolder );
					column.setJoins( secondaryTables );
					column.setMappings( mappings );
					column.bind();
					columns[index] = column;
				}
			}
		}
		return columns;
	}

	private static Ejb3Column[] buildImplicitColumn(
			PropertyData inferredData, Map<String, Join> secondaryTables, PropertyHolder propertyHolder,
			Nullability nullability, ExtendedMappings mappings
	) {
		Ejb3Column[] columns;
		columns = new Ejb3Column[1];
		Ejb3Column column = new Ejb3Column();
		column.setImplicit( false );
		//not following the spec but more clean
		if ( nullability != Nullability.FORCED_NULL
				&& inferredData.getClassOrElement().isPrimitive()
				&& !inferredData.getProperty().isArray() ) {
			column.setNullable( false );
		}
		column.setLength( DEFAULT_COLUMN_LENGTH );
		column.setPropertyName(
				BinderHelper.getRelativePath( propertyHolder, inferredData.getPropertyName() )
		);
		column.setPropertyHolder( propertyHolder );
		column.setJoins( secondaryTables );
		column.setMappings( mappings );
		column.bind();
		columns[0] = column;
		return columns;
	}

	public static void checkPropertyConsistency(Ejb3Column[] columns, String propertyName) {
		int nbrOfColumns = columns.length;
		if ( nbrOfColumns > 1 ) {
			for (int currentIndex = 1; currentIndex < nbrOfColumns; currentIndex++) {
				if ( columns[currentIndex].isInsertable() != columns[currentIndex - 1].isInsertable() ) {
					throw new AnnotationException(
							"Mixing insertable and non insertable columns in a property is not allowed: " + propertyName
					);
				}
				if ( columns[currentIndex].isNullable() != columns[currentIndex - 1].isNullable() ) {
					throw new AnnotationException(
							"Mixing nullable and non nullable columns in a property is not allowed: " + propertyName
					);
				}
				if ( columns[currentIndex].isUpdatable() != columns[currentIndex - 1].isUpdatable() ) {
					throw new AnnotationException(
							"Mixing updatable and non updatable columns in a property is not allowed: " + propertyName
					);
				}
				if ( !columns[currentIndex].getTable().equals( columns[currentIndex - 1].getTable() ) ) {
					throw new AnnotationException(
							"Mixing different tables in a property is not allowed: " + propertyName
					);
				}
			}
		}
	}

	public void addIndex(Index index, boolean inSecondPass) {
		if ( index == null ) return;
		String indexName = index.name();
		addIndex( indexName, inSecondPass );
	}

	void addIndex(String indexName, boolean inSecondPass) {
		IndexOrUniqueKeySecondPass secondPass = new IndexOrUniqueKeySecondPass( indexName, this, mappings, false );
		if ( inSecondPass ) {
			secondPass.doSecondPass( mappings.getClasses() );
		}
		else {
			mappings.addSecondPass(
					secondPass
			);
		}
	}

	void addUniqueKey(String uniqueKeyName, boolean inSecondPass) {
		IndexOrUniqueKeySecondPass secondPass = new IndexOrUniqueKeySecondPass( uniqueKeyName, this, mappings, true );
		if ( inSecondPass ) {
			secondPass.doSecondPass( mappings.getClasses() );
		}
		else {
			mappings.addSecondPass(
					secondPass
			);
		}
	}
}