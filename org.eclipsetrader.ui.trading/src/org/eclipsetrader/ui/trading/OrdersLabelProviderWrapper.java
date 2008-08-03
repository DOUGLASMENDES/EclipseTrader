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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderStatus;

public class OrdersLabelProviderWrapper extends CellLabelProvider {
    private Color canceledColor;
    private Color rejectedColor;
    private Color filledColor;
    private Color partialColor;

    private ColumnLabelProvider labelProvider;

	public OrdersLabelProviderWrapper(ColumnLabelProvider labelProvider) {
		this.labelProvider = labelProvider;

		canceledColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	    rejectedColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	    filledColor = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	    partialColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
		Object element = cell.getElement();
		cell.setText(labelProvider.getText(element));
		cell.setImage(labelProvider.getImage(element));
		cell.setBackground(labelProvider.getBackground(element));

		Color color = labelProvider.getForeground(element);
		cell.setForeground(color != null ? color : getForeground(element));

		cell.setFont(labelProvider.getFont(element));
    }

    public Color getForeground(Object element) {
		IOrderMonitor order = (IOrderMonitor) element;
		if (order.getStatus() == IOrderStatus.Canceled || order.getStatus() == IOrderStatus.Expired)
			return canceledColor;
		if (order.getStatus() == IOrderStatus.Rejected)
			return rejectedColor;
		if (order.getStatus() == IOrderStatus.Filled)
			return filledColor;
		if (order.getStatus() == IOrderStatus.Partial)
			return partialColor;
		return null;
	}
}
