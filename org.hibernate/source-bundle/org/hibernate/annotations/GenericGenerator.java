//$Id: GenericGenerator.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Generator annotation describing any kind of Hibernate
 * generator in a detyped manner
 *
 * @author Emmanuel Bernard
 */
@Target({PACKAGE, TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface GenericGenerator {
	/**
	 * unique generator name
	 */
	String name();
	/**
	 * Generator strategy either a predefined Hibernate
	 * strategy or a fully qualified class name.
	 */
	String strategy();
	/**
	 * Optional generator parameters
	 */
	Parameter[] parameters() default {};
}
