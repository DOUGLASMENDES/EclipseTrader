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

import net.sourceforge.eclipsetrader.charts.Indicator;
import net.sourceforge.eclipsetrader.charts.ObjectPlugin;
import net.sourceforge.eclipsetrader.charts.Plot;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;

public class PlotSelectionEvent extends TypedEvent
{
    static final long serialVersionUID = 7634624784732351271L;
    public Plot plot;
    public Indicator indicator;
    public ObjectPlugin object;

    public PlotSelectionEvent(Event e)
    {
        super(e);
    }

}
