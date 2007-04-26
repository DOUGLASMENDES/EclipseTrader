/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui.report;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.internal.Trade;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class TradeLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
	Color negativeForeground = new Color(null, 240, 0, 0);

	Color positiveForeground = new Color(null, 0, 192, 0);

	NumberFormat nf = NumberFormat.getInstance();

	NumberFormat pf = NumberFormat.getInstance();

	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

	public TradeLabelProvider() {
		nf.setGroupingUsed(true);
		nf.setMinimumIntegerDigits(1);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		pf.setGroupingUsed(true);
		pf.setMinimumIntegerDigits(1);
		pf.setMinimumFractionDigits(4);
		pf.setMaximumFractionDigits(4);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		Trade trade = (Trade) element;

		switch (columnIndex) {
			case 0:
				return trade.getSecurity().getCode();
			case 1:
				return trade.getSecurity().getDescription();
			case 2:
				if (trade.getStrategy().getName() == null)
					return ATSPlugin.getStrategyPluginName(trade.getStrategy().getPluginId());
				return trade.getStrategy().getName();
			case 3:
				return String.valueOf(trade.getQuantity());
			case 4:
				return String.valueOf(trade.getBars());
			case 5:
				return pf.format(trade.getEnterPrice());
			case 6:
				return df.format(trade.getEnterDate());
			case 7:
				return trade.getEnterMessage();
			case 8:
				return pf.format(trade.getExitPrice());
			case 9:
				return df.format(trade.getExitDate());
			case 10:
				return trade.getExitMessage();
			case 11: {
				double amount = (trade.getExitPrice() * trade.getQuantity()) - (trade.getEnterPrice() * trade.getQuantity());
				return nf.format(amount);
			}
			case 12: {
				double paid = trade.getEnterPrice() * trade.getQuantity();
				double amount = (trade.getExitPrice() * trade.getQuantity()) - paid;
				return nf.format(amount / paid * 100) + "%";
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		Trade trade = (Trade) element;
		if (columnIndex == 11 || columnIndex == 12) {
			double amount = (trade.getExitPrice() * trade.getQuantity()) - (trade.getEnterPrice() * trade.getQuantity());
			return amount >= 0 ? positiveForeground : negativeForeground;
		}
		return null;
	}
}
