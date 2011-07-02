/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.directa.internal.core.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.BarGenerator;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.repository.IdentifierType;

public class FeedSubscription implements IFeedSubscription2 {

    private StreamingConnector connector;
    private ITrade trade;
    private IQuote quote;
    private ITodayOHL todayOHL;
    private ILastClose lastClose;
    private IBook book;
    private IBar bar;
    private BarGenerator barGenerator;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private IdentifierType identifierType;
    private List<QuoteDelta> deltaList = new ArrayList<QuoteDelta>();
    private int instanceCount = 0;
    private int level2InstanceCount = 0;

    public FeedSubscription(StreamingConnector connector, IdentifierType identifierType) {
        this.connector = connector;
        this.identifierType = identifierType;
        this.trade = identifierType.getTrade();
        this.quote = identifierType.getQuote();
        this.todayOHL = identifierType.getTodayOHL();
        this.lastClose = identifierType.getLastClose();

        this.barGenerator = new BarGenerator(TimeSpan.minutes(1));
        this.barGenerator.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof IBar) {
                    addDelta(new QuoteDelta(FeedSubscription.this.identifierType.getIdentifier(), bar, arg));
                    bar = (IBar) arg;
                }
                else {
                    addDelta(new QuoteDelta(FeedSubscription.this.identifierType.getIdentifier(), null, arg));
                }
            }
        });
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
    @Override
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

    protected int incrementLevel2InstanceCount() {
        level2InstanceCount++;
        return level2InstanceCount;
    }

    protected int decrementLevel2InstanceCount() {
        level2InstanceCount--;
        return level2InstanceCount;
    }

    public int getLevel2InstanceCount() {
        return level2InstanceCount;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#addSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
     */
    @Override
    public void addSubscriptionListener(ISubscriptionListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#removeSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
     */
    @Override
    public void removeSubscriptionListener(ISubscriptionListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getIdentifier()
     */
    @Override
    public IFeedIdentifier getIdentifier() {
        return identifierType.getIdentifier();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getSymbol()
     */
    @Override
    public String getSymbol() {
        return identifierType.getSymbol();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
     */
    @Override
    public IQuote getQuote() {
        return quote;
    }

    public void setQuote(IQuote quote) {
        if (!quote.equals(this.quote)) {
            addDelta(new QuoteDelta(identifierType.getIdentifier(), this.quote, quote));
            this.quote = quote;
            this.identifierType.setQuote(quote);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
     */
    @Override
    public ITodayOHL getTodayOHL() {
        return todayOHL;
    }

    public void setTodayOHL(ITodayOHL todayOHL) {
        if (!todayOHL.equals(this.todayOHL)) {
            addDelta(new QuoteDelta(identifierType.getIdentifier(), this.todayOHL, todayOHL));
            this.todayOHL = todayOHL;
            this.identifierType.setTodayOHL(todayOHL);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
     */
    @Override
    public ITrade getTrade() {
        return trade;
    }

    public void setTrade(ITrade trade) {
        if (!trade.equals(this.trade)) {
            addDelta(new QuoteDelta(identifierType.getIdentifier(), this.trade, trade));
            this.trade = trade;
            this.identifierType.setTrade(trade);
        }

        barGenerator.addTrade(trade);
    }

    public void setOTCTrade(ITrade trade) {
        if (!trade.equals(this.trade)) {
            addDelta(new QuoteDelta(identifierType.getIdentifier(), this.trade, trade));
            this.trade = trade;
            this.identifierType.setTrade(trade);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
     */
    @Override
    public ILastClose getLastClose() {
        return lastClose;
    }

    public void setLastClose(ILastClose lastClose) {
        if (!lastClose.equals(this.lastClose)) {
            addDelta(new QuoteDelta(identifierType.getIdentifier(), this.lastClose, lastClose));
            this.lastClose = lastClose;
            this.identifierType.setLastClose(lastClose);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription2#getBook()
     */
    @Override
    public IBook getBook() {
        return book;
    }

    public void setBook(IBook book) {
        addDelta(new QuoteDelta(identifierType.getIdentifier(), this.book, book));
        this.book = book;
    }

    public void addDelta(QuoteDelta delta) {
        synchronized (deltaList) {
            deltaList.add(delta);
        }
    }

    public boolean hasListeners() {
        return listeners.size() != 0;
    }

    public void fireNotification() {
        QuoteDelta[] deltas;
        synchronized (deltaList) {
            if (deltaList.isEmpty()) {
                return;
            }
            deltas = deltaList.toArray(new QuoteDelta[deltaList.size()]);
            deltaList.clear();
        }
        QuoteEvent event = new QuoteEvent(connector, getIdentifier(), deltas);
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((ISubscriptionListener) l[i]).quoteUpdate(event);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error notifying a quote update", e);
                Activator.log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error notifying a quote update", e);
                Activator.log(status);
            }
        }
    }

    public void forceBarClose() {
        if (barGenerator.isBarExpired()) {
            barGenerator.forceBarClose();
        }
    }

    public boolean hasPendingChanges() {
        synchronized (deltaList) {
            return deltaList.size() != 0;
        }
    }
}
