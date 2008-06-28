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

package org.eclipsetrader.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Display a 5-bands pressure bar.
 *
 * @since 1.0
 */
public class PressureBar {
	private Canvas canvas;

	private long[] leftWeights;
	private long[] rightWeights;

	private Color separatorColor;
	private Color[] bandColors;

	private int separatorWidth = 3;

	PressureBar() {
	}

	public PressureBar(Composite parent, int style) {
		canvas = new Canvas(parent, style | SWT.DOUBLE_BUFFERED);
		canvas.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
            	try {
            		onPaintControl(event);
            	} catch(Exception e) {
            		// TODO Log exception
            		e.printStackTrace();
            	} catch(Error e) {
            		// TODO Log error
            		e.printStackTrace();
            	}
            }
		});

		separatorColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		bandColors = new Color[] {
				Display.getCurrent().getSystemColor(SWT.COLOR_BLUE),
				Display.getCurrent().getSystemColor(SWT.COLOR_CYAN),
				Display.getCurrent().getSystemColor(SWT.COLOR_GREEN),
				Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW),
				Display.getCurrent().getSystemColor(SWT.COLOR_RED),
			};
	}

	public Color getSeparatorColor() {
    	return separatorColor;
    }

	public void setSeparatorColor(Color indicatorColor) {
    	this.separatorColor = indicatorColor;
		canvas.redraw();
    }

	public Color[] getBandColors() {
    	return bandColors;
    }

	public void setBandColors(Color[] bandColors) {
    	this.bandColors = bandColors;
		canvas.redraw();
    }

	public void setWeights(long[] leftWeights, long[] rightWeights) {
		this.leftWeights = leftWeights;
		this.rightWeights = rightWeights;
		canvas.redraw();
	}

	protected void onPaintControl(PaintEvent e) {
		Rectangle clientArea = ((Control) e.widget).getBounds();

		if (leftWeights != null && rightWeights != null) {
			int x = 0;
			int index = 0;
			int[] widths = getBandWidths(leftWeights, rightWeights, clientArea);

			for (int i = leftWeights.length - 1; i >= 0; i--) {
				e.gc.setBackground(bandColors[i]);
				e.gc.fillRectangle(x, 0, widths[i + index], clientArea.height);
				x += widths[i + index];
			}
			index += leftWeights.length;

			e.gc.setBackground(separatorColor);
			e.gc.fillRectangle(x, 0, widths[index], clientArea.height);
			x += widths[index++];

			for (int i = 0; i < rightWeights.length && i < 5; i++) {
				e.gc.setBackground(bandColors[i]);
				e.gc.fillRectangle(x, 0, widths[i + index], clientArea.height);
				x += widths[i + index];
			}
		}
	}

	protected int[] getBandWidths(long[] leftWeights, long[] rightWeights, Rectangle clientArea) {
		double total = 0;
		for (int i = 0; i < leftWeights.length; i++)
			total += leftWeights[i];
		for (int i = 0; i < rightWeights.length; i++)
			total += rightWeights[i];

		int bands = leftWeights.length + 1 + rightWeights.length;
		int[] widths = new int[bands];
		int maxWidth = clientArea.width - separatorWidth;
		int last = maxWidth;
		int index = 0;

		for (int i = 0; i < leftWeights.length; i++, index++) {
			widths[index] = (int) ((maxWidth / total) * leftWeights[i]);
			last -= widths[index];
		}
		widths[index++] = separatorWidth;
		for (int i = 0; i < rightWeights.length - 1; i++, index++) {
			widths[index] = (int) ((maxWidth / total) * rightWeights[i]);
			last -= widths[index];
		}
		if (index < widths.length)
			widths[index] = last;

		return widths;
	}

	public Control getControl() {
		return canvas;
	}

	public int getSeparatorWidth() {
    	return separatorWidth;
    }

	public void setSeparatorWidth(int separatorWidth) {
    	this.separatorWidth = separatorWidth;
		canvas.redraw();
    }
}
