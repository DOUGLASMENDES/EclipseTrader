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
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.OrderType;

public class TypeColumn extends ColumnLabelProvider {
	static Map<OrderType, String> labels = new HashMap<OrderType, String>();
	static {
		labels.put(OrderType.Limit, Messages.TypeColumn_Limit);
		labels.put(OrderType.Market, Messages.TypeColumn_Market);
		labels.put(OrderType.Stop, Messages.TypeColumn_Stop);
		labels.put(OrderType.StopLimit, Messages.TypeColumn_StopLimit);
	}

	public TypeColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		IOrder order = null;
		if (element instanceof IOrder)
			order = (IOrder) element;
		else if (element instanceof IOrderMonitor)
			order = ((IOrderMonitor) element).getOrder();

		if (order != null) {
			String text = labels.get(order.getType());
			return text != null ? text : order.getType().toString();
		}

		return ""; //$NON-NLS-1$
	}
}
