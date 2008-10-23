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

package org.eclipsetrader.core.markets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ILastClose;
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
		List<PricingDelta> deltas = new ArrayList<PricingDelta>();
	}

	class SubscriptionStatus {
		Map<IFeedConnector, IFeedSubscription> subscriptions = new HashMap<IFeedConnector, IFeedSubscription>();
		List<ISecurity> securities = new ArrayList<ISecurity>();
	}

	private Map<ISecurity, PricingStatus> securitiesMap = new HashMap<ISecurity, PricingStatus>();
	private Map<IFeedIdentifier, SubscriptionStatus> identifiersMap = new HashMap<IFeedIdentifier, SubscriptionStatus>();

	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private ISubscriptionListener listener = new ISubscriptionListener() {
        public void quoteUpdate(QuoteEvent event) {
        	processUpdateQuotes(event.getIdentifier(), event.getDelta());
        }
	};

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getSource() instanceof IMarket)
        		handleMarketChanges((IMarket) evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        	if (evt.getSource() instanceof IFeedIdentifier) {
            	for (Iterator<Entry<IFeedIdentifier, SubscriptionStatus>> iter = identifiersMap.entrySet().iterator(); iter.hasNext(); ) {
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

	protected MarketPricingEnvironment() {
	}

	public MarketPricingEnvironment(IMarketService marketService) {
		this(marketService, null);
    }

	public MarketPricingEnvironment(IMarketService marketService, ISecurity[] securities) {
	    this.marketService = marketService;

	    for (IMarket market : marketService.getMarkets()) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) market.getAdapter(PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
	    }

	    if (securities != null)
	    	addSecurities(securities);
    }

	protected IMarket getMarketsForSecurity(ISecurity security) {
		if (marketService != null) {
			for (IMarket market : marketService.getMarkets()) {
				if (market.hasMember(security))
					return market;
			}
		}
		return null;
	}

    public void addSecurity(ISecurity security) {
		IFeedConnector connector = getDefaultConnector();

		IMarket market = getMarketsForSecurity(security);
		if (market != null && market.getLiveFeedConnector() != null)
			connector = market.getLiveFeedConnector();

		IConnectorOverride override = (IConnectorOverride) AdapterManager.getDefault().getAdapter(security, IConnectorOverride.class);
		if (override != null) {
			if (override.getLiveFeedConnector() != null)
				connector = override.getLiveFeedConnector();
		}

		if (connector != null)
			subscribeSecurity(security, connector);
    }

    public void addSecurities(ISecurity[] securities) {
		for (ISecurity security : securities)
			addSecurity(security);
    }

	protected IFeedConnector getDefaultConnector() {
		if (CoreActivator.getDefault() == null)
			return null;
		return CoreActivator.getDefault().getDefaultConnector();
	}

	protected void subscribeSecurity(ISecurity security, IFeedConnector connector) {
		IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

		PricingStatus pricingStatus = securitiesMap.get(security);
		if (pricingStatus == null) {
			synchronized(securitiesMap) {
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
	    		if (propertyChangeSupport != null)
	    			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
			}

			subscriptionStatus.securities.add(security);

			IFeedSubscription subscription = subscriptionStatus.subscriptions.get(connector);
			if (subscription == null) {
				subscription = connector.subscribe(identifier);
				subscriptionStatus.subscriptions.put(connector, subscription);
				subscription.addSubscriptionListener(listener);
			}

			pricingStatus.trade = subscription.getTrade();
			pricingStatus.quote = subscription.getQuote();
			pricingStatus.todayOHL = subscription.getTodayOHL();
			pricingStatus.lastClose = subscription.getLastClose();
		}
	}

    public void removeSecurity(ISecurity security) {
		IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

		synchronized(securitiesMap) {
			securitiesMap.remove(security);
		}

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
		    		if (propertyChangeSupport != null)
		    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
				}
			}
		}
    }

    public void removeSecurities(ISecurity[] securities) {
		for (ISecurity security : securities)
			removeSecurity(security);
    }

	protected void handleMarketChanges(IMarket market, String propertyName, Object oldValue, Object newValue) {
		if ("liveFeedConnector".equals(propertyName)) {
			IFeedConnector oldConnector = (IFeedConnector) oldValue;
			IFeedConnector newConnector = (IFeedConnector) newValue;
			if (newConnector == null)
				newConnector = getDefaultConnector();

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

							pricingStatus.deltas.add(new PricingDelta(security, pricingStatus.trade, subscription.getTrade()));
							pricingStatus.deltas.add(new PricingDelta(security, pricingStatus.quote, subscription.getQuote()));
							pricingStatus.deltas.add(new PricingDelta(security, pricingStatus.todayOHL, subscription.getTodayOHL()));
							pricingStatus.deltas.add(new PricingDelta(security, pricingStatus.lastClose, subscription.getLastClose()));

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
	public void addPricingListener(IPricingListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IPricingEnvironment#removePricingListener(org.eclipsetrader.core.feed.IPricingListener)
	 */
	public void removePricingListener(IPricingListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IPricingEnvironment#dispose()
	 */
	public void dispose() {
	    for (IMarket market : marketService.getMarkets()) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) market.getAdapter(PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
	    }

	    listeners.clear();

    	for (Iterator<Entry<IFeedIdentifier, SubscriptionStatus>> iter = identifiersMap.entrySet().iterator(); iter.hasNext(); ) {
    		Entry<IFeedIdentifier, SubscriptionStatus> entry = iter.next();

    		for (IFeedSubscription subscription : entry.getValue().subscriptions.values()) {
				subscription.removeSubscriptionListener(listener);
				subscription.dispose();
    		}

			PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) entry.getKey().getAdapter(PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    	}

    	identifiersMap.clear();
    	securitiesMap.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IPricingEnvironment#getTrade(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public ITrade getTrade(ISecurity security) {
		return securitiesMap.get(security) != null ? securitiesMap.get(security).trade : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IPricingEnvironment#getQuote(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public IQuote getQuote(ISecurity security) {
		return securitiesMap.get(security) != null ? securitiesMap.get(security).quote : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IPricingEnvironment#getTodayOHL(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public ITodayOHL getTodayOHL(ISecurity security) {
		return securitiesMap.get(security) != null ? securitiesMap.get(security).todayOHL : null;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getLastClose(org.eclipsetrader.core.instruments.ISecurity)
     */
    public ILastClose getLastClose(ISecurity security) {
		return securitiesMap.get(security) != null ? securitiesMap.get(security).lastClose : null;
    }

	protected void processUpdateQuotes(IFeedIdentifier identifier, QuoteDelta[] delta) {
		SubscriptionStatus subscriptionStatus = identifiersMap.get(identifier);
		if (subscriptionStatus != null) {
			for (ISecurity security : subscriptionStatus.securities) {
				PricingStatus pricingStatus = securitiesMap.get(security);
				if (pricingStatus != null) {
					for (QuoteDelta d : delta) {
						if (d.getNewValue() instanceof ITrade) {
							Object oldValue = pricingStatus.trade;
							if ((oldValue == null && d.getNewValue() != null) || (oldValue != null && !oldValue.equals(d.getNewValue()))) {
								pricingStatus.trade = (ITrade) d.getNewValue();
								pricingStatus.deltas.add(new PricingDelta(security, oldValue, d.getNewValue()));
							}
						}
						if (d.getNewValue() instanceof IQuote) {
							Object oldValue = pricingStatus.quote;
							if ((oldValue == null && d.getNewValue() != null) || (oldValue != null && !oldValue.equals(d.getNewValue()))) {
								pricingStatus.quote = (IQuote) d.getNewValue();
								pricingStatus.deltas.add(new PricingDelta(security, oldValue, d.getNewValue()));
							}
						}
						if (d.getNewValue() instanceof ITodayOHL) {
							Object oldValue = pricingStatus.todayOHL;
							if ((oldValue == null && d.getNewValue() != null) || (oldValue != null && !oldValue.equals(d.getNewValue()))) {
								pricingStatus.todayOHL = (ITodayOHL) d.getNewValue();
								pricingStatus.deltas.add(new PricingDelta(security, oldValue, d.getNewValue()));
							}
						}
						if (d.getNewValue() instanceof ILastClose) {
							Object oldValue = pricingStatus.lastClose;
							if ((oldValue == null && d.getNewValue() != null) || (oldValue != null && !oldValue.equals(d.getNewValue()))) {
								pricingStatus.lastClose = (ILastClose) d.getNewValue();
								pricingStatus.deltas.add(new PricingDelta(security, oldValue, d.getNewValue()));
							}
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
		synchronized(securitiesMap) {
			Set<ISecurity> set = securitiesMap.keySet();
			securities = set.toArray(new ISecurity[set.size()]);
		}

		for (ISecurity security : securities) {
			PricingStatus pricingStatus = securitiesMap.get(security);
			if (pricingStatus == null || pricingStatus.deltas.size() == 0)
				continue;
			PricingEvent event = new PricingEvent(security, pricingStatus.deltas.toArray(new PricingDelta[pricingStatus.deltas.size()]));
    		for (int i = 0; i < l.length; i++) {
        		try {
        			((IPricingListener) l[i]).pricingUpdate(event);
        		} catch(Exception e) {
        			Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment listener", e); //$NON-NLS-1$
        			CoreActivator.log(status);
        		} catch (LinkageError e) {
        			Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment listener", e); //$NON-NLS-1$
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
}
