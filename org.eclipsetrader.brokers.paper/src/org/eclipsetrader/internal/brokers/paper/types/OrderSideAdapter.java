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

package org.eclipsetrader.internal.brokers.paper.types;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.trading.IOrderSide;

public class OrderSideAdapter extends XmlAdapter<String, IOrderSide> {

    public OrderSideAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IOrderSide v) throws Exception {
        return v != null ? v.getId() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IOrderSide unmarshal(String v) throws Exception {
        if (IOrderSide.Buy.getId().equals(v)) {
            return IOrderSide.Buy;
        }
        if (IOrderSide.Sell.getId().equals(v)) {
            return IOrderSide.Sell;
        }
        if (IOrderSide.SellShort.getId().equals(v)) {
            return IOrderSide.SellShort;
        }
        if (IOrderSide.BuyCover.getId().equals(v)) {
            return IOrderSide.BuyCover;
        }
        return null;
    }
}
