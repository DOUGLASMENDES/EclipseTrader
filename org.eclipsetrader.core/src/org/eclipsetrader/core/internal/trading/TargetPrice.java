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

package org.eclipsetrader.core.internal.trading;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.trading.IAlert;

public class TargetPrice implements IExecutableExtension, IAlert, IAdaptable {

    public static final String PLUGIN_ID = "org.eclipsetrader.core.trading.alerts.targetprice";

    public static final int F_LAST = 0;
    public static final int F_BID = 1;
    public static final int F_ASK = 2;

    public static final String K_FIELD = "field";
    public static final String K_PRICE = "price";
    public static final String K_CROSS = "cross";

    private String id = PLUGIN_ID;
    private String name = "Target Price";

    int field;
    double price;
    boolean cross;
    String description;

    double initialPrice;
    boolean triggered;

    public static String getDescriptionFor(Map<String, Object> map) {
        int field = (Integer) map.get(K_FIELD);
        double price = (Double) map.get(K_PRICE);
        boolean cross = (Boolean) map.get(K_CROSS);

        return NLS.bind("{0} price {1} {2}", new Object[] {
                field == F_LAST ? "Last" : field == F_BID ? "Bid" : "Ask",
                cross ? "crosses" : "reaches",
                NumberFormat.getInstance().format(price),
        });
    }

    public TargetPrice() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Object> map) {
        field = (Integer) map.get(K_FIELD);
        price = (Double) map.get(K_PRICE);
        cross = (Boolean) map.get(K_CROSS);
        description = getDescriptionFor(map);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#getParameters()
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(K_FIELD, field);
        map.put(K_PRICE, price);
        map.put(K_CROSS, cross);
        return map;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#setInitialValues(org.eclipsetrader.core.feed.ITrade, org.eclipsetrader.core.feed.IQuote)
     */
    @Override
    public void setInitialValues(ITrade trade, IQuote quote) {
        switch (field) {
            case F_LAST:
                if (trade == null || trade.getPrice() == null) {
                    return;
                }
                initialPrice = trade.getPrice();
                break;
            case F_BID:
                if (quote == null || quote.getBid() == null) {
                    return;
                }
                initialPrice = quote.getBid();
                break;
            case F_ASK:
                if (quote == null || quote.getAsk() == null) {
                    return;
                }
                initialPrice = quote.getAsk();
                break;
        }
        triggered = false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#setTrade(org.eclipsetrader.core.feed.ITrade)
     */
    @Override
    public void setTrade(ITrade trade) {
        if (field != F_LAST) {
            return;
        }
        if (trade == null || trade.getPrice() == null) {
            return;
        }
        updateTrigger(trade.getPrice());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#setQuote(org.eclipsetrader.core.feed.IQuote)
     */
    @Override
    public void setQuote(IQuote quote) {
        if (field != F_LAST && field != F_BID) {
            return;
        }
        if (quote == null) {
            return;
        }

        if (field == F_BID) {
            if (quote.getBid() == null) {
                return;
            }
            updateTrigger(quote.getBid());
        }
        else if (field == F_ASK) {
            if (quote.getAsk() == null) {
                return;
            }
            updateTrigger(quote.getAsk());
        }
    }

    void updateTrigger(double value) {
        if (initialPrice == 0.0) {
            initialPrice = value;
        }

        if (cross) {
            if (price > initialPrice) {
                triggered = value > initialPrice && value > price;
            }
            if (price < initialPrice) {
                triggered = value < initialPrice && value < price;
            }
        }
        else {
            if (price > initialPrice) {
                triggered = value > initialPrice && value >= price;
            }
            if (price < initialPrice) {
                triggered = value < initialPrice && value <= price;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlert#isTriggered()
     */
    @Override
    public boolean isTriggered() {
        return triggered;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
