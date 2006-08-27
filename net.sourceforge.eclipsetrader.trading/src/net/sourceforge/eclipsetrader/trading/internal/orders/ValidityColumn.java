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

public class ValidityColumn implements IOrdersLabelProvider
{

    public ValidityColumn()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getHeaderText()
     */
    public String getHeaderText()
    {
        return "Valid";
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
        switch(order.getValidity())
        {
            case Order.VALID_DAY:
                return "Day";
            case Order.VALID_IMMEDIATE_OR_CANCEL:
                return "Imm. or cancel";
            case Order.VALID_OPENING:
                return "At Opening";
            case Order.VALID_CLOSING:
                return "At Closing";
            default:
                return String.valueOf(order.getValidity());
        }
    }
}
