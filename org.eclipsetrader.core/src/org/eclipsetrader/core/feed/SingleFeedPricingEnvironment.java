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

package org.eclipsetrader.core.feed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;

public class SingleFeedPricingEnvironment implements IPricingEnvironment {

    private IFeedConnector connector;

    class PricingStatus {

        ITrade trade;
        IQuote quote;
        ITodayOHL todayOHL;
        ILastClose lastClose;
        IBook book;
        List<PricingDelta> deltas = new ArrayList<PricingDelta>();
        IFeedSubscription subscription;
    }

    class SubscriptionStatus {

        IFeedSubscription subscription;
        List<ISecurity> securities = new ArrayList<ISecurity>();
    }

    Map<ISecurity, PricingStatus> securitiesMap = new HashMap<ISecurity, PricingStatus>();
    Map<IFeedIdentifier, SubscriptionStatus> identifiersMap = new HashMap<IFeedIdentifier, SubscriptionStatus>();

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private ISubscriptionListener listener = new ISubscriptionListener() {

        @Override
        public void quoteUpdate(QuoteEvent event) {
            processUpdateQuotes(event.getIdentifier(), event.getDelta());
        }
    };

    protected SingleFeedPricingEnvironment() {
    }

    public SingleFeedPricingEnvironment(IFeedConnector connector) {
        this.connector = connector;
    }

    public void addSecurity(ISecurity security) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

        PricingStatus pricingStatus = securitiesMap.get(security);
        if (pricingStatus == null) {
            pricingStatus = new PricingStatus();
            securitiesMap.put(security, pricingStatus);
        }

        if (identifier != null) {
            SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
            if (subscriptionStatus == null) {
                subscriptionStatus = new SubscriptionStatus();
                identifiersMap.put(identifier, subscriptionStatus);
            }

            subscriptionStatus.securities.add(security);

            if (subscriptionStatus.subscription == null) {
                subscriptionStatus.subscription = connector.subscribe(identifier);
                subscriptionStatus.subscription.addSubscriptionListener(listener);
            }

            pricingStatus.trade = subscriptionStatus.subscription.getTrade();
            pricingStatus.quote = subscriptionStatus.subscription.getQuote();
            pricingStatus.todayOHL = subscriptionStatus.subscription.getTodayOHL();
            pricingStatus.lastClose = subscriptionStatus.subscription.getLastClose();
        }
    }

    public void addSecurities(ISecurity[] securities) {
        for (ISecurity security : securities) {
            addSecurity(security);
        }
    }

    public void removeSecurity(ISecurity security) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

        securitiesMap.remove(security);

        if (identifier != null) {
            SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
            if (subscriptionStatus != null) {
                subscriptionStatus.securities.remove(security);

                if (subscriptionStatus.securities.size() == 0) {
                    subscriptionStatus.subscription.removeSubscriptionListener(listener);
                    identifiersMap.remove(identifier);
                    subscriptionStatus.subscription.dispose();
                }
            }
        }
    }

    public void removeSecurities(ISecurity[] securities) {
        for (ISecurity security : securities) {
            removeSecurity(security);
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
        listeners.clear();

        for (SubscriptionStatus pricingStatus : identifiersMap.values()) {
            if (pricingStatus.subscription != null) {
                pricingStatus.subscription.removeSubscriptionListener(listener);
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
                    for (QuoteDelta d : delta) {
                        if (d.getNewValue() instanceof ITrade) {
                            pricingStatus.trade = (ITrade) d.getNewValue();
                        }
                        if (d.getNewValue() instanceof IQuote) {
                            pricingStatus.quote = (IQuote) d.getNewValue();
                        }
                        if (d.getNewValue() instanceof ITodayOHL) {
                            pricingStatus.todayOHL = (ITodayOHL) d.getNewValue();
                        }
                        if (d.getNewValue() instanceof ILastClose) {
                            pricingStatus.lastClose = (ILastClose) d.getNewValue();
                        }
                        if (d.getNewValue() instanceof IBook) {
                            pricingStatus.book = (IBook) d.getNewValue();
                        }
                        pricingStatus.deltas.add(new PricingDelta(d.getOldValue(), d.getNewValue()));
                    }
                }
            }
            notifyListeners();
        }
    }

    protected void notifyListeners() {
        Object[] l = listeners.getListeners();
        for (ISecurity security : securitiesMap.keySet()) {
            PricingStatus status = securitiesMap.get(security);
            if (status == null || status.deltas.size() == 0) {
                continue;
            }
            final PricingEvent event = new PricingEvent(security, status.deltas.toArray(new PricingDelta[status.deltas.size()]));
            for (int i = 0; i < l.length; i++) {
                final IPricingListener listener = (IPricingListener) l[i];
                SafeRunner.run(new ISafeRunnable() {

                    @Override
                    public void run() throws Exception {
                        listener.pricingUpdate(event);
                    }

                    @Override
                    public void handleException(Throwable exception) {
                        Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment listener", exception); //$NON-NLS-1$
                        CoreActivator.getDefault().getLog().log(status);
                    }
                });
            }
            status.deltas.clear();
        }
    }

    PricingStatus getPricingStatus(ISecurity security) {
        return securitiesMap.get(security);
    }

    SubscriptionStatus getSubscriptionStatus(ISecurity security) {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
        return identifier != null ? identifiersMap.get(identifier) : null;
    }
}
