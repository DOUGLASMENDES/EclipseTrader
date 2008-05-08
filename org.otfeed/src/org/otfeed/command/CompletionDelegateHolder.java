package org.otfeed.command;

import org.otfeed.event.ICompletionDelegate;

/**
 * Common superclass for all commands.
 * Holds optional completionDelegate reference.
 */
class CompletionDelegateHolder {

	private ICompletionDelegate completionDelegate;

	/**
	 * Option delegate to watch stream completion event.
	 * 
	 * @return delegate.
	 */
	public ICompletionDelegate getCompletionDelegate() {
		return completionDelegate;
	}
	
	/**
	 * Sets completion delegate.
	 * 
	 * @param val delegate.
	 */
	public void setCompletionDelegate(ICompletionDelegate val) {
		completionDelegate = val;
	}
}
