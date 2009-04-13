//$Id: Parameter.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Parameter (basically key/value pattern)
 *
 * @author Emmanuel Bernard
 */
@Target({})
@Retention(RUNTIME)
public @interface Parameter {
	String name();

	String value();
}
