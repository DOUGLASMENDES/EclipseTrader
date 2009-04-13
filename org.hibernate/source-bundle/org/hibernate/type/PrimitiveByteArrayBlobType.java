//$Id: PrimitiveByteArrayBlobType.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.type;

/**
 * Map a byte[] to a Blob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveByteArrayBlobType extends ByteArrayBlobType {
	public Class getReturnedClass() {
		return byte[].class;
	}

	protected Object wrap(byte[] bytes) {
		return bytes;
	}

	protected byte[] unWrap(Object bytes) {
		return (byte[]) bytes;
	}
}
