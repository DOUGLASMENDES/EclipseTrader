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

import net.sourceforge.eclipsetrader.core.db.Order;

import org.eclipse.jface.viewers.LabelProvider;

public class TradingProviderColumn extends LabelProvider
{

    public TradingProviderColumn()
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
            if (order.getProvider() != null)
                return order.getProvider().getName();
        }
        return "";
    }
}
