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

package net.sourceforge.eclipsetrader.charts.events;

import java.util.Date;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.eclipsetrader.charts.Plot;

/**
 * @see PlotMouseListener
 */
public class PlotMouseEvent
{
    /**
     * x-position relative to the chart surface
     */
    public int x;
    /**
     * y-position relative to the chart surface
     */
    public int y;
    /**
     * Mouse pointer position relative to the containing control
     */
    public Point mouse;
    public Display display;
    public int button;
    /**
     * Widget that originated the event
     */
    public Plot plot;
    /**
     * Date corresponding to the x position
     */
    public Date date;
    /**
     * Numeric value corresponding to the y position
     */
    public double value;
    /**
     * Same as <code>value</code> but rounded to the nearest tick
     */
    public double roundedValue;
    public boolean doit = true;

    /**
     * Constructs a new instance of this class.
     */
    public PlotMouseEvent()
    {
    }
}
