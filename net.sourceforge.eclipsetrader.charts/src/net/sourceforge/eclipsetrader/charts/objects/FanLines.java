/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts.objects;

import java.text.NumberFormat;
import java.util.Date;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPlugin;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;
import net.sourceforge.eclipsetrader.charts.internal.PixelTools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Fibonacci Fan Lines
 */
public class FanLines extends ObjectPlugin
{
    private Date date1, date2;
    private double value1 = 0, value2 = 0;
    private Color color = new Color(null, 0, 0, 224);
    private double line[] = { 0, 0.382, 0.5, 0.618, 0 };
    private Point p1, p2, selected;
    private Point o1, o2;
    private int lastX = -1, lastY = -1;
    private NumberFormat pcf = ChartsPlugin.getPercentageFormat();

    public FanLines()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#dispose()
     */
    public void dispose()
    {
        color.dispose();
        super.dispose();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#isOverLine(int, int)
     */
    public boolean isOverLine(int x, int y)
    {
        if (p1 == null || p2 == null)
            return false;
        if (isOverHandle(x, y))
            return true;
        return PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, p2.y);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#isOverHandle(int, int)
     */
    public boolean isOverHandle(int x, int y)
    {
        if (p1 != null && p2 != null)
        {
            if (Math.abs(x - p1.x) <= 2 && Math.abs(y - p1.y) <= 2)
                return true;
            else if (Math.abs(x - p2.x) <= 2 && Math.abs(y - p2.y) <= 2)
                return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#mouseDown(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseDown(PlotMouseEvent e)
    {
        if (p1 == null && p2 == null)
        {
            date1 = e.date;
            date2 = e.date;
            value1 = e.value;
            value2 = e.value;
            p1 = new Point(e.x, e.y);
            p2 = new Point(e.x, e.y);
            selected = p2;
        }
        else
        {
            selected = null;
            if (Math.abs(e.x - p1.x) <= 2 && Math.abs(e.y - p1.y) <= 2)
                selected = p1;
            else if (Math.abs(e.x - p2.x) <= 2 && Math.abs(e.y - p2.y) <= 2)
                selected = p2;
            else
            {
                o1 = new Point(p1.x, p1.y);
                o2 = new Point(p2.x, p2.y);
                lastX = e.x;
                lastY = e.y;
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#mouseUp(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseUp(PlotMouseEvent e)
    {
        selected = null;
        lastX = lastY = -1;

        getSettings().set("date1", date1);
        getSettings().set("value1", value1);
        getSettings().set("date2", date2);
        getSettings().set("value2", value2);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#mouseMove(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseMove(PlotMouseEvent e)
    {
        if (selected != null)
        {
            Point point = new Point(getPlot().getDatePlot().mapToScreen(e.date), getPlot().getScaler().convertToY(e.roundedValue));
            if (!point.equals(selected))
            {
                if (selected == p1)
                {
                    date1 = e.date;
                    value1 = e.roundedValue;
                }
                else if (selected == p2)
                {
                    date2 = e.date;
                    value2 = e.roundedValue;
                }
                
                invalidateRectangle();
            }
        }
        else
        {
            Date date1 = getPlot().getDatePlot().mapToDate(o1.x + (e.x - lastX));
            Date date2 = getPlot().getDatePlot().mapToDate(o2.x + (e.x - lastX));
            if (date1 != null && date2 != null)
            {
                this.date1 = date1;
                this.date2 = date2;
            }
            value1 = getPlot().getScaler().convertToValue(o1.y + (e.y - lastY));
            value2 = getPlot().getScaler().convertToValue(o2.y + (e.y - lastY));

            invalidateRectangle();
        }
    }
    
    private void invalidateRectangle()
    {
        getPlot().getIndicatorPlot().redraw();
        getPlot().getIndicatorPlot().update();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#drawObject(org.eclipse.swt.graphics.GC, boolean)
     */
    public void drawObject(GC gc, boolean selected)
    {
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(color);
        
        if (date1 != null && date2 != null)
        {
            Point location = getPlot().getIndicatorPlot().getPlotLocation();
            if (p1 == null)
                p1 = new Point(0, 0);
            p1.x = getPlot().getDatePlot().mapToScreen(date1);
            p1.y = getPlot().getScaler().convertToY(value1);
            if (p2 == null)
                p2 = new Point(0, 0);
            p2.x = getPlot().getDatePlot().mapToScreen(date2);
            p2.y = getPlot().getScaler().convertToY(value2);
            gc.drawLine (p1.x + location.x, p1.y, p2.x + location.x, p2.y);

            float dx = (p2.x - p1.x);
            float dy = p2.y - p1.y;

            for (int i = 0; i < line.length; i++)
            {
                if (line[i] == 0)
                    continue;
                double k = (dx == 0) ? 1 : dy * (line[i] / 100.0) / dx;
                drawExtendedLine(gc, p1.x, p1.y, p2.x, yOfLine(p2.x, p1.x, p1.y, k));
                String s = pcf.format(line[i]) + "%";
                gc.drawString(s, p2.x + location.x, yOfLine(p2.x, p1.x, p1.y, k), true);
            }
            
            if (selected)
            {
                gc.setBackground(color);
                gc.fillRectangle(p1.x + location.x - 2, p1.y - 2, 5, 5);
                gc.fillRectangle(p2.x + location.x - 2, p2.y - 2, 5, 5);
            }
        }
    }
    
    private int yOfLine(int x, int xorigin, int yorigin, double k)
    {
        return (int)(yorigin + (x - xorigin) * k);
    }
    
    private void drawExtendedLine(GC gc, int x, int y, int x2, int y2)
    {
        int ydiff = y - y2;
        int xdiff = x2 - x;
        Point location = getPlot().getIndicatorPlot().getPlotLocation();
        Rectangle rect = getPlot().getIndicatorPlot().getPlotBounds();

        gc.drawLine (x + location.x, y, x2 + location.x, y2);

        if (xdiff != 0 || ydiff != 0)
        {
            while(x2 > 0 && x2 < rect.width && y2 > 0 && y2 < rect.height)
            {
                x = x2;
                y = y2;
                x2 += xdiff;
                y2 -= ydiff;
                gc.drawLine (x + location.x, y, x2 + location.x, y2);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setSettings(Settings settings)
    {
        super.setSettings(settings);

        date1 = settings.getDate("date1", null);
        date2 = settings.getDate("date2", null);
        value1 = settings.getDouble("value1", 0).doubleValue();
        value2 = settings.getDouble("value2", 0).doubleValue();
        color = settings.getColor("color", color.getRGB());
        line[0] = settings.getDouble("line1", 0).doubleValue();
        line[1] = settings.getDouble("line2", 38.2).doubleValue();
        line[2] = settings.getDouble("line3", 50.).doubleValue();
        line[3] = settings.getDouble("line4", 61.8).doubleValue();
        line[4] = settings.getDouble("line5", 0).doubleValue();
        p1 = p2 = null;
    }
}
