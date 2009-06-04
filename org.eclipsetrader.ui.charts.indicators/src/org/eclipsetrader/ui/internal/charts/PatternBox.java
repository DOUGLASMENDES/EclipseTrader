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

package org.eclipsetrader.ui.internal.charts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.ui.charts.ChartObject;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.IGraphics;

public class PatternBox extends ChartObject {
	IOHLC[] bars;
	RGB color;
	String title;
	String label;
	double highest = Double.MIN_VALUE;
	double lowest = Double.MAX_VALUE;

	int x1;
	int x2;
	int y1;
	int y2;
	Rectangle r1;
	Rectangle r2;
	boolean selected;

	public PatternBox(IOHLC[] bars, RGB color, String title, String label) {
		this.bars = bars;
		this.color = color;
		this.title = title;
		this.label = label;

		for (int i = 0; i < bars.length; i++) {
			highest = Math.max(highest, bars[i].getHigh());
			lowest = Math.min(lowest, bars[i].getLow());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.ChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
	 */
	@Override
	public void paint(IGraphics graphics) {
		x1 = graphics.mapToHorizontalAxis(bars[0].getDate());
		x2 = graphics.mapToHorizontalAxis(bars[bars.length - 1].getDate());
		y1 = graphics.mapToVerticalAxis(highest) - 10;
		y2 = graphics.mapToVerticalAxis(lowest) + 10;

		graphics.setForegroundColor(color);

		Font oldFont = graphics.getFont();
		Font font = null;
		if (selected) {
			graphics.setLineWidth(2);
			FontData fontData = oldFont.getFontData()[0];
			font = new Font(Display.getCurrent(), fontData.getName(), fontData.getHeight(), SWT.BOLD);
			graphics.setFont(font);
		}

		Point e1 = graphics.stringExtent(title);
		Point e2 = graphics.stringExtent(label);

		r1 = new Rectangle(x1, y1 - e1.y - e2.y, e1.x, e1.y);
		graphics.drawString(title, x1, y1 - e1.y - e2.y);
		r2 = new Rectangle(x1, y1 - e2.y, e2.x, e2.y);
		graphics.drawString(label, x1, y1 - e2.y);

		graphics.drawLine(x1, y1, x2, y1);
		graphics.drawLine(x1, y1, x1, y1 + 5);
		graphics.drawLine(x2, y1, x2, y1 + 5);

		graphics.drawLine(x1, y2, x1, y2 - 5);
		graphics.drawLine(x2, y2, x2, y2 - 5);
		graphics.drawLine(x1, y2, x2, y2);

		if (font != null) {
			graphics.setFont(oldFont);
			font.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.ChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
	 */
	@Override
	public void handleFocusGained(ChartObjectFocusEvent event) {
		selected = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.ChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
	 */
	@Override
	public void handleFocusLost(ChartObjectFocusEvent event) {
		selected = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.ChartObject#containsPoint(int, int)
	 */
	@Override
	public boolean containsPoint(int x, int y) {
		if (x >= x1 && x <= x2) {
			if (Math.abs(y - y1) <= 2 || Math.abs(y - y2) <= 2)
				return true;
		}

		if (x >= r1.x && x < (r1.x + r1.width)) {
			if (y >= r1.y && y < (r1.y + r1.height))
				return true;
		}

		if (x >= r2.x && x < (r2.x + r2.width)) {
			if (y >= r2.y && y < (r2.y + r2.height))
				return true;
		}

		return false;
	}
}
