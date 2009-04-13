//$Id: Check.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Arbitrary SQL check constraints which can be defined at the class,
 * property or collection level
 *
 * @author Emmanuel Bernard
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface Check {
	String constraints();
}
