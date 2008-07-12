/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.trading;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.OrderStatus;

public class StatusColumn extends ColumnLabelProvider {
	static Map<OrderStatus, String> labels = new HashMap<OrderStatus, String>();
	static {
		labels.put(OrderStatus.New, Messages.StatusColumn_New);
		labels.put(OrderStatus.Partial, Messages.StatusColumn_Partial);
		labels.put(OrderStatus.Filled, Messages.StatusColumn_Filled);
		labels.put(OrderStatus.Canceled, Messages.StatusColumn_Canceled);
		labels.put(OrderStatus.Rejected, Messages.StatusColumn_Rejected);
		labels.put(OrderStatus.PendingCancel, Messages.StatusColumn_PendingCancel);
		labels.put(OrderStatus.PendingNew, Messages.StatusColumn_PendingNew);
		labels.put(OrderStatus.Expired, Messages.StatusColumn_Expired);
	}

	public StatusColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrderMonitor) {
			IOrderMonitor order = (IOrderMonitor) element;
			String text = labels.get(order.getStatus());
			return text != null ? text : order.getStatus().toString();
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
	    return IOrderMonitor.PROP_STATUS.equals(property);
    }
}
