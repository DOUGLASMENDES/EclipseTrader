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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IOHLC;

public class BarChart implements IChartObject {
	private IDataSeries dataSeries;
	private IChartObject parent;

	private int width = 5;
	private RGB positiveColor = new RGB(0, 254, 0);
	private RGB negativeColor = new RGB(254, 0, 0);

	private IAdaptable[] values;
	private List<Bar> pointArray;
	private boolean valid;
	private boolean hasFocus;

	private DateFormat dateFormat = DateFormat.getDateInstance();
	private NumberFormat numberFormat = NumberFormat.getInstance();

	public BarChart(IDataSeries dataSeries) {
	    this.dataSeries = dataSeries;

	    numberFormat.setGroupingUsed(true);
	    numberFormat.setMinimumIntegerDigits(1);
	    numberFormat.setMinimumFractionDigits(2);
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
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    public void paint(IGraphics graphics) {
    	if (!valid || pointArray == null && values != null) {
    		if (pointArray == null)
    			pointArray = new ArrayList<Bar>(values.length);

    		for (int i = 0; i < values.length; i++) {
    			IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
    			if (ohlc == null)
    				continue;

    			int h = graphics.mapToVerticalAxis(ohlc.getHigh());
    			int l = graphics.mapToVerticalAxis(ohlc.getLow());
    			int c = graphics.mapToVerticalAxis(ohlc.getClose());
    			int o = graphics.mapToVerticalAxis(ohlc.getOpen());

    			int x = graphics.mapToHorizontalAxis(ohlc.getDate());

    			Bar bar;
        		if (i < pointArray.size())
        			bar = pointArray.get(i);
        		else {
        			bar = new Bar();
        			pointArray.add(bar);
        		}
        		bar.setBounds(x, h, o, c, l);
        		bar.setOhlc(ohlc);
    			bar.setColor(ohlc.getClose() < ohlc.getOpen() ? negativeColor : positiveColor);
    		}
    		while(pointArray.size() > values.length)
    			pointArray.remove(pointArray.size() - 1);

    		valid = true;
    	}

    	if (pointArray != null) {
        	graphics.pushState();
        	graphics.setLineWidth(hasFocus ? 2 : 1);
        	for (Bar c : pointArray)
       			c.paint(graphics);
        	graphics.popState();
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y) {
		if (pointArray != null) {
        	for (Bar c : pointArray) {
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
		return dataSeries.getName();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    public String getToolTip(int x, int y) {
    	for (Bar c : pointArray) {
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

    private class Bar {
		int x;
		int yHigh;
		int yOpen;
		int yClose;
		int yLow;

		IOHLC ohlc;
		RGB color;

		public Bar() {
        }

		public void setBounds(int x, int yHigh, int yOpen, int yClose, int yLow) {
	        this.x = x;
	        this.yHigh = yHigh;
	        this.yOpen = yOpen;
	        this.yClose = yClose;
	        this.yLow = yLow;
        }

		public void setOhlc(IOHLC ohlc) {
        	this.ohlc = ohlc;
        }

		public void setColor(RGB color) {
        	this.color = color;
        }

		public void paint(IGraphics graphics) {
			graphics.setForegroundColor(color);
        	graphics.drawLine(x, yHigh, x, yLow);
        	graphics.drawLine(x - width / 2, yOpen, x, yOpen);
        	graphics.drawLine(x, yClose, x + width / 2, yClose);
		}

		public boolean containsPoint(int x, int y) {
			if (y == SWT.DEFAULT)
				return x >= (this.x - width / 2) && x <= (this.x + width / 2);
			if (x == this.x || x == SWT.DEFAULT)
				return y >= yHigh && y <= yLow;
			if (x >= (this.x - width / 2) && x <= (this.x + width / 2))
				return y >= yHigh && y <= yLow;
			return false;
		}

		public String getToolTip() {
			return dataSeries.getName() +
				   "\r\nD:" + dateFormat.format(ohlc.getDate()) +
				   "\r\nO:" + numberFormat.format(ohlc.getOpen()) +
				   "\r\nH:" + numberFormat.format(ohlc.getHigh()) +
				   "\r\nL:" + numberFormat.format(ohlc.getLow()) +
				   "\r\nC:" + numberFormat.format(ohlc.getHigh());
		}
    }
}
