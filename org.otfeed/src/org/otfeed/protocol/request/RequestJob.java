/**
 * Copyright 2007 Mike Kroutikov.
 *
 * This program is free software; you can redistribute it and/or modify
 *   it under the terms of the Lesser GNU General Public License as 
 *   published by the Free Software Foundation; either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Lesser GNU General Public License for more details.
 *
 *   You should have received a copy of the Lesser GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.otfeed.protocol.request;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.OTError;

/**
 * Common base for jobs (requests): encapsulates
 * job life cycle.
 */
class RequestJob {

	private final ICompletionDelegate completionDelegate;

	/**
	 * Creates new object.
	 * @param val completion delegate.
	 */
	RequestJob(ICompletionDelegate val) {
		completionDelegate = val;
	}

	private final AtomicReference<OTError> errorHolder = new AtomicReference<OTError>();
	
	public final OTError getError() {
		return errorHolder.get();
	}
	
	private final AtomicBoolean isCompleted = new AtomicBoolean(false);

	/**
	 * Internal method: used to wake up threads sleeping 
	 * in <code>waitForCompletion()</code>.
	 */
	public final void fireCompleted(OTError error) {
		
		if(!errorHolder.compareAndSet(null, error)) {
			return;
		}

		if(isCompleted.getAndSet(true)) {
			return;
		}
		
		synchronized(isCompleted) {
			isCompleted.notifyAll();
		}
		
		if(completionDelegate != null) {
			completionDelegate.onDataEnd(error);
		}
	}
	
	public final boolean isCompleted() {
		return isCompleted.get();
	}

	/**
	 * Convenience method: blocks calling thread until
	 * request finishes, or the specified number of milliseconds elapses.
	 * Note that if request was not submitted,
	 * this method returns immediately with no error.
	 *
	 * @param millis how long to wait for completion.
	 */
	public final boolean waitForCompletion(long millis) {

		long target = System.currentTimeMillis() + millis;

		try {
			while(!isCompleted.get()) {
				long towait = target - System.currentTimeMillis();
				if(towait < 0) towait = 0;
				if(towait == 0) {
					break;
				}

				synchronized(isCompleted) {
					if(isCompleted.get()) break;
					isCompleted.wait(towait);
				}
			} 
		} catch(InterruptedException ex) {
			// not sure what I am supposed to do here
		}

		return isCompleted.get();
	}

	/**
	 * Convenience method: blocks calling thread until
	 * request finishes. Note that if request was not submitted,
	 * this method returns immediately with no error.
	 */
	public final void waitForCompletion() {

		try {
			while(!isCompleted.get()) {
				synchronized(isCompleted) { 
					if(isCompleted.get()) break;
					isCompleted.wait();
				}
			}
		} catch(InterruptedException ex) {
			// not sure what I am supposed to do here
		}
	}
}

