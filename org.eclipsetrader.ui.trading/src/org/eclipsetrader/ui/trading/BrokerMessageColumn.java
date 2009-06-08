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
import org.eclipsetrader.core.trading.IOrderMonitor;

public class BrokerMessageColumn extends ColumnLabelProvider {
	public static final String COLUMN_ID = "org.eclipsetrader.ui.trading.orders.message"; //$NON-NLS-1$

	public BrokerMessageColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrderMonitor) {
			IOrderMonitor order = (IOrderMonitor) element;
			if (order.getMessage() != null)
				return order.getMessage();
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return IOrderMonitor.PROP_MESSAGE.equals(property);
	}
}
