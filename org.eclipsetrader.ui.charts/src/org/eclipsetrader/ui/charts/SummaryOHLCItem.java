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

package org.eclipsetrader.ui.charts;

import java.text.NumberFormat;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.feed.IOHLC;

public class SummaryOHLCItem {
    private Label label;
	private Label changeLabel;

    private NumberFormat numberFormat = NumberFormat.getInstance();
    private NumberFormat percentFormat = NumberFormat.getInstance();
    private Color foreground;
    private Color positiveForeground;
    private Color negativeForeground;

	public SummaryOHLCItem(Composite parent, int style) {
		changeLabel = new Label(parent, SWT.NONE);
		label = new Label(parent, SWT.NONE);

		foreground = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		positiveForeground = parent.getDisplay().getSystemColor(SWT.COLOR_GREEN);
		negativeForeground = parent.getDisplay().getSystemColor(SWT.COLOR_RED);

		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(4);
		numberFormat.setGroupingUsed(true);

		percentFormat.setMinimumFractionDigits(2);
		percentFormat.setMaximumFractionDigits(2);
		percentFormat.setGroupingUsed(false);
	}

	public Color getForeground() {
    	return foreground;
    }

	public void setForeground(Color color) {
    	this.foreground = color;
    }

	public Color getPositiveForeground() {
    	return positiveForeground;
    }

	public void setPositiveForeground(Color positiveColor) {
    	this.positiveForeground = positiveColor;
    }

	public Color getNegativeForeground() {
    	return negativeForeground;
    }

	public void setNegativeForeground(Color negativeColor) {
    	this.negativeForeground = negativeColor;
    }

	public void setOHLC(IOHLC currentOHLC, IOHLC previousOHLC) {
		label.setText(NLS.bind("O={0} H={1} L={2} C={3}", new Object[] {
				numberFormat.format(currentOHLC.getOpen()),
				numberFormat.format(currentOHLC.getHigh()),
				numberFormat.format(currentOHLC.getLow()),
				numberFormat.format(currentOHLC.getClose()),
		}));
		label.setForeground(foreground);
		if (previousOHLC != null) {
			double change = (currentOHLC.getClose() - previousOHLC.getClose()) / previousOHLC.getClose() * 100.0;
			changeLabel.setText(NLS.bind("{0}%", new Object[] {
					(change > 0 ? "+" : "") + percentFormat.format(change),
			}));
			if (change > 0)
				changeLabel.setForeground(positiveForeground);
			else if (change < 0)
				changeLabel.setForeground(negativeForeground);
			else
				changeLabel.setForeground(null);
		}
		label.getParent().layout();
	}
}
