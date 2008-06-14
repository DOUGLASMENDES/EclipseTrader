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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class Graphics implements IGraphics {
	private GC gc;
	private IAxis horizontalAxis;
	private IAxis verticalAxis;
	private Point location;
	private Rectangle bounds;

	private RGB foregroundColor;
	private RGB backgroundColor;
	private Map<RGB, Color> colorMap;

	private List<Map<String, Object>> stack;

	public Graphics(Drawable drawable, Point location, IAxis horizontalAxis, IAxis verticalAxis) {
	    this.gc = new GC(drawable);

	    this.horizontalAxis = horizontalAxis;
	    this.verticalAxis = verticalAxis;
	    this.location = location;

	    if (drawable instanceof Image)
	    	this.bounds = ((Image) drawable).getBounds();
	    if (drawable instanceof Control)
	    	this.bounds = ((Control) drawable).getBounds();

	    foregroundColor = gc.getForeground().getRGB();
	    backgroundColor = gc.getBackground().getRGB();
	    colorMap = new HashMap<RGB, Color>();

	    stack = new ArrayList<Map<String,Object>>();
    }

	public void dispose() {
		for (Color c : colorMap.values())
			c.dispose();
		colorMap.clear();
		gc.dispose();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#getBounds()
     */
    public Rectangle getBounds() {
    	return bounds;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#drawLine(int, int, int, int)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
    	gc.drawLine(x1, y1, x2, y2);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#drawPolyline(org.eclipse.swt.graphics.Point[])
     */
    public void drawPolyline(Point[] pointArray) {
		if (pointArray == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		int[] array = new int[pointArray.length * 2];
		for (int i = 0, index = 0; i < array.length; i += 2, index++) {
			array[i] += pointArray[index].x;
			array[i + 1] += pointArray[index].y;
		}
		gc.drawPolyline(array);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#drawRectangle(int, int, int, int)
     */
    public void drawRectangle(int x, int y, int width, int height) {
    	gc.drawRectangle(x, y, width, height);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#drawRectangle(org.eclipse.swt.graphics.Rectangle)
     */
    public void drawRectangle(Rectangle rect) {
    	gc.drawRectangle(rect.x, rect.y, rect.width, rect.height);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#fillRectangle(int, int, int, int)
     */
    public void fillRectangle(int x, int y, int width, int height) {
    	gc.fillRectangle(x, y, width, height);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#fillRectangle(org.eclipse.swt.graphics.Rectangle)
     */
    public void fillRectangle(Rectangle rect) {
    	gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#fillPolygon(int[])
     */
    public void fillPolygon(int[] pointArray) {
    	gc.fillPolygon(pointArray);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#setLineStyle(int)
     */
    public void setLineStyle(int lineStyle) {
    	if (lineStyle == SWT.LINE_DASH) {
			int[] dashes = { 3, 3 };
			gc.setLineDash(dashes);
    	}
    	else if (lineStyle == SWT.LINE_DOT) {
    		int[] dashes = { 1, 2 };
    		gc.setLineDash(dashes);
    	}
    	else
    		gc.setLineStyle(lineStyle);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#setLineDash(int[])
     */
    public void setLineDash(int[] dash) {
		gc.setLineDash(dash);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#drawString(java.lang.String, int, int)
     */
    public void drawString(String s, int x, int y) {
    	gc.drawString(s, x, y, true);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#stringExtent(java.lang.String)
     */
    public Point stringExtent(String s) {
	    return gc.stringExtent(s);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#mapToPoint(java.lang.Object, java.lang.Object)
     */
    public Point mapToPoint(Object xValue, Object yValue) {
	    return new Point(horizontalAxis.mapToAxis(xValue) - location.x, verticalAxis.mapToAxis(yValue));
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#mapToHorizontalAxis(java.lang.Object)
     */
    public int mapToHorizontalAxis(Object value) {
	    return horizontalAxis.mapToAxis(value) - location.x;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#mapToHorizontalValue(int)
     */
    public Object mapToHorizontalValue(int x) {
	    return horizontalAxis.mapToValue(x + location.x);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#mapToVerticalAxis(java.lang.Object)
     */
    public int mapToVerticalAxis(Object value) {
	    return verticalAxis.mapToAxis(value);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#mapToVerticalValue(int)
     */
    public Object mapToVerticalValue(int y) {
	    return verticalAxis.mapToValue(y);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#getBackgroundColor()
     */
    public RGB getBackgroundColor() {
	    return backgroundColor;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#setBackgroundColor(org.eclipse.swt.graphics.RGB)
     */
    public void setBackgroundColor(RGB rgb) {
    	if (rgb == null)
    		return;
    	this.backgroundColor = rgb;
    	Color c = colorMap.get(rgb);
    	if (c == null) {
    		c = new Color(gc.getDevice(), rgb);
    		colorMap.put(rgb, c);
    	}
    	gc.setBackground(c);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#getForegroundColor()
     */
    public RGB getForegroundColor() {
	    return foregroundColor;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#setForegroundColor(org.eclipse.swt.graphics.RGB)
     */
    public void setForegroundColor(RGB rgb) {
    	if (rgb == null)
    		return;
    	this.foregroundColor = rgb;
    	Color c = colorMap.get(rgb);
    	if (c == null) {
    		c = new Color(gc.getDevice(), rgb);
    		colorMap.put(rgb, c);
    	}
    	gc.setForeground(c);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#setLineWidth(int)
     */
    public void setLineWidth(int lineWidth) {
    	gc.setLineWidth(lineWidth == 1 ? 0 : lineWidth);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#popState()
     */
    public void popState() {
    	if (stack.size() != 0) {
        	Map<String, Object> state = stack.get(0);
        	stack.remove(0);

        	setForegroundColor((RGB) state.get("foreground-color"));
    		setBackgroundColor((RGB) state.get("background-color"));
    		gc.setLineStyle((Integer) state.get("line-style"));
    		gc.setLineDash((int[]) state.get("line-dash"));
    		gc.setLineWidth(((Integer) state.get("line-width")).intValue());
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#pushState()
     */
    public void pushState() {
    	Map<String, Object> state = new HashMap<String, Object>();
    	state.put("foreground-color", getForegroundColor());
    	state.put("background-color", getBackgroundColor());
    	state.put("line-style", gc.getLineStyle());
    	state.put("line-dash", gc.getLineDash());
    	state.put("line-width", new Integer(gc.getLineWidth()));
    	stack.add(0, state);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IGraphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    	gc.drawArc(x, y, width, height, startAngle, arcAngle);
    }
}
