//$Id: EnumType.java 12822 2007-07-26 02:12:05Z d.plentz $
package org.hibernate.type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.util.ReflectHelper;
import org.hibernate.util.StringHelper;

/**
 * Enum type mapper
 * Try and find the appropriate SQL type depending on column metadata
 *
 * @author Emmanuel Bernard
 */
//TODO implements readobject/writeobject to recalculate the enumclasses
public class EnumType implements EnhancedUserType, ParameterizedType, Serializable {
	private static Log log = LogFactory.getLog( EnumType.class );
	private static final boolean IS_TRACE_ENABLED;

	static {
		//cache this, because it was a significant performance cost
		IS_TRACE_ENABLED = LogFactory.getLog( StringHelper.qualifier( Type.class.getName() ) ).isTraceEnabled();
	}

	public static final String ENUM = "enumClass";
	public static final String SCHEMA = "schema";
	public static final String CATALOG = "catalog";
	public static final String TABLE = "table";
	public static final String COLUMN = "column";
	public static final String TYPE = "type";

	private static Map<Class, Object[]> enumValues = new HashMap<Class, Object[]>();

	private Class<? extends Enum> enumClass;
	private String column;
	private String table;
	private String catalog;
	private String schema;
	private boolean guessed = false;
	private int sqlType = Types.INTEGER; //before any guessing

	public int[] sqlTypes() {
		return new int[]{sqlType};
	}

	public Class returnedClass() {
		return enumClass;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return x == y;
	}

	public int hashCode(Object x) throws HibernateException {
		return x == null ? 0 : x.hashCode();
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		Object object = rs.getObject( names[0] );
		if ( rs.wasNull() ) {
			if ( IS_TRACE_ENABLED ) {
				log.debug( "Returning null as column " + names[0] );
			}
			return null;
		}
		if ( object instanceof Number ) {
			Object[] values = enumValues.get( enumClass );
			if ( values == null ) throw new AssertionFailure( "enumValues not preprocessed: " + enumClass );
			int ordinal = ( (Number) object ).intValue();
			if ( ordinal < 0 || ordinal >= values.length ) {
				throw new IllegalArgumentException( "Unknown ordinal value for enum " + enumClass + ": " + ordinal );
			}
			if ( IS_TRACE_ENABLED ) {
				log.debug( "Returning '" + ordinal + "' as column " + names[0] );
			}
			return values[ordinal];
		}
		else {
			String name = (String) object;
			if ( IS_TRACE_ENABLED ) {
				log.debug( "Returning '" + name + "' as column " + names[0] );
			}
			try {
				return Enum.valueOf( enumClass, name );
			}
			catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException( "Unknown name value for enum " + enumClass + ": " + name, iae );
			}
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		//if (!guessed) guessType( st, index );
		if ( value == null ) {
			if ( IS_TRACE_ENABLED ) log.debug( "Binding null to parameter: " + index );
			st.setNull( index, sqlType );
		}
		else {
			boolean isOrdinal = isOrdinal( sqlType );
			if ( isOrdinal ) {
				int ordinal = ( (Enum) value ).ordinal();
				if ( IS_TRACE_ENABLED ) {
					log.debug( "Binding '" + ordinal + "' to parameter: " + index );
				}
				st.setObject( index, Integer.valueOf( ordinal ), sqlType );
			}
			else {
				String enumString = ( (Enum) value ).name();
				if ( IS_TRACE_ENABLED ) {
					log.debug( "Binding '" + enumString + "' to parameter: " + index );
				}
				st.setObject( index, enumString, sqlType );
			}
		}
	}

	private boolean isOrdinal(int paramType) {
		switch ( paramType ) {
			case Types.INTEGER:
			case Types.NUMERIC:
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.BIGINT:
			case Types.DECIMAL: //for Oracle Driver
			case Types.DOUBLE:  //for Oracle Driver
			case Types.FLOAT:   //for Oracle Driver
				return true;
			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
				return false;
			default:
				throw new HibernateException( "Unable to persist an Enum in a column of SQL Type: " + paramType );
		}
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean isMutable() {
		return false;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public void setParameterValues(Properties parameters) {
		String enumClassName = parameters.getProperty( ENUM );
		try {
			enumClass = ReflectHelper.classForName( enumClassName, this.getClass() ).asSubclass( Enum.class );
		}
		catch (ClassNotFoundException exception) {
			throw new HibernateException( "Enum class not found", exception );
		}
		//this is threadsafe to do it here, setParameterValues() is called sequencially
		initEnumValue();
		//nullify unnullified properties yuck!
		schema = parameters.getProperty( SCHEMA );
		if ( "".equals( schema ) ) schema = null;
		catalog = parameters.getProperty( CATALOG );
		if ( "".equals( catalog ) ) catalog = null;
		table = parameters.getProperty( TABLE );
		column = parameters.getProperty( COLUMN );
		String type = parameters.getProperty( TYPE );
		if ( type != null ) {
			sqlType = Integer.decode( type ).intValue();
			guessed = true;
		}
	}

	private void initEnumValue() {
		Object[] values = enumValues.get( enumClass );
		if ( values == null ) {
			try {
				Method method = null;
				method = enumClass.getDeclaredMethod( "values", new Class[0] );
				values = (Object[]) method.invoke( null, new Object[0] );
				enumValues.put( enumClass, values );
			}
			catch (Exception e) {
				throw new HibernateException( "Error while accessing enum.values(): " + enumClass, e );
			}
		}
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		//FIXME Hum, I think I break the thread safety here
		ois.defaultReadObject();
		initEnumValue();
	}

	public String objectToSQLString(Object value) {
		boolean isOrdinal = isOrdinal( sqlType );
		if ( isOrdinal ) {
			int ordinal = ( (Enum) value ).ordinal();
			return Integer.toString( ordinal );
		}
		else {
			return '\'' + ( (Enum) value ).name() + '\'';
		}
	}

	public String toXMLString(Object value) {
		boolean isOrdinal = isOrdinal( sqlType );
		if ( isOrdinal ) {
			int ordinal = ( (Enum) value ).ordinal();
			return Integer.toString( ordinal );
		}
		else {
			return ( (Enum) value ).name();
		}
	}

	public Object fromXMLString(String xmlValue) {
		try {
			int ordinal = Integer.parseInt( xmlValue );
			Object[] values = enumValues.get( enumClass );
			if ( values == null ) throw new AssertionFailure( "enumValues not preprocessed: " + enumClass );
			if ( ordinal < 0 || ordinal >= values.length ) {
				throw new IllegalArgumentException( "Unknown ordinal value for enum " + enumClass + ": " + ordinal );
			}
			return values[ordinal];
		}
		catch(NumberFormatException e) {
			try {
				return Enum.valueOf( enumClass, xmlValue );
			}
			catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException( "Unknown name value for enum " + enumClass + ": " + xmlValue, iae );
			}
		}
	}
}
