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
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.OrderStatus;

public class StatusColumn extends ColumnLabelProvider {
	static Map<OrderStatus, String> labels = new HashMap<OrderStatus, String>();
	static {
		labels.put(OrderStatus.New, "New");
		labels.put(OrderStatus.Partial, "Partial");
		labels.put(OrderStatus.Filled, "Filled");
		labels.put(OrderStatus.Canceled, "Canceled");
		labels.put(OrderStatus.Rejected, "Rejected");
		labels.put(OrderStatus.PendingCancel, "Pending Cancel");
		labels.put(OrderStatus.PendingNew, "Pending New");
		labels.put(OrderStatus.Expired, "Expired");
	}

	public StatusColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrder) {
			IOrder order = (IOrder) element;
			String text = labels.get(order.getStatus());
			return text != null ? text : order.getStatus().toString();
		}
		return "";
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
	    return IOrder.PROP_STATUS.equals(property);
    }
}
