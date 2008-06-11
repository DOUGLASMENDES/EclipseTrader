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

package org.eclipsetrader.ui.internal.charts.tools;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IEditableChartObject;
import org.eclipsetrader.ui.charts.IGraphics;
import org.eclipsetrader.ui.charts.PixelTools;
import org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent;

public class LineToolObject implements IChartObject, IEditableChartObject {
	private Value v1;
	private Value v2;

	private Point p1;
	private Point p2;
	private boolean valid;

	private RGB color = new RGB(0, 0, 0);

	private boolean focus;
	private boolean editorActive;
	private Value currentValue;

	private Point p1start;
	private Point p2start;
	private int lastX = -1, lastY = -1;

	public static class Value implements IAdaptable {
		private Date date;
		private Number value;

		public Value(Date date, Number value) {
	        this.date = date;
	        this.value = value;
        }

		public Date getDate() {
        	return date;
        }

		public void setDate(Date date) {
        	this.date = date;
        }

		public Number getValue() {
        	return value;
        }

		public void setValue(Number value) {
        	this.value = value;
        }

		/* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
        	if (adapter.isAssignableFrom(Date.class))
        		return date;
        	if (adapter.isAssignableFrom(Number.class))
        		return value;
	        return null;
        }
	}

	public LineToolObject() {
	}

	public Value[] getValues() {
		return new Value[] { v1, v2 };
	}

	public void setValues(Value[] values) {
		v1 = values[0];
		v2 = values[1];
		p1 = p2 = null;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
     */
    public void setDataBounds(DataBounds bounds) {
		p1 = p2 = null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
     */
    public IDataSeries getDataSeries() {
	    return new DataSeries("LINE", new IAdaptable[] { v1, v2 });
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
     */
    public boolean containsPoint(int x, int y) {
    	if (p1 != null && p2 != null)
    		return PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, p2.y);
	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#add(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void add(IChartObject object) {
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
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
     */
    public String getToolTip() {
	    return "Line";
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    public String getToolTip(int x, int y) {
	    return null;
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
     * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
     */
    public void invalidate() {
    	this.valid = false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    public void paint(IGraphics graphics) {
    	if (editorActive)
    		return;

    	if ((!valid || p1 == null) && v1 != null)
    		p1 = graphics.mapToPoint(v1.getDate(), v1.getValue());
    	if ((!valid || p2 == null) && v2 != null)
    		p2 = graphics.mapToPoint(v2.getDate(), v2.getValue());

    	if (p1 != null && p2 != null) {
        	graphics.pushState();
        	graphics.setForegroundColor(color);

        	graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
        	if (hasFocus()) {
            	graphics.setBackgroundColor(color);
        		graphics.fillRectangle(p1.x - 2, p1.y - 2, 5, 5);
        		graphics.fillRectangle(p2.x - 2, p2.y - 2, 5, 5);
        	}

        	graphics.popState();
        	valid = true;
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IEditableChartObject#isOnDragHandle(int, int)
     */
    public boolean isOnDragHandle(int x, int y) {
    	if (p1 != null && p2 != null)
    		return PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, p2.y);
	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IEditableChartObject#isOnEditHandle(int, int)
     */
    public boolean isOnEditHandle(int x, int y) {
		if (p1 != null && p2 != null) {
			if (Math.abs(x - p1.x) <= 2 && Math.abs(y - p1.y) <= 2)
				return true;
			else if (Math.abs(x - p2.x) <= 2 && Math.abs(y - p2.y) <= 2)
				return true;
		}
	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IEditableChartObject#handleMouseDown(org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent)
     */
    public void handleMouseDown(ChartObjectEditorEvent e) {
	    if (v1 == null && v2 == null) {
	    	v1 = new Value(e.date, e.value);
	    	v2 = new Value(e.date, e.value);
	    	currentValue = v2;
	    }
	    else {
		    if (p1 == null || p2 == null) {
	    		p1 = e.graphics.mapToPoint(v1.getDate(), v1.getValue());
	    		p2 = e.graphics.mapToPoint(v2.getDate(), v2.getValue());
		    }
			if (Math.abs(e.x - p1.x) <= 2 && Math.abs(e.y - p1.y) <= 2)
		    	currentValue = v1;
			else if (Math.abs(e.x - p2.x) <= 2 && Math.abs(e.y - p2.y) <= 2)
		    	currentValue = v2;
			else {
				p1start = p1;
				p2start = p2;
				lastX = e.x;
				lastY = e.y;
			}
	    }
	    editorActive = true;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IEditableChartObject#handleMouseUp(org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent)
     */
    public void handleMouseUp(ChartObjectEditorEvent e) {
    	currentValue = null;
		lastX = -1;
		lastY = -1;
	    editorActive = false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IEditableChartObject#handleMouseMove(org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent)
     */
    public void handleMouseMove(ChartObjectEditorEvent e) {
    	if (editorActive && v1 != null && v2 != null) {
    		if (p1 != null && p2 != null) {
    			int x = Math.min(p1.x, p2.x);
    			int y = Math.min(p1.y, p2.y);
    			int width = Math.abs(p2.x - p1.x) + 1;
    			int height = Math.abs(p2.y - p1.y) + 1;
    			e.canvas.redraw(x, y, width, height, false);
    			e.canvas.update();
    		}

    		if (currentValue != null) {
        		currentValue.setDate(e.date);
        		currentValue.setValue(e.value);
    		}
    		else {
				v1.setDate((Date) e.graphics.mapToHorizontalValue(p1start.x + (e.x - lastX)));
				v1.setValue((Number) e.graphics.mapToVerticalValue(p1start.y + (e.y - lastY)));

				v2.setDate((Date) e.graphics.mapToHorizontalValue(p2start.x + (e.x - lastX)));
				v2.setValue((Number) e.graphics.mapToVerticalValue(p2start.y + (e.y - lastY)));
    		}

    		p1 = e.graphics.mapToPoint(v1.getDate(), v1.getValue());
    		p2 = e.graphics.mapToPoint(v2.getDate(), v2.getValue());
    		e.graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#remove(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void remove(IChartObject object) {
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setParent(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void setParent(IChartObject parent) {
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    public void accept(IChartObjectVisitor visitor) {
    	visitor.visit(this);
    }
}
