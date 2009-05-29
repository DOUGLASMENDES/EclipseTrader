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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.charts.IDataSeries;

/**
 * Draws a line chart.
 *
 * @since 1.0
 */
public class LineChart implements IChartObject, ISummaryBarDecorator, IAdaptable {
	private IDataSeries dataSeries;
	private IChartObject parent;

	private LineStyle style;
	private RGB color;
	private int width = 5;

	private IAdaptable[] values;
	private Point[] pointArray;
	private boolean valid;
	private boolean hasFocus;

	private SummaryNumberItem numberItem;

	private NumberFormat numberFormat = NumberFormat.getInstance();

	public static enum LineStyle {
		Solid,
		Dot,
		Dash,
		Invisible
	}

	public LineChart(IDataSeries dataSeries, LineStyle style, RGB color) {
	    this.dataSeries = dataSeries;
	    this.style = style;
	    this.color = color;

	    numberFormat.setGroupingUsed(true);
	    numberFormat.setMinimumIntegerDigits(1);
	    numberFormat.setMinimumFractionDigits(0);
	    numberFormat.setMaximumFractionDigits(4);
	}

	public RGB getColor() {
    	return color;
    }

	public void setColor(RGB color) {
    	this.color = color;
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
     * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
     */
    public void invalidate() {
    	this.valid = false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    public void paint(IGraphics graphics) {
    	if ((!valid || pointArray == null) && values != null && style != LineStyle.Invisible) {
    		pointArray = new Point[values.length];
    		for (int i = 0; i < values.length; i++) {
    			Date date = (Date) values[i].getAdapter(Date.class);
    			Number value = (Number) values[i].getAdapter(Number.class);
    			pointArray[i] = graphics.mapToPoint(date, value);
    		}
        	valid = true;
    	}

    	if (pointArray != null && style != LineStyle.Invisible) {
        	switch(style) {
        		case Dash:
        			graphics.setLineStyle(SWT.LINE_DASH);
        			break;
        		case Dot:
        			graphics.setLineStyle(SWT.LINE_DOT);
        			break;
        		default:
        			graphics.setLineStyle(SWT.LINE_SOLID);
    				break;
        	}

        	graphics.pushState();
        	graphics.setForegroundColor(color);
        	graphics.setLineWidth(hasFocus() ? 2 : 1);
        	graphics.drawPolyline(pointArray);
        	graphics.popState();
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y) {
		if (pointArray != null) {
			if (y == SWT.DEFAULT)
				return true;
			return PixelTools.isPointOnLine(x, y, pointArray);
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
		if (pointArray != null) {
			if (y == SWT.DEFAULT) {
				for (int i = 0; i < pointArray.length; i++) {
					if (x >= (pointArray[i].x - width / 2) && x <= (pointArray[i].x + width / 2))
						return dataSeries.getName() + ": " + numberFormat.format(values[i].getAdapter(Number.class));
				}
			}
			else {
				for (int i = 1; i < pointArray.length; i++) {
					if (PixelTools.isPointOnLine(x, y, pointArray[i - 1].x, pointArray[i - 1].y, pointArray[i].x, pointArray[i].y))
						return dataSeries.getName() + " " + numberFormat.format(values[i - 1].getAdapter(Number.class));
				}
			}
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
		numberItem.setValue(dataSeries.getName() + ": ", value);
		if (color != null)
			numberItem.setForeground(color);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#updateDecorator(int, int)
     */
    public void updateDecorator(int x, int y) {
		if (pointArray != null) {
			Number value = null;
			if (y == SWT.DEFAULT) {
				for (int i = 0; i < pointArray.length; i++) {
					if (x >= (pointArray[i].x - width / 2) && x <= (pointArray[i].x + width / 2))
						value = (Number) values[i].getAdapter(Number.class);
				}
			}
			else {
				for (int i = 1; i < pointArray.length; i++) {
					if (PixelTools.isPointOnLine(x, y, pointArray[i - 1].x, pointArray[i - 1].y, pointArray[i].x, pointArray[i].y))
						value = (Number) values[i - 1].getAdapter(Number.class);
				}
			}
			if (value != null)
				numberItem.setValue(dataSeries.getName() + ": ", value);
		}
    }
}
