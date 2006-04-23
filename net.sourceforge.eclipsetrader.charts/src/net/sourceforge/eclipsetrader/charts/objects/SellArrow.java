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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPlugin;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 */
public class SellArrow extends ObjectPlugin
{
    private Date date;
    private double value = 0;
    private Image image;
    private ImageData imageData;
    private Point p1, selected;
    private Point o1;
    private int lastX = -1, lastY = -1;
    private NumberFormat nf = ChartsPlugin.getPriceFormat();
    private Color color = new Color(null, 0, 0, 0);

    public SellArrow()
    {
        try
        {
            URL url = new URL(ChartsPlugin.getDefault().getBundle().getEntry("/"), "icons/etool16/sellarrow.gif");
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            image = descriptor.createImage();
            imageData = descriptor.getImageData();
        }
        catch (MalformedURLException e)
        {
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#dispose()
     */
    public void dispose()
    {
        color.dispose();
        if (image != null)
            image.dispose();
        super.dispose();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#isOverLine(int, int)
     */
    public boolean isOverLine(int x, int y)
    {
        if (p1 == null)
            return false;
        if (isOverHandle(x, y))
            return true;
        return (Math.abs(x - p1.x) <= 8 && Math.abs(y - p1.y) <= 16);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#isOverHandle(int, int)
     */
    public boolean isOverHandle(int x, int y)
    {
        if (p1 != null)
        {
            if (Math.abs(x - p1.x) <= 2 && Math.abs(y - p1.y) <= 2)
                return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#mouseDown(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseDown(PlotMouseEvent e)
    {
        if (p1 == null)
        {
            date = e.date;
            value = e.value;
            p1 = new Point(e.x, e.y);
            o1 = new Point(p1.x, p1.y);
            lastX = e.x;
            lastY = e.y;
        }
        else
        {
            selected = null;
            if (Math.abs(e.x - p1.x) <= 2 && Math.abs(e.y - p1.y) <= 2)
                selected = p1;
            else
            {
                o1 = new Point(p1.x, p1.y);
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

        getSettings().set("date", date);
        getSettings().set("value", value);
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
                    date = e.date;
                    value = e.roundedValue;
                }
                
                invalidateRectangle();
            }
        }
        else
        {
            Date date1 = getPlot().getDatePlot().mapToDate(o1.x + (e.x - lastX));
            if (date1 != null)
                this.date = date1;
            value = getPlot().getScaler().convertToValue(o1.y + (e.y - lastY));

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
        if (date != null)
        {
            gc.setForeground(color);
            Point location = getPlot().getIndicatorPlot().getPlotLocation();
            if (p1 == null)
                p1 = new Point(0, 0);
            p1.x = getPlot().getDatePlot().mapToScreen(date);
            p1.y = getPlot().getScaler().convertToY(value);
            gc.drawImage(image, p1.x + location.x - imageData.width / 2, p1.y - imageData.height);
            String s = nf.format(getPlot().getScaler().convertToRoundedValue(p1.y));
            gc.drawString(s, p1.x + location.x - imageData.width / 2, p1.y, true);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setSettings(Settings settings)
    {
        super.setSettings(settings);

        date = settings.getDate("date", null);
        value = settings.getDouble("value", 0).doubleValue();
        p1 = null;
    }
}
