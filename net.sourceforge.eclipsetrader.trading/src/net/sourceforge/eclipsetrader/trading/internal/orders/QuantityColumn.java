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

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.core.db.Order;

import org.eclipse.jface.viewers.LabelProvider;

public class QuantityColumn extends LabelProvider
{
    private NumberFormat numberFormat = NumberFormat.getInstance();

    public QuantityColumn()
    {
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        if (element instanceof Order)
        {
            Order order = (Order)element;
            return numberFormat.format(order.getQuantity());
        }
        return "";
    }
}
