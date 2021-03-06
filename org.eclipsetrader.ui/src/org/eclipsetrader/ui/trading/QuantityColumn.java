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

package org.eclipsetrader.ui.trading;

import java.text.NumberFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;

public class QuantityColumn extends ColumnLabelProvider {

    public static final String COLUMN_ID = "org.eclipsetrader.ui.trading.orders.qty"; //$NON-NLS-1$

    private NumberFormat formatter = NumberFormat.getInstance();

    public QuantityColumn() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        IOrder order = null;
        if (element instanceof IOrder) {
            order = (IOrder) element;
        }
        else if (element instanceof IOrderMonitor) {
            order = ((IOrderMonitor) element).getOrder();
        }

        if (order != null && order.getQuantity() != null) {
            return formatter.format(order.getQuantity());
        }

        return ""; //$NON-NLS-1$
    }
}
