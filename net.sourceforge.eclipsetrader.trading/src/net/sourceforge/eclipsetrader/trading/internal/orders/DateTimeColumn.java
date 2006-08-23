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

import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider;

public class DateTimeColumn implements IOrdersLabelProvider
{
    private SimpleDateFormat dateTimeFormat = CorePlugin.getDateTimeFormat();

    public DateTimeColumn()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getHeaderText()
     */
    public String getHeaderText()
    {
        return "Date / Time";
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
        return dateTimeFormat.format(order.getDate());
    }
}