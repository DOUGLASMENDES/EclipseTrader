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

package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.Order;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public abstract class BaseOrderFunction extends ScriptableObject {

    private static final long serialVersionUID = 7916839001932000909L;

    public static final int Buy = 0;
    public static final int Sell = 1;
    public static final int Limit = 0;
    public static final int Market = 1;

    public static final String PROPERTY_ACCOUNT = "account";
    public static final String PROPERTY_BROKER = "broker";
    public static final String PROPERTY_INSTRUMENT = "instrument";

    private IBroker broker;
    private IAccount account;
    private ISecurity instrument;

    protected IOrderType type;
    protected IOrderSide side;
    protected long quantity;
    protected Double price;
    protected String text;

    private IOrderMonitor monitor;

    public BaseOrderFunction() {
    }

    public BaseOrderFunction(IBroker broker, IAccount account, ISecurity instrument) {
        this.broker = broker;
        this.account = account;
        this.instrument = instrument;
    }

    public double jsGet_type() {
        if (type == IOrderType.Limit) {
            return Limit;
        }
        if (type == IOrderType.Market) {
            return Market;
        }
        return -1;
    }

    public void jsSet_type(Object arg) {
        switch ((int) Context.toNumber(arg)) {
            case Limit:
                type = IOrderType.Limit;
                break;
            case Market:
                type = IOrderType.Market;
                break;
        }
    }

    public double jsGet_side() {
        if (side == IOrderSide.Buy) {
            return Buy;
        }
        if (side == IOrderSide.Sell) {
            return Sell;
        }
        return -1;
    }

    public void jsSet_side(Object arg) {
        switch ((int) Context.toNumber(arg)) {
            case Buy:
                side = IOrderSide.Buy;
                break;
            case Sell:
                side = IOrderSide.Sell;
                break;
        }
    }

    public double jsGet_quantity() {
        return quantity;
    }

    public void jsSet_quantity(Object arg) {
        quantity = (int) Context.toNumber(arg);
    }

    public double jsGet_price() {
        return price;
    }

    public void jsSet_price(Object arg) {
        price = Context.toNumber(arg);
    }

    public String jsGet_text() {
        return text;
    }

    public void jsSet_text(Object arg) {
        text = Context.toString(arg);
    }

    public Object jsFunction_send() throws Exception {
        Order order = new Order(account, type, side, instrument, quantity, price);
        order.setReference(text);

        monitor = broker.prepareOrder(order);
        monitor.submit();

        return null;
    }

    public Object jsFunction_cancel() throws Exception {
        if (monitor != null) {
            monitor.cancel();
        }
        return null;
    }
}
