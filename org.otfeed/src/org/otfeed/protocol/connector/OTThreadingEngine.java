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

package org.otfeed.protocol.connector;


import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTError;
import org.otfeed.protocol.request.*;
import org.otfeed.protocol.ErrorEnum;
import org.otfeed.protocol.MessageEnum;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.StatusEnum;
import org.otfeed.support.IBufferAllocator;

import java.util.Queue;
import java.util.Map;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.otfeed.protocol.request.AbstractRequest.JobStatus;
import static org.otfeed.protocol.request.Util.readError;
import static org.otfeed.protocol.request.Util.newError;

class OTThreadingEngine {

	private final static int BUFFER_SIZE = 1024;
	
	// submitted request that has not yet been sent out
	private final Queue<AbstractSessionRequest> writeQueue 
			= new ConcurrentLinkedQueue<AbstractSessionRequest>();

	// response frames that were read from the net, but not yet
	// processed
	private final Queue<ByteBuffer> readQueue 
			= new ConcurrentLinkedQueue<ByteBuffer>();

	// task scheduled to be run in the event processing queue
	private final Queue<Runnable> taskQueue 
			= new ConcurrentLinkedQueue<Runnable>();

	// requests that have been sent out but not yet completed.
	private final Map<Integer,AbstractSessionRequest> pendingJobs
			= new ConcurrentHashMap<Integer,AbstractSessionRequest>();

	private final IBufferAllocator allocator;

	private final ISessionStreamerFactory factory;

	private final long heartbeatIntervalMillis;

	private final IConnectionStateListener listener;

	private ISessionStreamer connector = null;

	// holds the earliest error that caused the shutdown
	private final AtomicReference<OTError> error 
				= new AtomicReference<OTError>();

	protected OTThreadingEngine(ISessionStreamerFactory f,
					IBufferAllocator a,
					long hb,
					IConnectionStateListener l) {
		factory                 = f;
		allocator               = a;
		heartbeatIntervalMillis = hb;
		listener                = l;
                          
		isControlRunning.set(true);

		Thread thread = new Thread() {
			@Override
			public void run() { controlThread(); }
		};

		thread.start();
	}

	private static final long SLEEP_DELAY = 1000;

	private final AtomicBoolean isWriteRunning = new AtomicBoolean(false);

	private void checkWriteInterrupted() throws InterruptedException {
		if(!isWriteRunning.get()) throw new InterruptedException();
	}

	private void interruptWriteThread() {
		isWriteRunning.set(false);
		synchronized(isWriteRunning) {
			isWriteRunning.notifyAll();
		}
	}

	private void writeThread() {

		try {
			writeThreadRunner();
		} catch(InterruptedException ex) {
			error.compareAndSet(null, newError("interrupted"));

			interruptControlThread();
		}
	}			

	private void writeThreadRunner() throws InterruptedException {

		ByteBuffer outputBuffer = allocator.allocate(BUFFER_SIZE);

		while(true) {
			checkWriteInterrupted();

			long lastBufferTimestamp = System.currentTimeMillis();

			while(writeQueue.size() == 0) {
				if(System.currentTimeMillis() - lastBufferTimestamp > heartbeatIntervalMillis) {
					break;
				}

				synchronized(isWriteRunning) {
					isWriteRunning.wait(SLEEP_DELAY);
				}

				checkWriteInterrupted();
			}

			AbstractSessionRequest job = writeQueue.poll();
// System.out.println("write thread received request: " + job);

			outputBuffer.clear();
			if(job != null) {
				pendingJobs.put(job.getRequestId(), job);

				job.setSessionId(connector.getSessionId());
				job.writeRequest(outputBuffer);
			} else {
// System.out.println("sending heartbeat");
				HeartbeatRequest hb = new HeartbeatRequest(0);

				hb.writeRequest(outputBuffer);
			}
			outputBuffer.flip();

			try {
				connector.write(outputBuffer);
			} catch(IOException ex) {
				// notify control thread and get out
				error.compareAndSet(null, newError(
					"io error in writer: " + ex.getMessage()));
				throw new InterruptedException();
			}
		}
	}

	private final AtomicBoolean isReadRunning = new AtomicBoolean(false);

	private void checkReadInterrupted() throws InterruptedException {
		if(!isReadRunning.get()) throw new InterruptedException();
	}
	// read thread does not call wait(). hence no need to signal anything
	// just make user connector is closed, this should interrupt read thread
	// when blocked at network read.
	private void interruptReadThread() {
		isReadRunning.set(false);
	}

	private void readThread() {
		try {
			readThreadRunner();
		} catch(InterruptedException ex) {
			error.compareAndSet(null, newError("interrupted"));

			interruptControlThread();
		}
	}			

	private void readThreadRunner() throws InterruptedException {

		while(true) {
			checkReadInterrupted();

			try {
				ByteBuffer in = connector.read();
				readQueue.offer(in);
				synchronized(isControlRunning) {
					isControlRunning.notifyAll();
				}
			} catch(IOException ex) {
				error.compareAndSet(null, newError(
					"io exception in reader: " + ex.getMessage()));

				throw new InterruptedException();
			}
		}
	}

	// soft interrupt for control thread, and event to sleep on
	private final AtomicBoolean isControlRunning = new AtomicBoolean(false);

	private void checkControlInterrupted() throws InterruptedException {
		if(!isControlRunning.get()) throw new InterruptedException();
	}

	// soft interrupt of control thread
	private void interruptControlThread() {
		isControlRunning.set(false);
		synchronized(isControlRunning) {
			isControlRunning.notifyAll();
		}
	}

	private void controlRunner() throws InterruptedException {

		try {
			connector = factory.connect(listener);
		} catch(LoginFailureException ex) {
			// login errors are fatal
			error.compareAndSet(null, ex.error);
			throw new InterruptedException();
		} catch(Throwable ex) {
			// treat as login error
			error.compareAndSet(null, newError(
					"connection failed: " + ex.getMessage()));
			throw new InterruptedException();
		}

		Thread writeThread = new Thread() {
			@Override
			public void run() { writeThread(); }
		};

		Thread readThread = new Thread() {
			@Override
			public void run() { readThread(); }
		};

		isWriteRunning.set(true);
		writeThread.start();

		isReadRunning.set(true);
		readThread.start();

		try {
			while(true) {
				checkControlInterrupted();

				while(taskQueue.size() == 0 && readQueue.size() == 0) {
					synchronized(isControlRunning) {
						isControlRunning.wait();
					}

					checkControlInterrupted();
				}

				Runnable task;
				while((task = taskQueue.poll()) != null) {
					task.run();
					checkControlInterrupted();
				} 

				ByteBuffer buffer;
				while((buffer = readQueue.poll()) != null) {
					handleMessage(buffer);
					checkControlInterrupted();
					while((task = taskQueue.poll()) != null) {
						task.run();
						checkControlInterrupted();
					} 
				}
			}

		} finally {

			// stop write thread
			interruptWriteThread();

			interruptReadThread();
					
			connector.close();

			// wait till they really exit
			readThread.join();
			writeThread.join();
		}
	}

	private void controlThread() {

		try {
			controlRunner();
			shutdown();
		} catch(InterruptedException ex) {
		}

		error.compareAndSet(null, newError("unexpected shutdown"));

		for(AbstractRequest r : writeQueue) {
			r.fireCompleted(error.get());
		}

		writeQueue.clear();
		
		for(AbstractRequest r : pendingJobs.values()) {
			r.fireCompleted(error.get());
		}
		
		pendingJobs.clear();

		listener.onError(error.get());

		// notify threads waiting for shutdown
		isFinished.set(true);
		synchronized(isFinished) {
			isFinished.notifyAll();
		}
	}

	private final AtomicBoolean isShutdown = new AtomicBoolean(false);

	public void shutdown() {

		if(isShutdown.getAndSet(true)) {
			return;
		}

		error.compareAndSet(null,
				newError(ErrorEnum.E_OTFEED_OK, "shutdown"));

		interruptControlThread();

		factory.shutdown();
	}

	private final AtomicBoolean isFinished = new AtomicBoolean(false);

	public boolean isFinished() { 
		return isFinished.get(); 
	}

	public void waitForCompletion() {

		try {

			synchronized(isFinished) {
				while(!isFinished.get()) {
						isFinished.wait();
				}
			}
		} catch(InterruptedException ex) {
			// ?? fixme
			shutdown();
		}
	}

	public boolean waitForCompletion(long millis) {

		long target = System.currentTimeMillis() + millis;

		try {
			synchronized(isFinished) {
				while(!isFinished.get()) {
					long now = System.currentTimeMillis();

					if(now <= target) break;

					isFinished.wait(target - now);
				}
			} 
		} catch(InterruptedException ex) {
			// ?? fixme
			shutdown();
		}

		return isFinished.get();
	}

	private void handleMessage(ByteBuffer in) {
		Header header = new Header(in);

		if(header.getMessage() != MessageEnum.RESPONSE) {
			return;
		}
		if(header.getStatus() == null) {
			return;
		}
		if(header.getCommand() == null) {
			return;
		}

		if(header.getStatus() != StatusEnum.OK) {

			OTError error = readError(header.getRequestId(), in);

			AbstractRequest job = pendingJobs.remove(header.getRequestId());
			if(job == null) {
				// oops, unknown request id. ignore?
				return;
			}

			job.fireCompleted(error);
		} else {

			AbstractRequest job = pendingJobs.get(header.getRequestId());
			if(job == null) {
				// oops, unknown request id. ignore?
// System.out.println("ignoring unknown request id: " + header);
				return;
			}

			try {
				JobStatus status = job.handleMessage(header, in);
				if(status == JobStatus.FINISHED) {
					pendingJobs.remove(header.getRequestId());
					job.fireCompleted(null);
				}
			} catch(Exception ex) {
				// error parsing response buffer
				// log it and move on to the next buffer

ex.printStackTrace(System.out);
System.out.println(ex);
				pendingJobs.remove(header.getRequestId());
				job.fireCompleted(Util.newError(ex.getMessage()));
			}
		}
	}

	/**
	 * Utility methods for subclasses: submits request.
	 */
	void submit(AbstractSessionRequest request) {
		if(error.get() != null) {
			throw new IllegalStateException("shutting down");
		}

		writeQueue.offer(request);
		synchronized(isWriteRunning) {
			isWriteRunning.notifyAll();
		}
	}

	/**
	 * Utility method for subclasses: cancels request.
	 */
	void cancel(int requestId, final AbstractSessionRequest request) {

		CommandEnum command = request.getCancelCommand();

		if(command != null) {
			submit(new CancelRequest(command, requestId,
						request.getRequestId(), null));
		}

		runInEventThread(new Runnable() {
			public void run() {
				AbstractSessionRequest target
					= pendingJobs.remove(request.getRequestId());
				if(target != null) {
					target.fireCompleted(newError(ErrorEnum.E_OTFEED_CANCELLED, "cancelled"));
				}
			}
		});
	}

	public void runInEventThread(Runnable runnable) {
		if(isShutdown.get()) {
			throw new IllegalStateException("shutdown");
		}
		taskQueue.offer(runnable);
		synchronized(isControlRunning) {
			isControlRunning.notifyAll();
		}
	}
}
