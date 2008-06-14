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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;

/**
 * Draws an histogram area chart.
 *
 * @since 1.0
 */
public class HistogramAreaChart implements IChartObject {
	private IDataSeries dataSeries;
	private IChartObject parent;

	private IAdaptable[] values;
	private List<Polygon> pointArray = new ArrayList<Polygon>(2048);
	private boolean valid;
	private boolean focus;

	private RGB color = new RGB(0, 0, 0);
	private RGB fillColor;

	private NumberFormat numberFormat = NumberFormat.getInstance();

	public HistogramAreaChart(IDataSeries dataSeries) {
	    this.dataSeries = dataSeries;

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
    	if (!valid && values != null) {
    		int zero = graphics.mapToVerticalAxis(0.0);

    		for (int i = 0; i < values.length - 1; i++) {
    			Date date1 = (Date) values[i].getAdapter(Date.class);
    			Date date2 = (Date) values[i + 1].getAdapter(Date.class);
    			Number value1 = (Number) values[i].getAdapter(Number.class);
    			Number value2 = (Number) values[i + 1].getAdapter(Number.class);

    			int x1 = graphics.mapToHorizontalAxis(date1);
    			int y1 = graphics.mapToVerticalAxis(value1);
    			int x2 = graphics.mapToHorizontalAxis(date2);
    			int y2 = graphics.mapToVerticalAxis(value2);

        		Polygon candle;
        		if (i < pointArray.size())
        			candle = pointArray.get(i);
        		else {
        			candle = new Polygon();
        			pointArray.add(candle);
        		}
        		candle.setBounds(zero, x1, y1, x2, y2);
    		}
    		while(pointArray.size() > (values.length - 1))
    			pointArray.remove(pointArray.size() - 1);
    	}

    	if (fillColor == null)
    		fillColor = blend(color, graphics.getBackgroundColor(), 25);

    	graphics.pushState();
    	graphics.setLineWidth(hasFocus() ? 2 : 1);
    	for (Polygon c : pointArray)
    		c.paint(graphics);
    	graphics.popState();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y) {
    	for (Polygon c : pointArray) {
   			if (c.containsPoint(x, y))
   				return true;
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
		if (dataSeries.getLast() != null)
			return dataSeries.getName() + ": " + numberFormat.format(dataSeries.getLast().getAdapter(Number.class));
		return dataSeries.getName();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    public String getToolTip(int x, int y) {
    	for (Polygon c : pointArray) {
   			if (c.containsPoint(x, y))
   				return c.getToolTip();
    	}
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#add(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void add(IChartObject object) {
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#remove(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void remove(IChartObject object) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getChildren()
     */
    public IChartObject[] getChildren() {
	    return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getParent()
     */
    public IChartObject getParent() {
	    return parent;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setParent(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void setParent(IChartObject parent) {
    	this.parent = parent;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    public void handleFocusGained(ChartObjectFocusEvent event) {
    	focus = true;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    public void handleFocusLost(ChartObjectFocusEvent event) {
    	focus = false;
    }

	protected boolean hasFocus() {
    	return focus;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    public void accept(IChartObjectVisitor visitor) {
    	visitor.visit(this);
    }

	private RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

    private int blend(int v1, int v2, int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
	}

    private class Polygon {
		int x1;
		int x2;
		int y1;
		int y2;
		int yZero;
		int[] polygon = new int[8];
		Point[] points = new Point[5];

		public Polygon() {
		}

		public void setBounds(int yZero, int x1, int y1, int x2, int y2) {
	        this.yZero = yZero;
	        this.x1 = x1;
	        this.x2 = x2;
	        this.y1 = y1;
	        this.y2 = y2;

	        points[0] = new Point(x1, yZero);
	        points[1] = new Point(x1, y1);
	        points[2] = new Point(x2, y2);
	        points[3] = new Point(x2, yZero);
	        points[4] = points[0];

	        polygon[0] = x1;
			polygon[1] = yZero;
			polygon[2] = x1;
			polygon[3] = y1;
			polygon[4] = x2;
			polygon[5] = y2;
			polygon[6] = x2;
			polygon[7] = yZero;
        }

		public void paint(IGraphics graphics) {
	    	graphics.setBackgroundColor(fillColor);
			graphics.fillPolygon(polygon);
	    	graphics.setForegroundColor(color);
	    	graphics.drawLine(x1, y1, x2, y2);
	    	graphics.drawLine(x1, yZero, x2, yZero);
		}

		public boolean containsPoint(int x, int y) {
			int crossings = 0;
			for (int i = 0; i < points.length - 1; i++) {
				int div = (points[i + 1].y - points[i].y);
				if (div == 0)
					div = 1;
				double slope = (points[i + 1].x - points[i].x) / div;
				boolean cond1 = (points[i].y <= y) && (y < points[i + 1].y);
				boolean cond2 = (points[i + 1].y <= y) && (y < points[i].y);
				boolean cond3 = x < slope * (y - points[i].y) + points[i].x;
				if ((cond1 || cond2) && cond3)
					crossings++;
			}
			return (crossings % 2 != 0);
		}

		public String getToolTip() {
			return null;
		}
    }
}
