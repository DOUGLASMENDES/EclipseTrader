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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IOHLC;

/**
 * Draws a candlestick chart.
 *
 * @since 1.0
 */
public class CandleStickChart implements IChartObject {
	private IDataSeries dataSeries;
	private IChartObject parent;

	private int width = 5;
	private RGB outlineColor = new RGB(0, 0, 0);
	private RGB positiveColor = new RGB(255, 255, 255);
	private RGB negativeColor = new RGB(0, 0, 0);

	private IAdaptable[] values;
	private List<Candle> pointArray;
	private boolean valid;

	public CandleStickChart(IDataSeries dataSeries) {
	    this.dataSeries = dataSeries;
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
    		while(pointArray.size() > values.length)
    			pointArray.remove(pointArray.size() - 1);

    		valid = true;
    	}

    	if (pointArray != null) {
			graphics.pushState();
        	for (Candle c : pointArray)
       			c.paint(graphics);
    		graphics.popState();
    	}
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
		return dataSeries.getName();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    public String getToolTip(int x, int y) {
    	for (Candle c : pointArray) {
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
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    public void accept(IChartObjectVisitor visitor) {
    	visitor.visit(this);
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
			if (x == this.x)
				return y >= yHigh && y <= yLow;
			if (x >= (this.x - width / 2) && x <= (this.x + width / 2))
				return y >= yHigh && y <= yLow;
			return false;
		}

		public String getToolTip() {
			return dataSeries.getName() + " " + ohlc;
		}
    }
}
