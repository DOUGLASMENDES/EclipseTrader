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

import org.eclipse.swt.SWT;

import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider;

public class StatusColumn implements IOrdersLabelProvider
{

    public StatusColumn()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getHeaderText()
     */
    public String getHeaderText()
    {
        return "Status";
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
        switch(order.getStatus())
        {
            case Order.STATUS_NEW:
                return "New";
            case Order.STATUS_PARTIAL:
                return "Partial";
            case Order.STATUS_FILLED:
                return "Filled";
            case Order.STATUS_CANCELED:
                return "Cancelled";
            case Order.STATUS_REJECTED:
                return "Rejected";
            case Order.STATUS_PENDING_CANCEL:
                return "Cancel Pending";
            case Order.STATUS_PENDING_NEW:
                return "Pending";
            default:
                return String.valueOf(order.getStatus());
        }
    }
}
