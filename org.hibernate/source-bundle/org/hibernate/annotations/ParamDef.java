//$Id: ParamDef.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * A parameter definition
 *
 * @author Emmanuel Bernard
 */
@Target({})
@Retention(RUNTIME)
public @interface ParamDef {
	String name();

	String type();
}
