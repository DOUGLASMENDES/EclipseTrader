//$Id: Constraint.java 10660 2006-10-31 02:18:40Z epbernard $
package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.Mapping;

/**
 * A relational constraint.
 *
 * @author Gavin King
 */
public abstract class Constraint implements RelationalModel, Serializable {

	private String name;
	private final List columns = new ArrayList();
	private Table table;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Iterator getColumnIterator() {
		return columns.iterator();
	}

	public void addColumn(Column column) {
		if ( !columns.contains( column ) ) columns.add( column );
	}

	public void addColumns(Iterator columnIterator) {
		while ( columnIterator.hasNext() ) {
			Selectable col = (Selectable) columnIterator.next();
			if ( !col.isFormula() ) addColumn( (Column) col );
		}
	}

	/**
	 * @param column
	 * @return true if this constraint already contains a column with same name.
	 */
	public boolean containsColumn(Column column) {
		return columns.contains( column );
	}

	public int getColumnSpan() {
		return columns.size();
	}

	public Column getColumn(int i) {
		return (Column) columns.get( i );
	}

	public Iterator columnIterator() {
		return columns.iterator();
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public boolean isGenerated(Dialect dialect) {
		return true;
	}

	public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
		if ( isGenerated( dialect ) ) {
			return "alter table " + getTable()
					.getQualifiedName( dialect, defaultCatalog, defaultSchema ) + " drop constraint " + getName();
		}
		else {
			return null;
		}
	}

	public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema) {
		if ( isGenerated( dialect ) ) {
			String constraintString = sqlConstraintString( dialect, getName(), defaultCatalog, defaultSchema );
			StringBuffer buf = new StringBuffer( "alter table " )
					.append( getTable().getQualifiedName( dialect, defaultCatalog, defaultSchema ) )
					.append( constraintString );
			return buf.toString();
		}
		else {
			return null;
		}
	}

	public List getColumns() {
		return columns;
	}

	public abstract String sqlConstraintString(Dialect d, String constraintName, String defaultCatalog,
											   String defaultSchema);

	public String toString() {
		return getClass().getName() + '(' + getTable().getName() + getColumns() + ") as " + name;
	}
}
