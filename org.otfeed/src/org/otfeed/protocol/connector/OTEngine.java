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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.*;
import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTError;
import org.otfeed.protocol.ICommand;
import org.otfeed.protocol.request.AbstractSessionRequest;
import org.otfeed.protocol.request.BookStreamExRequest;
import org.otfeed.protocol.request.DividendRequest;
import org.otfeed.protocol.request.EquityInitRequest;
import org.otfeed.protocol.request.HistBookRequest;
import org.otfeed.protocol.request.HistDataRequest;
import org.otfeed.protocol.request.HistTicksRequest;
import org.otfeed.protocol.request.ListExchangesRequest;
import org.otfeed.protocol.request.ListSymbolsExRequest;
import org.otfeed.protocol.request.OptionChainRequest;
import org.otfeed.protocol.request.OptionChainSnapshotRequest;
import org.otfeed.protocol.request.OptionChainWithSnapshotRequest;
import org.otfeed.protocol.request.OptionInitRequest;
import org.otfeed.protocol.request.SnapshotRequest;
import org.otfeed.protocol.request.SplitRequest;
import org.otfeed.protocol.request.TickStreamExRequest;
import org.otfeed.protocol.request.TickStreamWithSnapshotRequest;
import org.otfeed.protocol.request.TodaysOHLRequest;
import org.otfeed.support.IBufferAllocator;

public class OTEngine extends OTThreadingEngine implements IConnection {

	private interface IRequestPreparer {
		IRequest prepareRequest(ICommand r);
	}
	
	private final Map<Class<?>,IRequestPreparer> commandMap
		= new HashMap<Class<?>,IRequestPreparer>();

	private class RequestHandler implements IRequest {

		private final AbstractSessionRequest request;

		private final AtomicBoolean isActiveFlag
					 = new AtomicBoolean(false);

		private RequestHandler(AbstractSessionRequest r) { 
			request = r;
		}

		public boolean isCompleted() { 
			return request.isCompleted();
		}
		
		public OTError getError() {
			return request.getError();
		}

		public void submit() {
			if(isActiveFlag.getAndSet(true)) {
				return; // already started
			}

			OTEngine.this.submit(request);
		}

		public void cancel() {

			if(!isActiveFlag.getAndSet(false)) {
				return; // not running
			}

			OTEngine.this.cancel(
				generateRequestId(),
				request);
		}

		public void waitForCompletion() {
			request.waitForCompletion();
		}

		public boolean waitForCompletion(long millis) {
			return request.waitForCompletion(millis);
		}
	}

	private final AtomicInteger requestCounter = new AtomicInteger(0);

	private int generateRequestId() {
		return requestCounter.incrementAndGet();
	}

	private IRequest newRequest(AbstractSessionRequest request) {

		return new RequestHandler(request);
	}

	public OTEngine(ISessionStreamerFactory factory, IBufferAllocator allocator, long heartbeatMillis, IConnectionStateListener listener) {
		super(factory, allocator, heartbeatMillis, listener);

		// configure request maps
		
		commandMap.put(BookStreamCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				BookStreamCommand c = (BookStreamCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new BookStreamExRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getOrderDelegate(), 
					c.getChangeDelegate(), 
					c.getReplaceDelegate(),
					c.getCancelDelegate(),
					c.getPurgeDelegate(),
					c.getExecuteDelegate(),
					c.getDeleteDelegate(),
					c.getPriceLevelDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});
		
		commandMap.put(DividendsCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				DividendsCommand c = (DividendsCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new DividendRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getStartDate(),
					c.getEndDate(),
					c.getDataDelegate(),
					c.getCompletionDelegate());
				return newRequest(request);
			}
		});

		commandMap.put(EquityInitCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				EquityInitCommand c = (EquityInitCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new EquityInitRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getDataDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});
		
		commandMap.put(HistBooksCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				HistBooksCommand c = (HistBooksCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new HistBookRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getStartDate(),
					c.getEndDate(),
					c.getOrderDelegate(), 
					c.getChangeDelegate(), 
					c.getReplaceDelegate(),
					c.getCancelDelegate(),
					c.getPurgeDelegate(),
					c.getExecuteDelegate(),
					c.getDeleteDelegate(),
					c.getPriceLevelDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(HistDataCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				HistDataCommand c = (HistDataCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new HistDataRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getStartDate(),
					c.getEndDate(),
					c.getAggregationSpan(),
					c.getDataDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(HistTicksCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				HistTicksCommand c = (HistTicksCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new HistTicksRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getStartDate(),
					c.getEndDate(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(ListExchangesCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				ListExchangesCommand c = (ListExchangesCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new ListExchangesRequest(reqId, 
						c.getDataDelegate(), 
						c.getCompletionDelegate());
				return newRequest(request);
			}
		});
		
		commandMap.put(ListSymbolsCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				ListSymbolsCommand c = (ListSymbolsCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new ListSymbolsExRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCodePattern(),
					c.getTypes(),
					c.getMatchStyle(),
					c.getDataDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});
		
		commandMap.put(OptionChainCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				OptionChainCommand c = (OptionChainCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new OptionChainRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getExpiration(),
					c.getStrike(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(OptionChainSnapshotCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				OptionChainSnapshotCommand c = (OptionChainSnapshotCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new OptionChainSnapshotRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getExpiration(),
					c.getStrike(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});
		
		commandMap.put(OptionInitCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				OptionInitCommand c = (OptionInitCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new OptionInitRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getExpiration(),
					c.getStrike(),
					c.getDataDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(SnapshotCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				SnapshotCommand c = (SnapshotCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new SnapshotRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});
		
		commandMap.put(SplitsCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				SplitsCommand c = (SplitsCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new SplitRequest(reqId, 
						c.getExchangeCode(),
						c.getSymbolCode(),
						c.getStartDate(),
						c.getEndDate(),
						c.getDataDelegate(),
						c.getCompletionDelegate());

				return newRequest(request);
			}
		});
		
		commandMap.put(TickStreamCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				TickStreamCommand c = (TickStreamCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new TickStreamExRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(TodaysOHLCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				TodaysOHLCommand c = (TodaysOHLCommand) command;
				int reqId = generateRequestId();

				AbstractSessionRequest request = new TodaysOHLRequest(reqId, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getDataDelegate(),
					c.getCompletionDelegate());

				return newRequest(request);
			}
		});

		commandMap.put(TickStreamWithSnapshotCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				TickStreamWithSnapshotCommand c = (TickStreamWithSnapshotCommand) command;

				TickStreamWithSnapshotRequest request = new TickStreamWithSnapshotRequest(OTEngine.this, 
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());
				request.prepareRequest();
				return request;
			}
		});

		commandMap.put(OptionChainWithSnapshotCommand.class, new IRequestPreparer() {

			public IRequest prepareRequest(ICommand command) {
				OptionChainWithSnapshotCommand c = (OptionChainWithSnapshotCommand) command;

				OptionChainWithSnapshotRequest request = new OptionChainWithSnapshotRequest(OTEngine.this,
					c.getExchangeCode(),
					c.getSymbolCode(),
					c.getExpiration(),
					c.getStrike(),
					c.getVolumeStyle(),
					c.getQuoteDelegate(),
					c.getTradeDelegate(),
					c.getMmQuoteDelegate(),
					c.getBboDelegate(),
					c.getCompletionDelegate());
				request.prepareRequest();
				return request;
			}
		});
	}
	
	public IRequest prepareRequest(ICommand command) {

		IRequestPreparer p = commandMap.get(command.getClass());
		if(p == null) {
			throw new IllegalArgumentException("Unknown (or unregistered) command: " + command.getClass());
		}
		
		return p.prepareRequest(command);
	}
}
