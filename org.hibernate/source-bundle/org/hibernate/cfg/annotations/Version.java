//$Id: $
package org.hibernate.cfg.annotations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Emmanuel Bernard
 */
public class Version {
	public static final String VERSION = "3.3.1.GA";
	private static Log log = LogFactory.getLog( Version.class );

	static {
		log.info( "Hibernate Annotations " + VERSION );
	}

	public static void touch() {
	}
}
