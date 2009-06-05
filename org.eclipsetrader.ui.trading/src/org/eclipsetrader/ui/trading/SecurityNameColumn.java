/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.trading;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;

public class SecurityNameColumn extends ColumnLabelProvider {
	public static final String COLUMN_ID = "org.eclipsetrader.ui.trading.orders.security";

	public SecurityNameColumn() {
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

		if (order != null && order.getSecurity() != null)
			return order.getSecurity().getName();

		return "";
	}
}
