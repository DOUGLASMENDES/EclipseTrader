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

package net.sourceforge.eclipsetrader.charts.actions;

import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.jface.action.Action;

/**
 */
public class Set5MinuteIntervalAction extends Action
{
    private ChartView view;

    public Set5MinuteIntervalAction(ChartView view)
    {
        super(Messages.Interval_5Min, AS_RADIO_BUTTON);
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        if (isChecked())
            ((ChartView)view).setInterval(BarData.INTERVAL_MINUTE5);
    }
}
