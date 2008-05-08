package org.otfeed.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Allocates byte-reverse ByteBuffers.
 */
public class ByteReverseBufferAllocator implements IBufferAllocator {

	public ByteBuffer allocate(int size) {
		return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
	}
}
