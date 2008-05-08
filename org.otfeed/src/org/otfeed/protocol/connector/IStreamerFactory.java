package org.otfeed.protocol.connector;

import java.io.IOException;

/**
 * Low level synchronous client socket connection interface.
 */
public interface IStreamerFactory {
	/**
	 * Creates a connection (synchronously).
	 */
	public IStreamer connect(String host, int port) throws IOException;
}
