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

package org.eclipsetrader.internal.ui.trading.portfolio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.PositionEvent;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.core.views.ViewItemDelta;
import org.eclipsetrader.internal.ui.trading.Activator;

public class PortfolioView extends PlatformObject implements IView {

    ITradingService tradingService;
    IMarketService marketService;
    IBroker[] broker;

    List<BrokerElement> items;
    MarketPricingEnvironment pricingEnvironment;
    ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    IPositionListener positionListener = new IPositionListener() {

        @Override
        public void positionChanged(PositionEvent e) {
            updateView();
        }

        @Override
        public void positionClosed(PositionEvent e) {
            updateView();
        }

        @Override
        public void positionOpened(PositionEvent e) {
            updateView();
        }
    };

    public PortfolioView() {
        items = new ArrayList<BrokerElement>();
    }

    public PortfolioView(ITradingService tradingService, IMarketService marketService) {
        this.tradingService = tradingService;
        this.marketService = marketService;

        this.broker = tradingService.getBrokers();

        items = new ArrayList<BrokerElement>();
        for (int i = 0; i < broker.length; i++) {
            items.add(new BrokerElement(broker[i]));
        }

        final Set<ISecurity> list = new HashSet<ISecurity>();
        accept(new IViewVisitor() {

            @Override
            public boolean visit(IView view) {
                return true;
            }

            @Override
            public boolean visit(IViewItem viewItem) {
                IAccount account = (IAccount) viewItem.getAdapter(IAccount.class);
                if (account != null) {
                    account.addPositionListener(positionListener);
                }

                ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
                if (security != null && !list.contains(security)) {
                    list.add(security);
                }
                return true;
            }
        });

        pricingEnvironment = new MarketPricingEnvironment(marketService);
        pricingEnvironment.addSecurities(list.toArray(new ISecurity[list.size()]));
        pricingEnvironment.addPricingListener(new IPricingListener() {

            @Override
            public void pricingUpdate(PricingEvent event) {
                doPricingUpdate(event);
            }
        });

        accept(new IViewVisitor() {

            @Override
            public boolean visit(IView view) {
                return true;
            }

            @Override
            public boolean visit(IViewItem viewItem) {
                PositionElement element = (PositionElement) viewItem.getAdapter(PositionElement.class);
                ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
                if (element != null && security != null) {
                    ITrade trade = pricingEnvironment.getTrade(security);
                    if (trade != null) {
                        element.setTrade(trade);
                    }
                }
                return true;
            }
        });
    }

    protected void doPricingUpdate(final PricingEvent event) {
        final Set<ViewItemDelta> viewDelta = new HashSet<ViewItemDelta>();

        accept(new IViewVisitor() {

            @Override
            public boolean visit(IView view) {
                return true;
            }

            @Override
            public boolean visit(IViewItem viewItem) {
                ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
                if (security == null || !event.getSecurity().equals(security)) {
                    return true;
                }

                PositionElement element = (PositionElement) viewItem.getAdapter(PositionElement.class);
                if (element != null) {
                    for (PricingDelta delta : event.getDelta()) {
                        if (delta.getNewValue() instanceof ITrade) {
                            element.setTrade((ITrade) delta.getNewValue());
                            viewDelta.add(new ViewItemDelta(ViewItemDelta.CHANGED, viewItem));
                        }
                    }
                }
                return true;
            }
        });

        if (viewDelta.size() != 0) {
            fireViewChangedEvent(viewDelta.toArray(new ViewItemDelta[viewDelta.size()]));
        }
    }

    void updateView() {
        accept(new IViewVisitor() {

            @Override
            public boolean visit(IView view) {
                return true;
            }

            @Override
            public boolean visit(IViewItem viewItem) {
                IAccount account = (IAccount) viewItem.getAdapter(IAccount.class);
                if (account != null) {
                    account.removePositionListener(positionListener);
                }
                return true;
            }
        });

        List<BrokerElement> items = new ArrayList<BrokerElement>();
        for (int i = 0; i < broker.length; i++) {
            items.add(new BrokerElement(broker[i]));
        }

        this.items = items;

        final Set<ISecurity> list = new HashSet<ISecurity>();
        accept(new IViewVisitor() {

            @Override
            public boolean visit(IView view) {
                return true;
            }

            @Override
            public boolean visit(IViewItem viewItem) {
                IAccount account = (IAccount) viewItem.getAdapter(IAccount.class);
                if (account != null) {
                    account.addPositionListener(positionListener);
                }

                ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
                if (security != null && !list.contains(security)) {
                    list.add(security);
                }

                return true;
            }
        });

        pricingEnvironment.addSecurities(list.toArray(new ISecurity[list.size()]));

        accept(new IViewVisitor() {

            @Override
            public boolean visit(IView view) {
                return true;
            }

            @Override
            public boolean visit(IViewItem viewItem) {
                PositionElement element = (PositionElement) viewItem.getAdapter(PositionElement.class);
                ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
                if (element != null && security != null) {
                    ITrade trade = pricingEnvironment.getTrade(security);
                    if (trade != null) {
                        element.setTrade(trade);
                    }
                }
                return true;
            }
        });

        fireViewChangedEvent(new ViewItemDelta[0]);
    }

    protected void fireViewChangedEvent(ViewItemDelta[] delta) {
        ViewEvent e = new ViewEvent(this, delta);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IViewChangeListener) l[i]).viewChanged(e);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error notifiying listeners", t); //$NON-NLS-1$
                Activator.log(status);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#accept(org.eclipsetrader.core.views.IViewVisitor)
     */
    @Override
    public void accept(IViewVisitor visitor) {
        if (visitor.visit(this)) {
            for (IViewItem viewItem : items) {
                viewItem.accept(visitor);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#addViewChangeListener(org.eclipsetrader.core.views.IViewChangeListener)
     */
    @Override
    public void addViewChangeListener(IViewChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#dispose()
     */
    @Override
    public void dispose() {
        listeners.clear();

        if (pricingEnvironment != null) {
            pricingEnvironment.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#getItems()
     */
    @Override
    public IViewItem[] getItems() {
        return items.toArray(new IViewItem[items.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#removeViewChangeListener(org.eclipsetrader.core.views.IViewChangeListener)
     */
    @Override
    public void removeViewChangeListener(IViewChangeListener listener) {
        listeners.remove(listener);
    }
}
