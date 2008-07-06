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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipsetrader.core.trading.IOrder;

public class RouteColumn extends LabelProvider {

	public RouteColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrder) {
			IOrder order = (IOrder) element;
			if (order.getRoute() != null)
				return order.getRoute().toString();
		}
		return "";
	}
}
