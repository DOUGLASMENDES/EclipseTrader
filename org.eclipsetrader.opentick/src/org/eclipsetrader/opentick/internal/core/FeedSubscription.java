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
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
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
import org.otfeed.command.EquityInitCommand;
import org.otfeed.command.TickStreamWithSnapshotCommand;
import org.otfeed.command.TodaysOHLCommand;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTEquityInit;
import org.otfeed.event.OTError;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTodaysOHL;
import org.otfeed.event.OTTrade;
import org.otfeed.event.TradeSideEnum;

public class FeedSubscription implements IFeedSubscription {
	private FeedConnector connector;
	private ITrade trade;
	private IQuote quote;
	private ITodayOHL todayOHL;
	private ILastClose lastClose;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private IdentifierType identifierType;
	private List<QuoteDelta> deltaList = new ArrayList<QuoteDelta>();
	private int instanceCount = 0;

	private IRequest equityInitRequest;
	private IRequest ohlRequest;
	private IRequest quoteStreamRequest;

	private IDataDelegate<OTEquityInit> equityInitDelegate = new IDataDelegate<OTEquityInit>() {
	    public void onData(OTEquityInit event) {
			PriceDataType priceData = identifierType.getPriceData();
			ILastClose oldValue = identifierType.getLastClose();
			priceData.setClose(event.getPrevClosePrice());
			ILastClose newValue = new LastClose(priceData.getClose(), null);
			if (!newValue.equals(oldValue)) {
				addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setLastClose(newValue);
			}
	    }
	};

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
	    this.trade = identifierType.getTrade();
	    this.quote = identifierType.getQuote();
	    this.todayOHL = identifierType.getTodayOHL();
	    this.lastClose = identifierType.getLastClose();
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
		return quote;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
	 */
	public ITodayOHL getTodayOHL() {
		return todayOHL;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
     */
    public ILastClose getLastClose() {
	    return lastClose;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
	 */
	public ITrade getTrade() {
		return trade;
	}

	protected void addDelta(QuoteDelta delta) {
    	synchronized(deltaList) {
        	deltaList.add(delta);
    	}
    	connector.wakeupNotifyThread();
    }

	protected void submitRequests(org.otfeed.IConnection connection) {
		EquityInitCommand equityInitCommand = new EquityInitCommand(identifierType.getExchange(), identifierType.getSymbol(), equityInitDelegate);
		equityInitRequest = connection.prepareRequest(equityInitCommand);
		equityInitRequest.submit();

		TodaysOHLCommand ohlCommand = new TodaysOHLCommand(identifierType.getExchange(), identifierType.getSymbol(), ohlDelegate);
		ohlRequest = connection.prepareRequest(ohlCommand);
		ohlRequest.submit();

    	TickStreamWithSnapshotCommand tickStreamCommand = new TickStreamWithSnapshotCommand(identifierType.getExchange(), identifierType.getSymbol());
    	tickStreamCommand.setQuoteDelegate(quoteDelegate);
    	tickStreamCommand.setTradeDelegate(tradeDelegate);
    	tickStreamCommand.setBboDelegate(bboDelegate);
    	tickStreamCommand.setCompletionDelegate(new ICompletionDelegate() {
            public void onDataEnd(OTError error) {
            	System.err.println(getSymbol() + ": " + error);
            }
    	});
    	quoteStreamRequest = connection.prepareRequest(tickStreamCommand);
    	quoteStreamRequest.submit();

    	if (getSymbol().endsWith("MSFT"))
    		System.out.println("Requesting data for " + getSymbol());
	}

	protected void cancelRequests() throws Exception {
		if (equityInitRequest != null && !equityInitRequest.isCompleted()) {
			try {
				equityInitRequest.cancel();
			} catch (IllegalStateException e) {
				// Do nothing
			}
			equityInitRequest = null;
		}
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
