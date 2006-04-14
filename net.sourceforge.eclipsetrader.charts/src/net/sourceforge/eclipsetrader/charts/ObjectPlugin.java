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

package net.sourceforge.eclipsetrader.charts;


import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseListener;

import org.eclipse.swt.graphics.GC;

/**
 * Base abstract class for all chart object plugins
 */
public abstract class ObjectPlugin implements PlotMouseListener
{
    private Plot plot;
    private Settings settings = new Settings();

    public ObjectPlugin()
    {
    }
    
    public void dispose()
    {
    }

    public Plot getPlot()
    {
        return plot;
    }

    public void setPlot(Plot plot)
    {
        this.plot = plot;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public void setSettings(Settings settings)
    {
        this.settings = settings;
    }

    /**
     * Returns whether the point at (x,y) is over the receiver.
     * 
     * @param x
     * @param y
     * @return true if the point is on the receiver, false otherwise
     */
    public boolean isOverLine(int x, int y)
    {
        return false;
    }

    /**
     * Returns whether the point at (x,y) is over the receiver's grab handles.
     * 
     * @param x
     * @param y
     * @return true if the point is on the grab handles, false otherwise
     */
    public boolean isOverHandle(int x, int y)
    {
        return false;
    }

    public abstract void drawObject(GC gc, boolean selected);

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.events.PlotMouseListener#mouseDown(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseDown(PlotMouseEvent e)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.events.PlotMouseListener#mouseUp(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseUp(PlotMouseEvent e)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.events.PlotMouseListener#mouseMove(net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent)
     */
    public void mouseMove(PlotMouseEvent e)
    {
    }
}
