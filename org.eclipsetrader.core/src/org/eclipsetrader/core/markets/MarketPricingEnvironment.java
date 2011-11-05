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

package org.eclipsetrader.core.markets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IBarOpen;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPrice;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;

/**
 * Pricing environment implementation based on markets.
 *
 * <p>Feed connectors are choosen based on the market that holds a security.
 * If securities don't belong to a market, the default feed connector is used.</p>
 *
 * @since 1.0
 */
public class MarketPricingEnvironment implements IPricingEnvironment {

    private IMarketService marketService;

    class PricingStatus {

        ITrade trade;
        IQuote quote;
        ITodayOHL todayOHL;
        ILastClose lastClose;
        IBook book;
        IBarOpen todayBarOpen;
        IBar todayBar;
        List<PricingDelta> deltas = new ArrayList<PricingDelta>();
    }

    class SubscriptionStatus {

        Map<IFeedConnector, IFeedSubscription> subscriptions = new HashMap<IFeedConnector, IFeedSubscription>();
        List<ISecurity> securities = new ArrayList<ISecurity>();
    }

    class SubscriptionStatus2 {

        Map<IFeedConnector2, IFeedSubscription2> subscriptions = new HashMap<IFeedConnector2, IFeedSubscription2>();
        List<ISecurity> securities = new ArrayList<ISecurity>();
    }

    private Map<ISecurity, PricingStatus> securitiesMap = new HashMap<ISecurity, PricingStatus>();
    private Map<IFeedIdentifier, SubscriptionStatus> identifiersMap = new HashMap<IFeedIdentifier, SubscriptionStatus>();
    private Map<IFeedIdentifier, SubscriptionStatus2> identifiersMap2 = new HashMap<IFeedIdentifier, SubscriptionStatus2>();

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private ISubscriptionListener listener = new ISubscriptionListener() {

        @Override
        public void quoteUpdate(QuoteEvent event) {
            processUpdateQuotes(event.getIdentifier(), event.getDelta());
        }
    };

    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof IMarket) {
                handleMarketChanges((IMarket) evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
            if (evt.getSource() instanceof IFeedIdentifier) {
                for (Iterator<Entry<IFeedIdentifier, SubscriptionStatus>> iter = identifiersMap.entrySet().iterator(); iter.hasNext();) {
                    Entry<IFeedIdentifier, SubscriptionStatus> entry = iter.next();
                    if (entry.getKey() == evt.getSource()) {
                        iter.remove();
                        identifiersMap.put((IFeedIdentifier) evt.getSource(), entry.getValue());
                        break;
                    }
                }
            }
        }
    };

    private IMarketStatusListener marketStatusListener = new IMarketStatusListener() {

        @Override
        public void marketStatusChanged(MarketStatusEvent event) {
            IMarket market = event.getMarket();
            if (!market.isOpen()) {
                // fireTodayBarCloseEvent();
            }
        }
    };

    protected MarketPricingEnvironment() {
    }

    public MarketPricingEnvironment(IMarketService marketService) {
        this(marketService, null);
    }

    public MarketPricingEnvironment(IMarketService marketService, ISecurity[] securities) {
        this.marketService = marketService;
        marketService.addMarketStatusListener(marketStatusListener);

        for (IMarket market : marketService.getMarkets()) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) market.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
            }
        }

        if (securities != null) {
            addSecurities(securities);
        }
    }

    protected IMarket getMarketsForSecurity(ISecurity security) {
        if (marketService != null) {
            for (IMarket market : marketService.getMarkets()) {
                if (market.hasMember(security)) {
                    return market;
                }
            }
        }
        return null;
    }

    public void addSecurity(ISecurity security) {
        IFeedConnector connector = getDefaultConnector();

        IMarket market = getMarketsForSecurity(security);
        if (market != null && market.getLiveFeedConnector() != null) {
            connector = market.getLiveFeedConnector();
        }

        IConnectorOverride override = (IConnectorOverride) AdapterManager.getDefault().getAdapter(security, IConnectorOverride.class);
        if (override != null) {
            if (override.getLiveFeedConnector() != null) {
                connector = override.getLiveFeedConnector();
            }
        }

        if (connector != null) {
            subscribeSecurity(security, connector);
        }
    }

    public void addSecurities(ISecurity[] securities) {
        for (ISecurity security : securities) {
            addSecurity(security);
        }
    }

    public void addLevel2Security(ISecurity security) {
        IFeedConnector2 connector = null;

        IMarket market = getMarketsForSecurity(security);
        if (market != null && market.getLiveFeedConnector() instanceof IFeedConnector2) {
            connector = (IFeedConnector2) market.getLiveFeedConnector();
        }

        if (connector != null) {
            subscribeSecurity2(security, connector);
        }
    }

    protected IFeedConnector getDefaultConnector() {
        if (CoreActivator.getDefault() == null) {
            return null;
        }
        return CoreActivator.getDefault().getDefaultConnector();
    }

    protected void subscribeSecurity(ISecurity security, IFeedConnector connector) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

        PricingStatus pricingStatus;
        synchronized (securitiesMap) {
            pricingStatus = securitiesMap.get(security);
            if (pricingStatus == null) {
                pricingStatus = new PricingStatus();
                securitiesMap.put(security, pricingStatus);
            }
        }

        if (identifier != null) {
            SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
            if (subscriptionStatus == null) {
                subscriptionStatus = new SubscriptionStatus();
                identifiersMap.put(identifier, subscriptionStatus);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
                }
            }

            subscriptionStatus.securities.add(security);

            IFeedSubscription subscription = subscriptionStatus.subscriptions.get(connector);
            if (subscription == null) {
                subscription = connector.subscribe(identifier);
                subscriptionStatus.subscriptions.put(connector, subscription);
                subscription.addSubscriptionListener(listener);
            }

            pricingStatus.trade = subscription.getTrade();
            if (pricingStatus.trade != null) {
                pricingStatus.deltas.add(new PricingDelta(null, pricingStatus.trade));
            }
            pricingStatus.quote = subscription.getQuote();
            if (pricingStatus.quote != null) {
                pricingStatus.deltas.add(new PricingDelta(null, pricingStatus.quote));
            }
            pricingStatus.todayOHL = subscription.getTodayOHL();
            if (pricingStatus.todayOHL != null) {
                pricingStatus.deltas.add(new PricingDelta(null, pricingStatus.todayOHL));
            }
            pricingStatus.lastClose = subscription.getLastClose();
            if (pricingStatus.lastClose != null) {
                pricingStatus.deltas.add(new PricingDelta(null, pricingStatus.lastClose));
            }
        }

        notifyListeners();
    }

    protected void subscribeSecurity2(ISecurity security, IFeedConnector2 connector) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

        PricingStatus pricingStatus;
        synchronized (securitiesMap) {
            pricingStatus = securitiesMap.get(security);
            if (pricingStatus == null) {
                pricingStatus = new PricingStatus();
                securitiesMap.put(security, pricingStatus);
            }
        }

        if (identifier != null) {
            SubscriptionStatus2 subscriptionStatus = identifiersMap2.get(identifier);
            if (subscriptionStatus == null) {
                subscriptionStatus = new SubscriptionStatus2();
                identifiersMap2.put(identifier, subscriptionStatus);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
                }
            }

            subscriptionStatus.securities.add(security);

            IFeedSubscription2 subscription = subscriptionStatus.subscriptions.get(connector);
            if (subscription == null) {
                subscription = connector.subscribeLevel2(identifier);
                subscriptionStatus.subscriptions.put(connector, subscription);
                subscription.addSubscriptionListener(listener);
            }

            pricingStatus.trade = subscription.getTrade();
            pricingStatus.quote = subscription.getQuote();
            pricingStatus.todayOHL = subscription.getTodayOHL();
            pricingStatus.lastClose = subscription.getLastClose();
            pricingStatus.book = subscription.getBook();
        }
    }

    public void removeSecurity(ISecurity security) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

        if (identifier != null) {
            SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
            if (subscriptionStatus != null) {
                subscriptionStatus.securities.remove(security);

                if (subscriptionStatus.securities.size() == 0) {
                    for (IFeedSubscription subscription : subscriptionStatus.subscriptions.values()) {
                        subscription.removeSubscriptionListener(listener);
                        subscription.dispose();
                    }
                    identifiersMap.remove(identifier);

                    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                    if (propertyChangeSupport != null) {
                        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
                    }
                }
            }
        }

        synchronized (securitiesMap) {
            if (!identifiersMap.containsKey(identifier) && !identifiersMap2.containsKey(identifier)) {
                securitiesMap.remove(security);
            }
        }
    }

    public void removeLevel2Security(ISecurity security) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

        if (identifier != null) {
            SubscriptionStatus2 subscriptionStatus2 = identifiersMap2.get(identifier);
            if (subscriptionStatus2 != null) {
                subscriptionStatus2.securities.remove(security);

                if (subscriptionStatus2.securities.size() == 0) {
                    for (IFeedSubscription2 subscription : subscriptionStatus2.subscriptions.values()) {
                        subscription.removeSubscriptionListener(listener);
                        subscription.dispose();
                    }
                    identifiersMap2.remove(identifier);

                    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                    if (propertyChangeSupport != null) {
                        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
                    }
                }
            }
        }

        synchronized (securitiesMap) {
            if (!identifiersMap.containsKey(identifier) && !identifiersMap2.containsKey(identifier)) {
                securitiesMap.remove(security);
            }
        }
    }

    public void removeSecurities(ISecurity[] securities) {
        for (ISecurity security : securities) {
            removeSecurity(security);
        }
    }

    protected void handleMarketChanges(IMarket market, String propertyName, Object oldValue, Object newValue) {
        if (IMarket.PROP_LIVE_FEED_CONNECTOR.equals(propertyName)) {
            IFeedConnector oldConnector = (IFeedConnector) oldValue;
            IFeedConnector newConnector = (IFeedConnector) newValue;
            if (newConnector == null) {
                newConnector = getDefaultConnector();
            }

            for (ISecurity security : market.getMembers()) {
                if (securitiesMap.containsKey(security)) {
                    IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
                    if (identifier != null) {
                        SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
                        if (subscriptionStatus != null) {
                            IFeedSubscription subscription = subscriptionStatus.subscriptions.get(oldConnector);
                            if (subscription != null) {
                                subscription.removeSubscriptionListener(listener);
                                subscription.dispose();
                            }
                        }

                        if (newConnector != null) {
                            IFeedSubscription subscription = newConnector.subscribe(identifier);
                            subscriptionStatus.subscriptions.put(newConnector, subscription);
                            subscription.addSubscriptionListener(listener);

                            PricingStatus pricingStatus = securitiesMap.get(security);

                            pricingStatus.deltas.add(new PricingDelta(pricingStatus.trade, subscription.getTrade()));
                            pricingStatus.deltas.add(new PricingDelta(pricingStatus.quote, subscription.getQuote()));
                            pricingStatus.deltas.add(new PricingDelta(pricingStatus.todayOHL, subscription.getTodayOHL()));
                            pricingStatus.deltas.add(new PricingDelta(pricingStatus.lastClose, subscription.getLastClose()));

                            pricingStatus.trade = subscription.getTrade();
                            pricingStatus.quote = subscription.getQuote();
                            pricingStatus.todayOHL = subscription.getTodayOHL();
                            pricingStatus.lastClose = subscription.getLastClose();
                        }
                    }
                }
            }

            notifyListeners();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#addPricingListener(org.eclipsetrader.core.feed.IPricingListener)
     */
    @Override
    public void addPricingListener(IPricingListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#removePricingListener(org.eclipsetrader.core.feed.IPricingListener)
     */
    @Override
    public void removePricingListener(IPricingListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#dispose()
     */
    @Override
    public void dispose() {
        marketService.removeMarketStatusListener(marketStatusListener);

        for (IMarket market : marketService.getMarkets()) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) market.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            }
        }

        listeners.clear();

        for (Iterator<Entry<IFeedIdentifier, SubscriptionStatus>> iter = identifiersMap.entrySet().iterator(); iter.hasNext();) {
            Entry<IFeedIdentifier, SubscriptionStatus> entry = iter.next();

            for (IFeedSubscription subscription : entry.getValue().subscriptions.values()) {
                subscription.removeSubscriptionListener(listener);
                subscription.dispose();
            }

            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) entry.getKey().getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            }
        }

        identifiersMap.clear();
        securitiesMap.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public ITrade getTrade(ISecurity security) {
        return securitiesMap.get(security) != null ? securitiesMap.get(security).trade : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getQuote(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IQuote getQuote(ISecurity security) {
        return securitiesMap.get(security) != null ? securitiesMap.get(security).quote : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getTodayOHL(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public ITodayOHL getTodayOHL(ISecurity security) {
        return securitiesMap.get(security) != null ? securitiesMap.get(security).todayOHL : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getLastClose(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public ILastClose getLastClose(ISecurity security) {
        return securitiesMap.get(security) != null ? securitiesMap.get(security).lastClose : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getBook(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IBook getBook(ISecurity security) {
        return securitiesMap.get(security) != null ? securitiesMap.get(security).book : null;
    }

    protected void processUpdateQuotes(IFeedIdentifier identifier, QuoteDelta[] delta) {
        SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
        if (subscriptionStatus != null) {
            for (ISecurity security : subscriptionStatus.securities) {
                PricingStatus pricingStatus = securitiesMap.get(security);
                if (pricingStatus != null) {
                    for (int i = 0; i < delta.length; i++) {
                        if (delta[i].getNewValue() instanceof ITrade) {
                            Object oldValue = pricingStatus.trade;
                            pricingStatus.trade = (ITrade) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(oldValue, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof IQuote) {
                            Object oldValue = pricingStatus.quote;
                            pricingStatus.quote = (IQuote) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(oldValue, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof ITodayOHL) {
                            Object oldValue = pricingStatus.todayOHL;
                            pricingStatus.todayOHL = (ITodayOHL) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(oldValue, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof ILastClose) {
                            Object oldValue = pricingStatus.lastClose;
                            pricingStatus.lastClose = (ILastClose) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(oldValue, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof IBook) {
                            Object oldValue = pricingStatus.book;
                            pricingStatus.book = (IBook) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(oldValue, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof IBarOpen) {
                            pricingStatus.todayBarOpen = (IBarOpen) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(null, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof IBar) {
                            pricingStatus.todayBar = (IBar) delta[i].getNewValue();
                            pricingStatus.deltas.add(new PricingDelta(null, delta[i].getNewValue()));
                        }
                        if (delta[i].getNewValue() instanceof IPrice) {
                            pricingStatus.deltas.add(new PricingDelta(null, delta[i].getNewValue()));
                        }
                    }
                }
            }
            notifyListeners();
        }
    }

    protected void notifyListeners() {
        Object[] l = listeners.getListeners();

        ISecurity[] securities;
        synchronized (securitiesMap) {
            Set<ISecurity> set = securitiesMap.keySet();
            securities = set.toArray(new ISecurity[set.size()]);
        }

        for (ISecurity security : securities) {
            PricingStatus pricingStatus = securitiesMap.get(security);
            if (pricingStatus == null || pricingStatus.deltas.size() == 0) {
                continue;
            }
            PricingEvent event = new PricingEvent(security, pricingStatus.deltas.toArray(new PricingDelta[pricingStatus.deltas.size()]));
            for (int i = 0; i < l.length; i++) {
                try {
                    ((IPricingListener) l[i]).pricingUpdate(event);
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment listener", e); //$NON-NLS-1$
                    CoreActivator.log(status);
                } catch (LinkageError e) {
                    Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment listener", e); //$NON-NLS-1$
                    CoreActivator.log(status);
                }
            }
            pricingStatus.deltas.clear();
        }
    }

    PricingStatus getPricingStatus(ISecurity security) {
        return securitiesMap.get(security);
    }

    SubscriptionStatus getSubscriptionStatus(ISecurity security) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
        return identifier != null ? identifiersMap.get(identifier) : null;
    }

    protected void fireTodayBarCloseEvent() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        for (ISecurity security : securitiesMap.keySet()) {
            PricingStatus pricingStatus = securitiesMap.get(security);
            if (pricingStatus != null && pricingStatus.todayOHL != null && pricingStatus.trade != null) {
                IBar newValue = new Bar(c.getTime(), TimeSpan.days(1),
                    pricingStatus.todayOHL.getOpen(), pricingStatus.todayOHL.getHigh(), pricingStatus.todayOHL.getLow(),
                    pricingStatus.trade.getPrice(), pricingStatus.trade.getVolume());
                if (!newValue.equals(pricingStatus.todayBar)) {
                    pricingStatus.todayBar = newValue;
                    System.out.println(String.format("%s: %s", security.getName(), newValue));
                    pricingStatus.deltas.add(new PricingDelta(null, newValue));
                }
            }
        }

        notifyListeners();
    }
}
