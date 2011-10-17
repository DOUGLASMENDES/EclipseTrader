/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public interface IGraphics {

    public Rectangle getBounds();

    public void drawLine(int x1, int y1, int x2, int y2);

    public void drawPolyline(Point[] pointArray);

    public void drawRectangle(int x, int y, int width, int height);

    public void drawRectangle(Rectangle rect);

    public void fillRectangle(int x, int y, int width, int height);

    public void fillRectangle(Rectangle rect);

    public void fillPolygon(int[] pointArray);

    public void setLineStyle(int lineStyle);

    public void setLineDash(int[] dash);

    public Point mapToPoint(Object xValue, Object yValue);

    public int mapToVerticalAxis(Object value);

    public Object mapToVerticalValue(int y);

    public int mapToHorizontalAxis(Object value);

    public Object mapToHorizontalValue(int x);

    public RGB getBackgroundColor();

    public void setBackgroundColor(RGB rgb);

    public RGB getForegroundColor();

    public void setForegroundColor(RGB rgb);

    public Font getFont();

    public void setFont(Font font);

    public void drawString(String s, int x, int y);

    public Point stringExtent(String s);

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    public void setLineWidth(int lineWidth);

    /**
     * Pushes the current state of this graphics object onto a stack.
     */
    public void pushState();

    /**
     * Pops the previous state of this graphics object off the stack (if <code>pushState()</code>
     * has previously been called) and restores the current state to that popped state.
     */
    public void popState();
}
