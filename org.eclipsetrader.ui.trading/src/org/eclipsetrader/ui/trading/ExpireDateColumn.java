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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipsetrader.core.trading.IOrder;

public class ExpireDateColumn extends LabelProvider {
	protected DateFormat formatter = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

	public ExpireDateColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOrder) {
			IOrder order = (IOrder) element;
			if (order.getExpire() != null)
				return formatter.format(order.getDate());
		}
		return "";
	}
}
