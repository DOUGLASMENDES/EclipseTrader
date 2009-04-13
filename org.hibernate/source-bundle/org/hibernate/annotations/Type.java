//$Id: Type.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * hibernate type
 *
 * @author Emmanuel Bernard
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface Type {
	String type();

	Parameter[] parameters() default {};
}