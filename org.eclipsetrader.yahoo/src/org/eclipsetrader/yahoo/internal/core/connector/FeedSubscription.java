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

package org.eclipsetrader.yahoo.internal.core.connector;

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
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.eclipsetrader.yahoo.internal.core.repository.IdentifierType;

public class FeedSubscription implements IFeedSubscription {
	private SnapshotConnector connector;
	private IFeedIdentifier identifier;
	private ITrade trade;
	private IQuote quote;
	private ITodayOHL todayOHL;
	private ILastClose lastClose;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private IdentifierType identifierType;
	private List<QuoteDelta> deltaList = new ArrayList<QuoteDelta>();
	private int instanceCount = 0;

	public FeedSubscription(SnapshotConnector connector, IdentifierType identifierType) {
		this.connector = connector;
		this.identifierType = identifierType;
	    this.identifier = identifierType.getIdentifier();
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
     * @see org.eclipsetrader.core.feed.IFeedSubscription#dispose()
     */
    public void dispose() {
	    connector.disposeSubscription(this);
    }

    protected void incrementInstanceCount() {
    	instanceCount++;
    }

    protected int decrementInstanceCount() {
    	instanceCount--;
    	return instanceCount;
    }

	protected int getInstanceCount() {
    	return instanceCount;
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
		return identifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
	 */
	public IQuote getQuote() {
		return quote;
	}

	public void setQuote(IQuote quote) {
    	this.quote = quote;
    	this.identifierType.setQuote(quote);
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
	 */
	public ITodayOHL getTodayOHL() {
		return todayOHL;
	}

	public void setTodayOHL(ITodayOHL todayOHL) {
    	this.todayOHL = todayOHL;
    	this.identifierType.setTodayOHL(todayOHL);
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
	 */
	public ITrade getTrade() {
		return trade;
	}

	public void setTrade(ITrade trade) {
    	this.trade = trade;
    	this.identifierType.setTrade(trade);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
     */
    public ILastClose getLastClose() {
	    return lastClose;
    }

	public void setLastClose(ILastClose lastClose) {
    	this.lastClose = lastClose;
    	this.identifierType.setLastClose(lastClose);
    }

	public void addDelta(QuoteDelta delta) {
    	synchronized(deltaList) {
        	deltaList.add(delta);
    	}
    }

	public boolean hasListeners() {
		return listeners.size() != 0;
	}

    public void fireNotification() {
    	QuoteDelta[] deltas;
    	synchronized(deltaList) {
        	if (deltaList.isEmpty())
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
				Status status = new Status(Status.ERROR, YahooActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
				YahooActivator.log(status);
    		} catch (LinkageError e) {
				Status status = new Status(Status.ERROR, YahooActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
				YahooActivator.log(status);
    		}
    	}
    }
}
