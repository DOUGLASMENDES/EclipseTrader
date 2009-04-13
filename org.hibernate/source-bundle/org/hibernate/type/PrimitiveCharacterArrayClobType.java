//$Id: PrimitiveCharacterArrayClobType.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.type;


/**
 * Map a char[] to a Clob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveCharacterArrayClobType extends CharacterArrayClobType {
	public Class returnedClass() {
		return char[].class;
	}
}
