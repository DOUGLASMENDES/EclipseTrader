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
import net.sourceforge.eclipsetrader.core.db.OrderStatus;

import org.eclipse.jface.viewers.LabelProvider;

public class StatusColumn extends LabelProvider
{
    static Map labels = new HashMap();
    static {
        labels.put(OrderStatus.NEW, "New");
        labels.put(OrderStatus.PARTIAL, "Partial");
        labels.put(OrderStatus.FILLED, "Filled");
        labels.put(OrderStatus.CANCELED, "Canceled");
        labels.put(OrderStatus.REJECTED, "Rejected");
        labels.put(OrderStatus.PENDING_CANCEL, "Pending Cancel");
        labels.put(OrderStatus.PENDING_NEW, "Pending New");
    }

    public StatusColumn()
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
            String text = (String)labels.get(order.getStatus());
            return text != null ? text : "";
        }
        return "";
    }
}
