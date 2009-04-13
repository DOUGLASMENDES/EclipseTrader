//$Id: FilterDef.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Filter definition
 *
 * @author Matthew Inger
 * @author Emmanuel Bernard
 */
@Target({TYPE, PACKAGE})
@Retention(RUNTIME)
public @interface FilterDef {
	String name();

	String defaultCondition() default "";

	ParamDef[] parameters() default {};
}
