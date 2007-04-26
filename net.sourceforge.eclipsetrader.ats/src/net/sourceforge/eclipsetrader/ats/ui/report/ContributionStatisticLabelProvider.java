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

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class ContributionStatisticLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
	Color negativeForeground = new Color(null, 240, 0, 0);

	Color positiveForeground = new Color(null, 0, 192, 0);

	NumberFormat nf = NumberFormat.getInstance();

	public ContributionStatisticLabelProvider() {
		nf.setGroupingUsed(true);
		nf.setMinimumIntegerDigits(1);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
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
		ContributionStatistic c = (ContributionStatistic) element;

		switch (columnIndex) {
			case 0:
				return c.name;
			case 1:
				return nf.format(c.grossProfit + c.grossLoss);
			case 2:
				return nf.format(c.grossProfit);
			case 3:
				return nf.format(c.grossLoss);
			case 4:
				return nf.format((double) c.winningTrades / (double) (c.winningTrades + c.losingTrades) * 100.0);
			case 5:
				return nf.format(c.totalAmount / (c.winningTrades + c.losingTrades));
			case 6:
				return String.valueOf(c.winningTrades + c.losingTrades);
			case 7:
				return String.valueOf(c.winningTrades);
			case 8:
				return String.valueOf(c.losingTrades);
		}

		return "";
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
		ContributionStatistic c = (ContributionStatistic) element;

		switch (columnIndex) {
			case 1:
				return (c.grossProfit + c.grossLoss) >= 0 ? positiveForeground : negativeForeground;
		}

		return null;
	}
}
