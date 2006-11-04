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

import org.eclipse.jface.action.Action;

/**
 */
public class SetLast2YearsPeriodAction extends Action
{
    private ChartView view;

    public SetLast2YearsPeriodAction(ChartView view)
    {
        super(Messages.Period_2Years, AS_RADIO_BUTTON);
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        if (isChecked())
            ((ChartView)view).setPeriod(ChartView.PERIOD_LAST2YEARS);
    }
}
