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
import org.eclipsetrader.core.trading.OrderValidity;

public class ValidityColumn extends ColumnLabelProvider {
	static Map<OrderValidity, String> labels = new HashMap<OrderValidity, String>();
	static {
		labels.put(OrderValidity.Day, "Day");
		labels.put(OrderValidity.ImmediateOrCancel, "Imm. or Cancel");
		labels.put(OrderValidity.AtOpening, "At Opening");
		labels.put(OrderValidity.AtClosing, "At Closing");
		labels.put(OrderValidity.GoodTillDate, "Good Till Cancel");
	}

	public ValidityColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrder) {
			IOrder order = (IOrder) element;
			String text = labels.get(order.getValidity());
			return text != null ? text : order.getValidity().toString();
		}
		return "";
	}
}
