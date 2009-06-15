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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IOHLC;

/**
 * Draws a candlestick chart.
 *
 * @since 1.0
 */
public class CandleStickChart implements IChartObject, ISummaryBarDecorator, IAdaptable {
	private IDataSeries dataSeries;

	private int width = 5;
	private RGB outlineColor = new RGB(0, 0, 0);
	private RGB positiveColor = new RGB(254, 254, 254);
	private RGB negativeColor = new RGB(0, 0, 0);

	private IAdaptable[] values;
	private List<Candle> pointArray;
	private boolean valid;
	private boolean hasFocus;

	private SummaryDateItem dateItem;
	private SummaryOHLCItem ohlcItem;

	private DateFormat dateFormat = DateFormat.getDateInstance();
	private NumberFormat numberFormat = NumberFormat.getInstance();

	public CandleStickChart(IDataSeries dataSeries) {
		this.dataSeries = dataSeries;

		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumIntegerDigits(1);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(4);
	}

	public CandleStickChart(IDataSeries dataSeries, RGB outlineColor, RGB positiveColor, RGB negativeColor) {
		this.dataSeries = dataSeries;
		if (outlineColor != null)
			this.outlineColor = outlineColor;
		if (positiveColor != null)
			this.positiveColor = positiveColor;
		if (negativeColor != null)
			this.negativeColor = negativeColor;

		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumIntegerDigits(1);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(4);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
	 */
	public void setDataBounds(DataBounds dataBounds) {
		List<IAdaptable> l = new ArrayList<IAdaptable>(2048);
		for (IAdaptable value : dataSeries.getValues()) {
			Date date = (Date) value.getAdapter(Date.class);
			if ((dataBounds.first == null || !date.before(dataBounds.first)) && (dataBounds.last == null || !date.after(dataBounds.last)))
				l.add(value);
		}
		this.values = l.toArray(new IAdaptable[l.size()]);
		this.width = dataBounds.horizontalSpacing;
		this.valid = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
	 */
	public void invalidate() {
		this.valid = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
	 */
	public void paint(IGraphics graphics) {
		if (!valid || pointArray == null && values != null) {
			if (pointArray == null)
				pointArray = new ArrayList<Candle>(values.length);

			for (int i = 0; i < values.length; i++) {
				IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
				if (ohlc == null)
					continue;

				int h = graphics.mapToVerticalAxis(ohlc.getHigh());
				int l = graphics.mapToVerticalAxis(ohlc.getLow());
				int c = graphics.mapToVerticalAxis(ohlc.getClose());
				int o = graphics.mapToVerticalAxis(ohlc.getOpen());

				int x = graphics.mapToHorizontalAxis(ohlc.getDate());

				Candle candle;
				if (i < pointArray.size())
					candle = pointArray.get(i);
				else {
					candle = new Candle();
					pointArray.add(candle);
				}
				candle.setBounds(x, h, o, c, l);
				candle.setOhlc(ohlc);
				candle.setOutlineColor(outlineColor);
				candle.setFillColor(ohlc.getClose() < ohlc.getOpen() ? negativeColor : positiveColor);
			}
			while (pointArray.size() > values.length)
				pointArray.remove(pointArray.size() - 1);

			valid = true;
		}

		if (pointArray != null) {
			graphics.pushState();
			graphics.setLineWidth(hasFocus ? 2 : 1);
			for (Candle c : pointArray)
				c.paint(graphics);
			graphics.popState();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
	 */
	public void paintScale(Graphics graphics) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y) {
		if (pointArray != null) {
			for (Candle c : pointArray) {
				if (c.containsPoint(x, y))
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
	 */
	public IDataSeries getDataSeries() {
		return dataSeries;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
	 */
	public String getToolTip() {
		if (dataSeries.getLast() != null) {
			IOHLC ohlc = (IOHLC) dataSeries.getLast().getAdapter(IOHLC.class);
			return dateFormat.format(ohlc.getDate()) + " O:" + numberFormat.format(ohlc.getOpen()) + //$NON-NLS-1$
			" H:" + numberFormat.format(ohlc.getHigh()) + //$NON-NLS-1$
			" L:" + numberFormat.format(ohlc.getLow()) + //$NON-NLS-1$
			" C:" + numberFormat.format(ohlc.getHigh()); //$NON-NLS-1$
		}
		return dataSeries.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
	 */
	public String getToolTip(int x, int y) {
		if (pointArray != null) {
			for (Candle c : pointArray) {
				if (c.containsPoint(x, y))
					return c.getToolTip();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
	 */
	public void handleFocusGained(ChartObjectFocusEvent event) {
		hasFocus = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
	 */
	public void handleFocusLost(ChartObjectFocusEvent event) {
		hasFocus = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
	 */
	public void accept(IChartObjectVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ISummaryBarDecorator.class)) {
			return this;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#createDecorator(org.eclipse.swt.widgets.Composite)
	 */
	public void createDecorator(Composite parent) {
		IAdaptable[] values = dataSeries.getValues();
		IOHLC ohlc = (IOHLC) (values.length > 0 ? values[values.length - 1].getAdapter(IOHLC.class) : null);
		IOHLC previousOhlc = (IOHLC) (values.length > 1 ? values[values.length - 2].getAdapter(IOHLC.class) : null);

		dateItem = new SummaryDateItem(parent, SWT.DATE);
		dateItem.setDate(ohlc != null ? ohlc.getDate() : null);

		ohlcItem = new SummaryOHLCItem(parent, SWT.NONE);
		ohlcItem.setOHLC(ohlc, previousOhlc);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#updateDecorator(int, int)
	 */
	public void updateDecorator(int x, int y) {
		if (x == SWT.DEFAULT) {
			IAdaptable[] values = dataSeries.getValues();
			IOHLC ohlc = (IOHLC) (values.length > 0 ? values[values.length - 1].getAdapter(IOHLC.class) : null);
			IOHLC previousOhlc = (IOHLC) (values.length > 1 ? values[values.length - 2].getAdapter(IOHLC.class) : null);

			dateItem.setDate(ohlc != null ? ohlc.getDate() : null);
			ohlcItem.setOHLC(ohlc, previousOhlc);
		}
		else if (pointArray != null) {
			for (int i = 1; i < pointArray.size(); i++) {
				Candle c = pointArray.get(i);
				if (c.containsPoint(x, y)) {
					dateItem.setDate(c.ohlc.getDate());
					ohlcItem.setOHLC(c.ohlc, pointArray.get(i - 1).ohlc);
				}
			}
		}
	}

	private class Candle {
		int x;
		int yHigh;
		int yOpen;
		int yClose;
		int yLow;

		IOHLC ohlc;
		RGB fillColor;
		RGB outlineColor;

		public Candle() {
		}

		public void setBounds(int x, int yHigh, int yOpen, int yClose, int yLow) {
			this.x = x;
			this.yHigh = yHigh;
			this.yOpen = yOpen;
			this.yClose = yClose;
			this.yLow = yLow;
		}

		public void setFillColor(RGB fillColor) {
			this.fillColor = fillColor;
		}

		public void setOutlineColor(RGB outlineColor) {
			this.outlineColor = outlineColor;
		}

		public void setOhlc(IOHLC ohlc) {
			this.ohlc = ohlc;
		}

		public void paint(IGraphics graphics) {
			graphics.setForegroundColor(outlineColor);
			graphics.drawLine(x, yHigh, x, yLow);
			if (yOpen < yClose) {
				graphics.setBackgroundColor(fillColor);
				graphics.fillRectangle(x - width / 2, yOpen, width, yClose - yOpen);
				graphics.drawRectangle(x - width / 2, yOpen, width - 1, yClose - yOpen - 1);
			}
			else {
				graphics.setBackgroundColor(fillColor);
				graphics.fillRectangle(x - width / 2, yClose, width, yOpen - yClose);
				graphics.drawRectangle(x - width / 2, yClose, width - 1, yOpen - yClose - 1);
			}
		}

		public boolean containsPoint(int x, int y) {
			if (y == SWT.DEFAULT)
				return x >= (this.x - width / 2) && x <= (this.x + width / 2);
			if (x == this.x)
				return y >= yHigh && y <= yLow;
			if (x >= (this.x - width / 2) && x <= (this.x + width / 2))
				return y >= yHigh && y <= yLow;
			return false;
		}

		public String getToolTip() {
			return dataSeries.getName() + "\r\nD:" + dateFormat.format(ohlc.getDate()) + //$NON-NLS-1$
			"\r\nO:" + numberFormat.format(ohlc.getOpen()) + //$NON-NLS-1$
			"\r\nH:" + numberFormat.format(ohlc.getHigh()) + //$NON-NLS-1$
			"\r\nL:" + numberFormat.format(ohlc.getLow()) + //$NON-NLS-1$
			"\r\nC:" + numberFormat.format(ohlc.getHigh()); //$NON-NLS-1$
		}
	}
}
