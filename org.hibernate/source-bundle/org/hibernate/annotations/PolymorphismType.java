//$Id: PolymorphismType.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

/**
 * Type of avaliable polymorphism for a particular entity
 *
 * @author Emmanuel Bernard
 */
public enum PolymorphismType {
	/**
	 * default, this entity is retrieved if any of its super entity is asked
	 */
	IMPLICIT,
	/**
	 * this entity is retrived only if explicitly asked
	 */
	EXPLICIT
}