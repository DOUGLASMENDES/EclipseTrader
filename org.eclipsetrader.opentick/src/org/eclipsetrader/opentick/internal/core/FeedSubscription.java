/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.opentick.internal.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.opentick.internal.OTActivator;
import org.eclipsetrader.opentick.internal.core.repository.IdentifierType;
import org.eclipsetrader.opentick.internal.core.repository.PriceDataType;
import org.otfeed.IRequest;
import org.otfeed.command.AggregationSpan;
import org.otfeed.command.HistDataCommand;
import org.otfeed.command.TickStreamWithSnapshotCommand;
import org.otfeed.command.TodaysOHLCommand;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTError;
import org.otfeed.event.OTOHLC;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTodaysOHL;
import org.otfeed.event.OTTrade;
import org.otfeed.event.TradeSideEnum;

public class FeedSubscription implements IFeedSubscription {
	private FeedConnector connector;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private IdentifierType identifierType;
	private List<QuoteDelta> deltaList = new ArrayList<QuoteDelta>();
	private int instanceCount = 0;

	private IRequest ohlRequest;
	private IRequest quoteStreamRequest;

	private IDataDelegate<OTQuote> quoteDelegate = new IDataDelegate<OTQuote>() {
	    public void onData(OTQuote event) {
			PriceDataType priceData = identifierType.getPriceData();
			IQuote oldValue = identifierType.getQuote();
	    	priceData.setBid(event.getBidPrice());
	    	priceData.setBidSize(new Long(event.getBidSize()));
	    	priceData.setAsk(event.getAskPrice());
	    	priceData.setAskSize(new Long(event.getAskSize()));
	    	IQuote newValue = new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize());
			if (!newValue.equals(oldValue)) {
				addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setQuote(newValue);
			}
	    }
	};

	private IDataDelegate<OTTrade> tradeDelegate = new IDataDelegate<OTTrade>() {
        public void onData(OTTrade event) {
			PriceDataType priceData = identifierType.getPriceData();
			ITrade oldValue = identifierType.getTrade();
			priceData.setTime(event.getTimestamp());
			priceData.setLast(event.getPrice());
			priceData.setLastSize(new Long(event.getSize()));
			priceData.setVolume(event.getVolume());
			ITrade newValue = new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume());
			if (!newValue.equals(oldValue)) {
				addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setTrade(newValue);
			}
        }
	};

	private IDataDelegate<OTTodaysOHL> ohlDelegate = new IDataDelegate<OTTodaysOHL>() {
        public void onData(OTTodaysOHL event) {
			PriceDataType priceData = identifierType.getPriceData();
			ITodayOHL oldValue = identifierType.getTodayOHL();
			priceData.setOpen(event.getOpenPrice());
			priceData.setHigh(event.getHighPrice());
			priceData.setLow(event.getLowPrice());
			ITodayOHL newValue = new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow());
			if (!newValue.equals(oldValue)) {
				addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setTodayOHL(newValue);
			}
        }
	};

	private IDataDelegate<OTBBO> bboDelegate = new IDataDelegate<OTBBO>() {
        public void onData(OTBBO event) {
			PriceDataType priceData = identifierType.getPriceData();
			IQuote oldValue = identifierType.getQuote();
	    	if (event.getSide() == TradeSideEnum.BUYER) {
				priceData.setBid(event.getPrice());
				priceData.setBidSize(new Long(event.getSize()));
	    	}
	    	else {
				priceData.setAsk(event.getPrice());
				priceData.setAskSize(new Long(event.getSize()));
	    	}
	    	IQuote newValue = new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize());
			if (!newValue.equals(oldValue)) {
				addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setQuote(newValue);
			}
        }
	};

	public FeedSubscription(FeedConnector connector, IdentifierType identifierType) {
		this.connector = connector;
		this.identifierType = identifierType;
	}

	public IdentifierType getIdentifierType() {
    	return identifierType;
    }

	public void setIdentifierType(IdentifierType identifierType) {
    	this.identifierType = identifierType;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#addSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
	 */
	public void addSubscriptionListener(ISubscriptionListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#removeSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
	 */
	public void removeSubscriptionListener(ISubscriptionListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getIdentifier()
	 */
	public IFeedIdentifier getIdentifier() {
		return identifierType.getIdentifier();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getSymbol()
     */
    public String getSymbol() {
	    return identifierType.getCompoundSymbol();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
	 */
	public IQuote getQuote() {
		return identifierType.getQuote();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
	 */
	public ITodayOHL getTodayOHL() {
		return identifierType.getTodayOHL();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
     */
    public ILastClose getLastClose() {
	    return identifierType.getLastClose();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
	 */
	public ITrade getTrade() {
		return identifierType.getTrade();
	}

	protected void addDelta(QuoteDelta delta) {
    	synchronized(deltaList) {
        	deltaList.add(delta);
    	}
    	connector.wakeupNotifyThread();
    }

	protected void submitRequests(final org.otfeed.IConnection connection) {
		TodaysOHLCommand ohlCommand = new TodaysOHLCommand(identifierType.getExchange(), identifierType.getSymbol(), ohlDelegate);
		ohlRequest = connection.prepareRequest(ohlCommand);
		ohlRequest.submit();

    	TickStreamWithSnapshotCommand tickStreamCommand = new TickStreamWithSnapshotCommand(identifierType.getExchange(), identifierType.getSymbol());
    	tickStreamCommand.setQuoteDelegate(quoteDelegate);
    	tickStreamCommand.setTradeDelegate(tradeDelegate);
    	tickStreamCommand.setBboDelegate(bboDelegate);
    	tickStreamCommand.setCompletionDelegate(new ICompletionDelegate() {
            public void onDataEnd(OTError error) {
            	Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID,
            			NLS.bind("Tick stream error for symbol {0}: {1}", new Object[] {
            					getSymbol(),
            					error.toString(),
            				}));
            	OTActivator.log(status);
            }
    	});
    	quoteStreamRequest = connection.prepareRequest(tickStreamCommand);
    	quoteStreamRequest.submit();

    	Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), Locale.US);
		getLastCloseFromHistory(connection, c);
	}

	// Recursively retrives the daily OHLC data until the last trade day is reached.
	private void getLastCloseFromHistory(final org.otfeed.IConnection connection, final Calendar refDay) {
		refDay.set(Calendar.HOUR_OF_DAY, 0);
		refDay.set(Calendar.MINUTE, 0);
		refDay.set(Calendar.SECOND, 0);
		refDay.set(Calendar.MILLISECOND, 0);

		refDay.add(Calendar.DATE, -1);
		while (refDay.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || refDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			refDay.add(Calendar.DATE, -1);

		final Set<OTOHLC> events = new HashSet<OTOHLC>();
		HistDataCommand command = new HistDataCommand(identifierType.getExchange(), identifierType.getSymbol(), refDay.getTime(), refDay.getTime(), AggregationSpan.days(1), new IDataDelegate<OTOHLC>() {
            public void onData(OTOHLC event) {
    			PriceDataType priceData = identifierType.getPriceData();
    			ILastClose oldValue = identifierType.getLastClose();
    			priceData.setClose(event.getClosePrice());
    			ILastClose newValue = new LastClose(priceData.getClose(), null);
    			if (!newValue.equals(oldValue)) {
    				addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
    				identifierType.setLastClose(newValue);
    			}
            	events.add(event);
            }
		});
		command.setCompletionDelegate(new ICompletionDelegate() {
            public void onDataEnd(OTError error) {
            	if (events.size() == 0)
            		getLastCloseFromHistory(connection, refDay);
            }
		});
		IRequest request = connection.prepareRequest(command);
		request.submit();
	}

	protected void cancelRequests() throws Exception {
		if (ohlRequest != null && !ohlRequest.isCompleted()) {
			try {
				ohlRequest.cancel();
			} catch (IllegalStateException e) {
				// Do nothing
			}
			ohlRequest = null;
		}
		if (quoteStreamRequest != null && !quoteStreamRequest.isCompleted()) {
			try {
				quoteStreamRequest.cancel();
			} catch (IllegalStateException e) {
				// Do nothing
			}
			quoteStreamRequest = null;
		}
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#dispose()
     */
    public void dispose() {
    	connector.disposeSubscription(this);
	}

    protected int incrementInstanceCount() {
    	instanceCount++;
    	return instanceCount;
    }

    protected int decrementInstanceCount() {
    	instanceCount--;
    	return instanceCount;
    }

	protected int getInstanceCount() {
    	return instanceCount;
    }

    protected void fireNotification() {
    	QuoteDelta[] deltas;
    	synchronized(deltaList) {
        	if (deltaList.isEmpty() || listeners.size() == 0)
        		return;
        	deltas = deltaList.toArray(new QuoteDelta[deltaList.size()]);
        	deltaList.clear();
    	}
    	QuoteEvent event = new QuoteEvent(connector, getIdentifier(), deltas);
    	Object[] l = listeners.getListeners();
    	for (int i = 0; i < l.length; i++) {
    		try {
        		((ISubscriptionListener) l[i]).quoteUpdate(event);
    		} catch(Exception e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
				OTActivator.log(status);
    		} catch (LinkageError e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
				OTActivator.log(status);
    		}
    	}
    }
}
