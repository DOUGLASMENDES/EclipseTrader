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
import net.sourceforge.eclipsetrader.core.db.OrderType;
import net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider;

import org.eclipse.swt.SWT;

public class TypeColumn implements IOrdersLabelProvider
{
    static Map labels = new HashMap();
    static {
        labels.put(OrderType.LIMIT, "Limit");
        labels.put(OrderType.MARKET, "Market");
        labels.put(OrderType.STOP, "Stop");
        labels.put(OrderType.STOPLIMIT, "Stop Limit");
    }

    public TypeColumn()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getHeaderText()
     */
    public String getHeaderText()
    {
        return "Type";
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getStyle()
     */
    public int getStyle()
    {
        return SWT.LEFT;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getText(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public String getText(Order order)
    {
        String text = (String)labels.get(order.getType());
        return text != null ? text : "";
    }
}
