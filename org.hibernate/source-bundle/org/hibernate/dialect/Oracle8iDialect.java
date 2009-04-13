//$Id: Oracle8iDialect.java 14189 2007-11-06 02:09:45Z d.plentz $
package org.hibernate.dialect;

import java.sql.Types;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;

import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.DecodeCaseFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.OracleJoinFragment;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.util.ReflectHelper;
import org.hibernate.exception.ViolatedConstraintNameExtracter;
import org.hibernate.exception.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.JDBCExceptionHelper;

/**
 * A dialect for Oracle 8i.
 *
 * @author Steve Ebersole
 */
public class Oracle8iDialect extends Dialect {

	public Oracle8iDialect() {
		super();
		registerCharacterTypeMappings();
		registerNumericTypeMappings();
		registerDateTimeTypeMappings();
		registerLargeObjectTypeMappings();

		registerReverseHibernateTypeMappings();

		registerFunctions();

		registerDefaultProperties();
	}

	protected void registerCharacterTypeMappings() {
		registerColumnType( Types.CHAR, "char(1)" );
		registerColumnType( Types.VARCHAR, 4000, "varchar2($l)" );
		registerColumnType( Types.VARCHAR, "long" );
	}

	protected void registerNumericTypeMappings() {
		registerColumnType( Types.BIT, "number(1,0)" );
		registerColumnType( Types.BIGINT, "number(19,0)" );
		registerColumnType( Types.SMALLINT, "number(5,0)" );
		registerColumnType( Types.TINYINT, "number(3,0)" );
		registerColumnType( Types.INTEGER, "number(10,0)" );

		registerColumnType( Types.FLOAT, "float" );
		registerColumnType( Types.DOUBLE, "double precision" );
		registerColumnType( Types.NUMERIC, "number($p,$s)" );
		registerColumnType( Types.DECIMAL, "number($p,$s)" );
	}

	protected void registerDateTimeTypeMappings() {
		registerColumnType( Types.DATE, "date" );
		registerColumnType( Types.TIME, "date" );
		registerColumnType( Types.TIMESTAMP, "date" );
	}

	protected void registerLargeObjectTypeMappings() {
		registerColumnType( Types.VARBINARY, 2000, "raw($l)" );
		registerColumnType( Types.VARBINARY, "long raw" );

		registerColumnType( Types.BLOB, "blob" );
		registerColumnType( Types.CLOB, "clob" );
	}

	protected void registerReverseHibernateTypeMappings() {
	}

	protected void registerFunctions() {
		registerFunction( "abs", new StandardSQLFunction("abs") );
		registerFunction( "sign", new StandardSQLFunction("sign", Hibernate.INTEGER) );

		registerFunction( "acos", new StandardSQLFunction("acos", Hibernate.DOUBLE) );
		registerFunction( "asin", new StandardSQLFunction("asin", Hibernate.DOUBLE) );
		registerFunction( "atan", new StandardSQLFunction("atan", Hibernate.DOUBLE) );
		registerFunction( "cos", new StandardSQLFunction("cos", Hibernate.DOUBLE) );
		registerFunction( "cosh", new StandardSQLFunction("cosh", Hibernate.DOUBLE) );
		registerFunction( "exp", new StandardSQLFunction("exp", Hibernate.DOUBLE) );
		registerFunction( "ln", new StandardSQLFunction("ln", Hibernate.DOUBLE) );
		registerFunction( "sin", new StandardSQLFunction("sin", Hibernate.DOUBLE) );
		registerFunction( "sinh", new StandardSQLFunction("sinh", Hibernate.DOUBLE) );
		registerFunction( "stddev", new StandardSQLFunction("stddev", Hibernate.DOUBLE) );
		registerFunction( "sqrt", new StandardSQLFunction("sqrt", Hibernate.DOUBLE) );
		registerFunction( "tan", new StandardSQLFunction("tan", Hibernate.DOUBLE) );
		registerFunction( "tanh", new StandardSQLFunction("tanh", Hibernate.DOUBLE) );
		registerFunction( "variance", new StandardSQLFunction("variance", Hibernate.DOUBLE) );

		registerFunction( "round", new StandardSQLFunction("round") );
		registerFunction( "trunc", new StandardSQLFunction("trunc") );
		registerFunction( "ceil", new StandardSQLFunction("ceil") );
		registerFunction( "floor", new StandardSQLFunction("floor") );

		registerFunction( "chr", new StandardSQLFunction("chr", Hibernate.CHARACTER) );
		registerFunction( "initcap", new StandardSQLFunction("initcap") );
		registerFunction( "lower", new StandardSQLFunction("lower") );
		registerFunction( "ltrim", new StandardSQLFunction("ltrim") );
		registerFunction( "rtrim", new StandardSQLFunction("rtrim") );
		registerFunction( "soundex", new StandardSQLFunction("soundex") );
		registerFunction( "upper", new StandardSQLFunction("upper") );
		registerFunction( "ascii", new StandardSQLFunction("ascii", Hibernate.INTEGER) );
		registerFunction( "length", new StandardSQLFunction("length", Hibernate.LONG) );

		registerFunction( "to_char", new StandardSQLFunction("to_char", Hibernate.STRING) );
		registerFunction( "to_date", new StandardSQLFunction("to_date", Hibernate.TIMESTAMP) );

		registerFunction( "current_date", new NoArgSQLFunction("current_date", Hibernate.DATE, false) );
		registerFunction( "current_time", new NoArgSQLFunction("current_timestamp", Hibernate.TIME, false) );
		registerFunction( "current_timestamp", new NoArgSQLFunction("current_timestamp", Hibernate.TIMESTAMP, false) );

		registerFunction( "last_day", new StandardSQLFunction("last_day", Hibernate.DATE) );
		registerFunction( "sysdate", new NoArgSQLFunction("sysdate", Hibernate.DATE, false) );
		registerFunction( "systimestamp", new NoArgSQLFunction("systimestamp", Hibernate.TIMESTAMP, false) );
		registerFunction( "uid", new NoArgSQLFunction("uid", Hibernate.INTEGER, false) );
		registerFunction( "user", new NoArgSQLFunction("user", Hibernate.STRING, false) );

		registerFunction( "rowid", new NoArgSQLFunction("rowid", Hibernate.LONG, false) );
		registerFunction( "rownum", new NoArgSQLFunction("rownum", Hibernate.LONG, false) );

		// Multi-param string dialect functions...
		registerFunction( "concat", new VarArgsSQLFunction(Hibernate.STRING, "", "||", "") );
		registerFunction( "instr", new StandardSQLFunction("instr", Hibernate.INTEGER) );
		registerFunction( "instrb", new StandardSQLFunction("instrb", Hibernate.INTEGER) );
		registerFunction( "lpad", new StandardSQLFunction("lpad", Hibernate.STRING) );
		registerFunction( "replace", new StandardSQLFunction("replace", Hibernate.STRING) );
		registerFunction( "rpad", new StandardSQLFunction("rpad", Hibernate.STRING) );
		registerFunction( "substr", new StandardSQLFunction("substr", Hibernate.STRING) );
		registerFunction( "substrb", new StandardSQLFunction("substrb", Hibernate.STRING) );
		registerFunction( "translate", new StandardSQLFunction("translate", Hibernate.STRING) );

		registerFunction( "substring", new StandardSQLFunction( "substr", Hibernate.STRING ) );
		registerFunction( "locate", new SQLFunctionTemplate( Hibernate.INTEGER, "instr(?2,?1)" ) );
		registerFunction( "bit_length", new SQLFunctionTemplate( Hibernate.INTEGER, "vsize(?1)*8" ) );
		registerFunction( "coalesce", new NvlFunction() );

		// Multi-param numeric dialect functions...
		registerFunction( "atan2", new StandardSQLFunction("atan2", Hibernate.FLOAT) );
		registerFunction( "log", new StandardSQLFunction("log", Hibernate.INTEGER) );
		registerFunction( "mod", new StandardSQLFunction("mod", Hibernate.INTEGER) );
		registerFunction( "nvl", new StandardSQLFunction("nvl") );
		registerFunction( "nvl2", new StandardSQLFunction("nvl2") );
		registerFunction( "power", new StandardSQLFunction("power", Hibernate.FLOAT) );

		// Multi-param date dialect functions...
		registerFunction( "add_months", new StandardSQLFunction("add_months", Hibernate.DATE) );
		registerFunction( "months_between", new StandardSQLFunction("months_between", Hibernate.FLOAT) );
		registerFunction( "next_day", new StandardSQLFunction("next_day", Hibernate.DATE) );

		registerFunction( "str", new StandardSQLFunction("to_char", Hibernate.STRING) );
	}

	protected void registerDefaultProperties() {
		getDefaultProperties().setProperty( Environment.USE_STREAMS_FOR_BINARY, "true" );
		getDefaultProperties().setProperty( Environment.STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE );
		// Oracle driver reports to support getGeneratedKeys(), but they only
		// support the version taking an array of the names of the columns to
		// be returned (via its RETURNING clause).  No other driver seems to
		// support this overloaded version.
		getDefaultProperties().setProperty( Environment.USE_GET_GENERATED_KEYS, "false" );
	}


	// features which change between 8i, 9i, and 10g ~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Support for the oracle proprietary join syntax...
	 *
	 * @return The orqacle join fragment
	 */
	public JoinFragment createOuterJoinFragment() {
		return new OracleJoinFragment();
	}

	/**
	 * Map case support to the Oracle DECODE function.  Oracle did not
	 * add support for CASE until 9i.
	 *
	 * @return The oracle CASE -> DECODE fragment
	 */
	public CaseFragment createCaseFragment() {
		return new DecodeCaseFragment();
	}

	public String getLimitString(String sql, boolean hasOffset) {
		sql = sql.trim();
		boolean isForUpdate = false;
		if ( sql.toLowerCase().endsWith(" for update") ) {
			sql = sql.substring( 0, sql.length()-11 );
			isForUpdate = true;
		}

		StringBuffer pagingSelect = new StringBuffer( sql.length()+100 );
		if (hasOffset) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		}
		else {
			pagingSelect.append("select * from ( ");
		}
		pagingSelect.append(sql);
		if (hasOffset) {
			pagingSelect.append(" ) row_ ) where rownum_ <= ? and rownum_ > ?");
		}
		else {
			pagingSelect.append(" ) where rownum <= ?");
		}

		if ( isForUpdate ) {
			pagingSelect.append( " for update" );
		}

		return pagingSelect.toString();
	}

	/**
	 * Allows access to the basic {@link Dialect#getSelectClauseNullString}
	 * implementation...
	 *
	 * @param sqlType The {@link java.sql.Types} mapping type code
	 * @return The appropriate select cluse fragment
	 */
	public String getBasicSelectClauseNullString(int sqlType) {
		return super.getSelectClauseNullString( sqlType );
	}

	public String getSelectClauseNullString(int sqlType) {
		switch(sqlType) {
			case Types.VARCHAR:
			case Types.CHAR:
				return "to_char(null)";
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIME:
				return "to_date(null)";
			default:
				return "to_number(null)";
		}
	}

	public String getCurrentTimestampSelectString() {
		return "select sysdate from dual";
	}

	public String getCurrentTimestampSQLFunctionName() {
		return "sysdate";
	}


	// features which remain constant across 8i, 9i, and 10g ~~~~~~~~~~~~~~~~~~

	public String getAddColumnString() {
		return "add";
	}

	public String getSequenceNextValString(String sequenceName) {
		return "select " + getSelectSequenceNextValString( sequenceName ) + " from dual";
	}

	public String getSelectSequenceNextValString(String sequenceName) {
		return sequenceName + ".nextval";
	}

	public String getCreateSequenceString(String sequenceName) {
		return "create sequence " + sequenceName; //starts with 1, implicitly
	}

	public String getDropSequenceString(String sequenceName) {
		return "drop sequence " + sequenceName;
	}

	public String getCascadeConstraintsString() {
		return " cascade constraints";
	}

	public boolean dropConstraints() {
		return false;
	}

	public String getForUpdateNowaitString() {
		return " for update nowait";
	}

	public boolean supportsSequences() {
		return true;
	}

	public boolean supportsPooledSequences() {
		return true;
	}

	public boolean supportsLimit() {
		return true;
	}

	public String getForUpdateString(String aliases) {
		return getForUpdateString() + " of " + aliases;
	}

	public String getForUpdateNowaitString(String aliases) {
		return getForUpdateString() + " of " + aliases + " nowait";
	}

	public boolean bindLimitParametersInReverseOrder() {
		return true;
	}

	public boolean useMaxForLimit() {
		return true;
	}

	public boolean forUpdateOfColumns() {
		return true;
	}

	public String getQuerySequencesString() {
		return "select sequence_name from user_sequences";
	}

	public String getSelectGUIDString() {
		return "select rawtohex(sys_guid()) from dual";
	}

	public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
        return EXTRACTER;
	}

	private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {

		/**
		 * Extract the name of the violated constraint from the given SQLException.
		 *
		 * @param sqle The exception that was the result of the constraint violation.
		 * @return The extracted constraint name.
		 */
		public String extractConstraintName(SQLException sqle) {
			int errorCode = JDBCExceptionHelper.extractErrorCode(sqle);
			if ( errorCode == 1 || errorCode == 2291 || errorCode == 2292 ) {
				return extractUsingTemplate( "constraint (", ") violated", sqle.getMessage() );
			}
			else if ( errorCode == 1400 ) {
				// simple nullability constraint
				return null;
			}
			else {
				return null;
			}
		}

	};

	// not final-static to avoid possible classcast exceptions if using different oracle drivers.
	int oracletypes_cursor_value = 0;
	public int registerResultSetOutParameter(java.sql.CallableStatement statement,int col) throws SQLException {
		if(oracletypes_cursor_value==0) {
			try {
				Class types = ReflectHelper.classForName("oracle.jdbc.driver.OracleTypes");
				oracletypes_cursor_value = types.getField("CURSOR").getInt(types.newInstance());
			} catch (Exception se) {
				throw new HibernateException("Problem while trying to load or access OracleTypes.CURSOR value",se);
			}
		}
		//	register the type of the out param - an Oracle specific type
		statement.registerOutParameter(col, oracletypes_cursor_value);
		col++;
		return col;
	}

	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		ps.execute();
		return ( ResultSet ) ps.getObject( 1 );
	}

	public boolean supportsUnionAll() {
		return true;
	}

	public boolean supportsCommentOn() {
		return true;
	}

	public boolean supportsTemporaryTables() {
		return true;
	}

	public String generateTemporaryTableName(String baseTableName) {
		String name = super.generateTemporaryTableName(baseTableName);
		return name.length() > 30 ? name.substring( 1, 30 ) : name;
	}

	public String getCreateTemporaryTableString() {
		return "create global temporary table";
	}

	public String getCreateTemporaryTablePostfix() {
		return "on commit delete rows";
	}

	public boolean dropTemporaryTableAfterUse() {
		return false;
	}

	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}


	// Overridden informational metadata ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public boolean supportsEmptyInList() {
		return false;
	}

	public boolean supportsExistsInSelect() {
		return false;
	}

}