//$Id: DerbyCaseFragment.java 4631 2004-09-29 12:29:05Z pgmjsd $
package org.hibernate.sql;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Simon Johnston
 * @see org.hibernate.dialect.DerbyDialect
 */
public class DerbyCaseFragment extends CaseFragment {

	/**
	 * From http://www.jroller.com/comments/kenlars99/Weblog/cloudscape_soon_to_be_derby
	 * <p/>
	 * The problem we had, was when Hibernate does a select with a case statement, for joined subclasses.
	 * This seems to be because there was no else at the end of the case statement (other dbs seem to not mind).
	 */
	public String toFragmentString() {
		StringBuffer buf = new StringBuffer( cases.size() * 15 + 10 );
		buf.append( "case" ); 								//$NON-NLS-1
		Iterator iter = cases.entrySet().iterator();
		while ( iter.hasNext() ) {
			Map.Entry me = ( Map.Entry ) iter.next();
			buf.append( " when " )//$NON-NLS-1
					.append( me.getKey() )
					.append( " is not null then " )//$NON-NLS-1
					.append( me.getValue() );
		}
		// null is not considered the same type as Integer.
		buf.append( " else -1" );								//$NON-NLS-1
		buf.append( " end" );									//$NON-NLS-1
		if ( returnColumnName != null ) {
			buf.append( " as " )//$NON-NLS-1
					.append( returnColumnName );
		}
		return buf.toString();
	}
}
