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

package org.eclipsetrader.ui.charts;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipsetrader.core.feed.IOHLC;

public class LineRenderers {

	private LineRenderers() {
	}

	public static void renderLine(RenderTarget graphics, IAdaptable[] values, Color color) {
		int[] pointArray = new int[values.length * 2];
		for (int i = 0, pa = 0; i < values.length; i++) {
			Date date = (Date) values[i].getAdapter(Date.class);
			Number value = (Number) values[i].getAdapter(Number.class);
			pointArray[pa++] = graphics.horizontalAxis.mapToAxis(date) + graphics.x;
			pointArray[pa++] = graphics.verticalAxis.mapToAxis(value);
		}

		graphics.gc.setLineStyle(SWT.LINE_SOLID);

		if (color != null)
			graphics.gc.setForeground(color);
		graphics.gc.setLineWidth(1);
		graphics.gc.drawPolyline(pointArray);
	}

	public static void renderDotLine(RenderTarget graphics, IAdaptable[] values, Color color) {
		int[] pointArray = new int[values.length * 2];
		for (int i = 0, pa = 0; i < values.length; i++) {
			Date date = (Date) values[i].getAdapter(Date.class);
			Number value = (Number) values[i].getAdapter(Number.class);
			pointArray[pa++] = graphics.horizontalAxis.mapToAxis(date) + graphics.x;
			pointArray[pa++] = graphics.verticalAxis.mapToAxis(value);
		}

		int[] dashes = { 1, 2 };
		graphics.gc.setLineDash(dashes);

		if (color != null)
			graphics.gc.setForeground(color);
		graphics.gc.setLineWidth(1);
		graphics.gc.drawPolyline(pointArray);
	}

	public static void renderDashLine(RenderTarget graphics, IAdaptable[] values, Color color) {
		int[] pointArray = new int[values.length * 2];
		for (int i = 0, pa = 0; i < values.length; i++) {
			Date date = (Date) values[i].getAdapter(Date.class);
			Number value = (Number) values[i].getAdapter(Number.class);
			pointArray[pa++] = graphics.horizontalAxis.mapToAxis(date) + graphics.x;
			pointArray[pa++] = graphics.verticalAxis.mapToAxis(value);
		}

		int[] dashes = { 3, 3 };
		graphics.gc.setLineDash(dashes);

		if (color != null)
			graphics.gc.setForeground(color);
		graphics.gc.setLineWidth(1);
		graphics.gc.drawPolyline(pointArray);
	}

	public static void renderHistogram(RenderTarget event, IAdaptable[] values, Color lineColor, Color fillColor) {
		event.gc.setLineStyle(SWT.LINE_SOLID);
		if (lineColor != null)
			event.gc.setForeground(lineColor);
		if (fillColor != null)
			event.gc.setBackground(fillColor);

		int zero = event.verticalAxis.mapToAxis(0.0);
		int[] polygon = new int[8];

		for (int i = 0; i < values.length - 1; i++) {
			Date date1 = (Date) values[i].getAdapter(Date.class);
			Date date2 = (Date) values[i + 1].getAdapter(Date.class);
			Number value1 = (Number) values[i].getAdapter(Number.class);
			Number value2 = (Number) values[i + 1].getAdapter(Number.class);

			int x1 = event.horizontalAxis.mapToAxis(date1) + event.x;
			int x2 = event.horizontalAxis.mapToAxis(date2) + event.x;
			int y1 = event.verticalAxis.mapToAxis(value1);
			int y2 = event.verticalAxis.mapToAxis(value2);

			polygon[0] = x1;
			polygon[1] = zero;
			polygon[2] = x1;
			polygon[3] = y1;
			polygon[4] = x2;
			polygon[5] = y2;
			polygon[6] = x2;
			polygon[7] = zero;
			event.gc.fillPolygon(polygon);
		}

		renderLine(event, values, lineColor);
	}

	public static void renderHistogramBars(RenderTarget event, IAdaptable[] values, int width, Color color) {
		int half = width / 2;
		int zero = event.verticalAxis.mapToAxis(0.0);

		event.gc.setLineStyle(SWT.LINE_SOLID);
		if (color != null)
			event.gc.setBackground(color);

		for (int i = 0; i < values.length; i++) {
			Date date = (Date) values[i].getAdapter(Date.class);
			Number value = (Number) values[i].getAdapter(Number.class);

			int x = event.horizontalAxis.mapToAxis(date) + event.x;
			int y = event.verticalAxis.mapToAxis(value);

			event.gc.fillRectangle(x - half, y, width, zero - y);
		}
	}

	public static void renderBars(RenderTarget event, IAdaptable[] values, int width, Color positiveColor, Color negativeColor) {
		int half = width / 2;

		event.gc.setLineStyle(SWT.LINE_SOLID);
		event.gc.setLineWidth(1);

		for (int i = 0; i < values.length; i++) {
			IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
			if (ohlc == null)
				continue;

			int h = event.verticalAxis.mapToAxis(ohlc.getHigh());
			int l = event.verticalAxis.mapToAxis(ohlc.getLow());
			int c = event.verticalAxis.mapToAxis(ohlc.getClose());
			int o = event.verticalAxis.mapToAxis(ohlc.getOpen());

			int x = event.horizontalAxis.mapToAxis(ohlc.getDate()) + event.x;

			if (positiveColor != null && negativeColor != null)
				event.gc.setForeground(ohlc.getClose() >= ohlc.getOpen() ? positiveColor : negativeColor);
			event.gc.drawLine(x, h, x, l);
			event.gc.drawLine(x - half, o, x, o);
			event.gc.drawLine(x, c, x + half + 1, c);
		}
	}

	public static void renderCandles(RenderTarget event, IAdaptable[] values, int width, Color borderColor, Color positiveColor, Color negativeColor) {
		int half = width / 2;

		event.gc.setLineStyle(SWT.LINE_SOLID);
		event.gc.setLineWidth(1);

		for (int i = 0; i < values.length; i++) {
			IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
			if (ohlc == null)
				continue;

			int h = event.verticalAxis.mapToAxis(ohlc.getHigh());
			int l = event.verticalAxis.mapToAxis(ohlc.getLow());
			int c = event.verticalAxis.mapToAxis(ohlc.getClose());
			int o = event.verticalAxis.mapToAxis(ohlc.getOpen());

			int x = event.horizontalAxis.mapToAxis(ohlc.getDate()) + event.x;

			if (borderColor != null)
				event.gc.setForeground(borderColor);
			event.gc.drawLine(x, h, x, c);
			event.gc.drawLine(x, o, x, l);
			if (ohlc.getClose() == ohlc.getOpen())
				event.gc.drawLine(x - half, c, x + half, c);
			else {
				if (positiveColor != null && negativeColor != null)
					event.gc.setBackground(ohlc.getClose() >= ohlc.getOpen() ? positiveColor : negativeColor);
				event.gc.fillRectangle(x - half, c, width, o - c);
				event.gc.drawRectangle(x - half, c, width - 1, o - c);
			}
		}
	}
}
