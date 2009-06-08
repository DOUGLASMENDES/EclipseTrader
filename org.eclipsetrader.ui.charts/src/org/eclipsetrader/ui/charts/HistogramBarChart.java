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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.charts.IDataSeries;

/**
 * Draw an historgram bar chart.
 *
 * @since 1.0
 */
public class HistogramBarChart implements IChartObject, ISummaryBarDecorator, IAdaptable {
	private IDataSeries dataSeries;

	private int width = 5;
	private RGB positiveColor = new RGB(0, 254, 0);
	private RGB negativeColor = new RGB(254, 0, 0);

	private IAdaptable[] values;
	private List<Bar> pointArray = new ArrayList<Bar>(2048);
	private boolean valid;
	private boolean hasFocus;

	private SummaryNumberItem numberItem;

	private NumberFormat numberFormat = NumberFormat.getInstance();

	public HistogramBarChart(IDataSeries dataSeries) {
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
    	this.width = dataBounds.horizontalSpacing - 1;
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
    	if ((!valid || pointArray == null) && values != null) {
    		int zero = graphics.mapToVerticalAxis(0.0);

    		RGB positiveOutlineColor = blend(positiveColor, graphics.getForegroundColor(), 75);
    		RGB negativeOutlineColor = blend(negativeColor, graphics.getForegroundColor(), 75);

    		for (int i = 0; i < values.length; i++) {
    			Date date = (Date) values[i].getAdapter(Date.class);
    			Number value = (Number) values[i].getAdapter(Number.class);
    			Number previousValue = i > 0 ? (Number) values[i - 1].getAdapter(Number.class) : null;

    			int x = graphics.mapToHorizontalAxis(date) - width / 2;
    			int y = graphics.mapToVerticalAxis(value);
    			Bar bar;
        		if (i < pointArray.size())
        			bar = pointArray.get(i);
        		else {
        			bar = new Bar();
        			pointArray.add(bar);
        		}
    			bar.setBounds(x, y, Math.abs(y - zero));
    			bar.setValue(value);
    			if (previousValue != null) {
    				bar.setColor(previousValue.doubleValue() > value.doubleValue() ? negativeColor : positiveColor);
    				bar.setOutlineColor(previousValue.doubleValue() > value.doubleValue() ? negativeOutlineColor : positiveOutlineColor);
    			}
    			else {
    				bar.setColor(positiveColor);
    				bar.setOutlineColor(positiveOutlineColor);
    			}
    		}
    		while(pointArray.size() > values.length)
    			pointArray.remove(pointArray.size() - 1);

    		valid = true;
    	}

    	graphics.pushState();
    	graphics.setLineWidth(hasFocus() ? 2 : 1);
    	for (Bar c : pointArray)
   			c.paint(graphics);
    	graphics.popState();
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
    	for (Bar c : pointArray) {
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
			return dataSeries.getName() + ": " + numberFormat.format(dataSeries.getLast().getAdapter(Number.class)); //$NON-NLS-1$
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

	protected boolean hasFocus() {
    	return hasFocus;
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
		Number value = (Number) (values.length > 0 ? values[values.length - 1].getAdapter(Number.class) : null);

		numberItem = new SummaryNumberItem(parent, SWT.NONE);
		numberItem.setValue(dataSeries.getName() + ": ", value); //$NON-NLS-1$
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#updateDecorator(int, int)
     */
    public void updateDecorator(int x, int y) {
		if (pointArray != null) {
			for (int i = 0; i < pointArray.size(); i++) {
				if (pointArray.get(i).containsPoint(x, y)) {
	    			Number value = (Number) values[i].getAdapter(Number.class);
	    			if (value != null)
	    				numberItem.setValue(dataSeries.getName() + ": ", value); //$NON-NLS-1$
				}
			}
		}
    }

    private class Bar {
		int x;
		int y;
		int height;

		Number value;
		RGB color;
		RGB outlineColor;

		public Bar() {
        }

		public void setBounds(int x, int y, int height) {
	        this.x = x;
	        this.y = y;
	        this.height = height;
	        if (height < 0) {
	        	this.y += height;
	        	this.height = -height;
	        }
        }

		public void setValue(Number value) {
        	this.value = value;
        }

		public void setColor(RGB color) {
        	this.color = color;
        }

		public void setOutlineColor(RGB outlineColor) {
        	this.outlineColor = outlineColor;
        }

		public void paint(IGraphics graphics) {
			graphics.setForegroundColor(outlineColor);
			graphics.setBackgroundColor(color);
   			graphics.fillRectangle(x, y, width, height);
   			graphics.drawRectangle(x, y, width - 1, height - 1);
		}

		public boolean containsPoint(int x, int y) {
			if (y == SWT.DEFAULT)
				return x >= this.x && x <= (this.x + width);
			if (x >= this.x && x <= (this.x + width))
				return y >= this.y && y <= (this.y + height);
			return false;
		}

		public String getToolTip() {
			return dataSeries.getName() + ": " + numberFormat.format(value); //$NON-NLS-1$
		}
    }
}
