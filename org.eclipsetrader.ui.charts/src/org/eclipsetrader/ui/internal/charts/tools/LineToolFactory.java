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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.Graphics;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.IEditableChartObject;
import org.eclipsetrader.ui.charts.IGraphics;
import org.eclipsetrader.ui.charts.PixelTools;
import org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent;

public class LineToolFactory implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String factoryName;
    private String name;

	private RGB color;
	private boolean extend1;
	private boolean extend2;

	private Value value1;
	private Value value2;
    private LineToolObject object;

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

    private class LineToolObject implements IChartObject, IEditableChartObject {
    	private Point p1;
    	private Point p2;
    	private boolean valid;

    	private boolean focus;
    	private boolean editorActive;
    	private Value currentValue;

    	private Point p1start;
    	private Point p2start;
    	private int lastX = -1, lastY = -1;

    	public LineToolObject() {
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
    	    return new DataSeries(name, new IAdaptable[] { value1, value2 });
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

        	if ((!valid || p1 == null) && value1 != null)
        		p1 = graphics.mapToPoint(value1.getDate(), value1.getValue());
        	if ((!valid || p2 == null) && value2 != null)
        		p2 = graphics.mapToPoint(value2.getDate(), value2.getValue());

        	if (p1 != null && p2 != null) {
            	graphics.pushState();
            	graphics.setForegroundColor(color);

            	if (extend1 || extend2) {
            		if (extend1)
            			drawExtendedLine(graphics, p2.x, p2.y, p1.x, p1.y);
            		if (extend2)
            			drawExtendedLine(graphics, p1.x, p1.y, p2.x, p2.y);
            	}
            	else
            		graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            	if (hasFocus()) {
                	graphics.setBackgroundColor(graphics.getForegroundColor());
            		graphics.fillRectangle(p1.x - 2, p1.y - 2, 5, 5);
            		graphics.fillRectangle(p2.x - 2, p2.y - 2, 5, 5);
            	}

            	graphics.popState();
            	valid = true;
        	}
        }

    	/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
         */
        public void paintScale(Graphics graphics) {
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
    	    if (value1 == null && value2 == null) {
    	    	value1 = new Value(e.date, e.value);
    	    	value2 = new Value(e.date, e.value);
    	    	currentValue = value2;
    	    }
    	    else {
    		    if (p1 == null || p2 == null) {
    	    		p1 = e.graphics.mapToPoint(value1.getDate(), value1.getValue());
    	    		p2 = e.graphics.mapToPoint(value2.getDate(), value2.getValue());
    		    }
    			if (Math.abs(e.x - p1.x) <= 2 && Math.abs(e.y - p1.y) <= 2)
    		    	currentValue = value1;
    			else if (Math.abs(e.x - p2.x) <= 2 && Math.abs(e.y - p2.y) <= 2)
    		    	currentValue = value2;
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
        	if (editorActive && value1 != null && value2 != null) {
        		if (p1 != null && p2 != null) {
        			if (extend1 || extend2)
        				e.canvas.redraw();
        			else {
            			int x = Math.min(p1.x, p2.x) - 2;
            			int y = Math.min(p1.y, p2.y) - 2;
            			int width = Math.abs(p2.x - p1.x) + 5;
            			int height = Math.abs(p2.y - p1.y) + 5;
            			e.canvas.redraw(x, y, width, height, false);
        			}
        			e.canvas.update();
        		}

        		if (currentValue != null) {
            		currentValue.setDate(e.date);
            		currentValue.setValue(e.value);
        		}
        		else {
        			value1.setDate((Date) e.graphics.mapToHorizontalValue(p1start.x + (e.x - lastX)));
        			value1.setValue((Number) e.graphics.mapToVerticalValue(p1start.y + (e.y - lastY)));

        			value2.setDate((Date) e.graphics.mapToHorizontalValue(p2start.x + (e.x - lastX)));
        			value2.setValue((Number) e.graphics.mapToVerticalValue(p2start.y + (e.y - lastY)));
        		}

        		p1 = e.graphics.mapToPoint(value1.getDate(), value1.getValue());
        		p2 = e.graphics.mapToPoint(value2.getDate(), value2.getValue());

            	e.graphics.setForegroundColor(color);
            	if (extend1 || extend2) {
            		if (extend1)
            			drawExtendedLine(e.graphics, p2.x, p2.y, p1.x, p1.y);
            		if (extend2)
            			drawExtendedLine(e.graphics, p1.x, p1.y, p2.x, p2.y);
            	}
            	else
            		e.graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            	if (hasFocus()) {
            		e.graphics.setBackgroundColor(e.graphics.getForegroundColor());
            		e.graphics.fillRectangle(p1.x - 2, p1.y - 2, 5, 5);
            		e.graphics.fillRectangle(p2.x - 2, p2.y - 2, 5, 5);
            	}
        	}
        }

    	/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
         */
        public void accept(IChartObjectVisitor visitor) {
        	visitor.visit(this);
        }

        private void drawExtendedLine(IGraphics graphics, int x, int y, int x2, int y2) {
    		int ydiff = y - y2;
    		int xdiff = x2 - x;

    		graphics.drawLine(x, y, x2, y2);

    		Rectangle bounds = graphics.getBounds();
    		if (xdiff != 0 || ydiff != 0) {
    			while (x2 > 0 && x2 < bounds.width && y2 > 0 && y2 < bounds.height) {
    				x = x2;
    				y = y2;
    				x2 += xdiff;
    				y2 -= ydiff;
    				graphics.drawLine(x, y, x2, y2);
    			}
    		}
    	}
    }

	public LineToolFactory() {
		object = new LineToolObject();
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	factoryName = config.getAttribute("name");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Value getValue1() {
    	return value1;
    }

	public void setValue1(Value value1) {
    	this.value1 = value1;
    }

	public Value getValue2() {
    	return value2;
    }

	public void setValue2(Value value2) {
    	this.value2 = value2;
    }

	public RGB getColor() {
    	return color;
    }

	public void setColor(RGB color) {
    	this.color = color;
    }

	public boolean isExtend1() {
    	return extend1;
    }

	public void setExtend1(boolean extend1) {
    	this.extend1 = extend1;
    }

	public boolean isExtend2() {
    	return extend2;
    }

	public void setExtend2(boolean extend2) {
    	this.extend2 = extend2;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
	 */
	public IChartObject createObject(IDataSeries source) {
		return object;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    public IChartParameters getParameters() {
    	ChartParameters parameters = new ChartParameters();

    	if (!factoryName.equals(name))
    		parameters.setParameter("name", name);

    	if (value1 != null) {
        	parameters.setParameter("d1", value1.getDate());
        	parameters.setParameter("v1", value1.getValue());
    	}
    	if (extend1)
    		parameters.setParameter("extend1", "true");

    	if (value2 != null) {
        	parameters.setParameter("d2", value2.getDate());
        	parameters.setParameter("v2", value2.getValue());
    	}
    	if (extend2)
    		parameters.setParameter("extend2", "true");

    	if (color != null)
        	parameters.setParameter("color", color);

	    return parameters;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
	 */
	public void setParameters(IChartParameters parameters) {
	    name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

	    Date d1 = parameters.hasParameter("d1") ? parameters.getDate("d1") : null;
	    Number v1 = parameters.hasParameter("v1") ? parameters.getDouble("v1") : null;
	    if (d1 != null && v1 != null)
	    	value1 = new Value(d1, v1);

	    Date d2 = parameters.hasParameter("d2") ? parameters.getDate("d2") : null;
	    Number v2 = parameters.hasParameter("v2") ? parameters.getDouble("v2") : null;
	    if (d2 != null && v2 != null)
	    	value2 = new Value(d2, v2);

	    extend1 = "true".equals(parameters.getString("extend1"));
	    extend2 = "true".equals(parameters.getString("extend2"));

    	color = parameters.hasParameter("color") ? parameters.getColor("color") : null;

	    object.invalidate();
	}
}
