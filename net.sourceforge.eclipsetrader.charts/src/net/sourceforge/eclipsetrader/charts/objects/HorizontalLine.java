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

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPlugin;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Horizontal Line
 */
public class HorizontalLine extends ObjectPlugin
{
    private Double value;
    private Color color = new Color(null, 0, 0, 224);
    private Point p1, p2;
    private int height = 1;
    private NumberFormat nf = ChartsPlugin.getPriceFormat();

    public HorizontalLine()
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
        if (p1 != null && p2 != null)
        {
            if (Math.abs(y - p1.y) <= 2)
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
            value = new Double(e.roundedValue);
            p1 = new Point(e.x, e.y);
            p2 = new Point(e.x, e.y);
            
            int rx = Math.min(p1.x, p2.x);
            getPlot().getIndicatorPlot().redraw(rx - 3, p1.y - height - 3, Math.abs(p2.x - p1.x) + 6, height + 6, false);
            getPlot().getIndicatorPlot().update();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#mouseUp(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseUp(PlotMouseEvent e)
    {
        getSettings().set("value", value); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#mouseMove(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseMove(PlotMouseEvent e)
    {
        Point location = getPlot().getIndicatorPlot().getPlotLocation();
        value = new Double(e.roundedValue);
        
        int rx = Math.min(p1.x + location.x, p2.x + location.x);
        getPlot().getIndicatorPlot().redraw(rx - 2, p1.y - height - 2, Math.abs(p2.x - p1.x) + 6, height + 5, false);
        getPlot().getIndicatorPlot().update();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#drawObject(org.eclipse.swt.graphics.GC, boolean)
     */
    public void drawObject(GC gc, boolean selected)
    {
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(color);
        
        BarData barData = getPlot().getDatePlot().getBarData();
        int size = barData.size();

        if (value != null && size != 0)
        {
            Point location = getPlot().getIndicatorPlot().getPlotLocation();
            if (p1 == null)
                p1 = new Point(0, 0);
            p1.x = getPlot().getDatePlot().mapToScreen(barData.getDate(0));
            p1.y = getPlot().getScaler().convertToY(value.doubleValue());
            if (p2 == null)
                p2 = new Point(0, 0);
            p2.x = getPlot().getDatePlot().mapToScreen(barData.getDate(size - 1));
            p2.y = p1.y;
            gc.drawLine (p1.x + location.x, p1.y, p2.x + location.x, p2.y);
            
            String s = nf.format(value);
            Rectangle rect = gc.getClipping();
            int x = rect.width - getPlot().getIndicatorPlot().getMarginWidth() - getPlot().getIndicatorPlot().getGridWidth() / 2 - gc.stringExtent(s).x;
            height = gc.stringExtent(s).y;
            gc.drawString(s, x, p1.y - height, true);
            
            if (selected)
            {
                gc.setBackground(color);
                gc.fillRectangle(p1.x + location.x - 2, p1.y - 2, 5, 5);
                gc.fillRectangle(p2.x + location.x - 2, p2.y - 2, 5, 5);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setSettings(Settings settings)
    {
        super.setSettings(settings);

        color = settings.getColor("color", color.getRGB()); //$NON-NLS-1$
        value = settings.getDouble("value", null); //$NON-NLS-1$
    }
}
