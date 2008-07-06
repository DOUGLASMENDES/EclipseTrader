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
import org.eclipsetrader.core.trading.OrderSide;

public class SideColumn extends ColumnLabelProvider {
	static Map<OrderSide, String> labels = new HashMap<OrderSide, String>();
	static {
		labels.put(OrderSide.Buy, "Buy");
		labels.put(OrderSide.Sell, "Sell");
		labels.put(OrderSide.SellShort, "Sell Short");
		labels.put(OrderSide.BuyCover, "Buy Cover");
	}

	public SideColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrder) {
			IOrder order = (IOrder) element;
			String text = labels.get(order.getSide());
			return text != null ? text : order.getSide().toString();
		}
		return "";
	}
}
