//$Id: DiscriminatorFormula.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Discriminator formula
 * To be placed at the root entity.
 *
 * @author Emmanuel Bernard
 * @see Formula
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface DiscriminatorFormula {
	String value();
}
