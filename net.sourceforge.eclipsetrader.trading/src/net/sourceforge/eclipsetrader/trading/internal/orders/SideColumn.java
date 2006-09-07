/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.internal.orders;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderSide;

import org.eclipse.jface.viewers.LabelProvider;

public class SideColumn extends LabelProvider
{
    static Map labels = new HashMap();
    static {
        labels.put(OrderSide.BUY, "Buy");
        labels.put(OrderSide.SELL, "Sell");
        labels.put(OrderSide.SELLSHORT, "Sell Short");
        labels.put(OrderSide.BUYCOVER, "Buy Cover");
    }

    public SideColumn()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        if (element instanceof Order)
        {
            Order order = (Order)element;
            String text = (String)labels.get(order.getSide());
            return text != null ? text : "";
        }
        return "";
    }
}
