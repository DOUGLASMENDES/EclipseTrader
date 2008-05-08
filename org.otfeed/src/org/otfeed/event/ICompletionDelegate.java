package org.otfeed.event;

/**
 * Defines contract for the listeners that want to receive
 * end-of-stream notification. After such a notification 
 * is recevied, no more stream data can arrive.
 */
public interface ICompletionDelegate {

	/**
	 * Handles end-of-stream event.
	 * <p/>
	 * If data stream is ended with error (because server
	 * returned an error, or driver encountered an error condition),
	 * <code>error</code> parameter will be not null. On normal stream
	 * completion, <code>error</code> parameter is null.
	 *
	 * <p/>
	 * It is guarnteed, that this method will be called before
	 * request completes.
	 * 
	 * @param error error code, if stream completion was abnormal. 
	 * Null otherwise.
	 */
	public void onDataEnd(OTError error);
}
