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

import java.util.Date;

import net.sourceforge.eclipsetrader.charts.dialogs.CustomPeriodDialog;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Chart;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

/**
 */
public class SetCustomPeriodAction extends Action
{
    private ChartView view;

    public SetCustomPeriodAction(ChartView view)
    {
        super("Custom...", AS_RADIO_BUTTON);
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        if (isChecked())
        {
            Chart chart = view.getChart();
            Date begin = chart.getBeginDate();
            if (begin == null && chart.getSecurity().getHistory().size() != 0)
                begin = ((Bar) chart.getSecurity().getHistory().get(0)).getDate();
            Date end = chart.getEndDate();
            if (end == null && chart.getSecurity().getHistory().size() != 0)
                end = ((Bar) chart.getSecurity().getHistory().get(chart.getSecurity().getHistory().size() - 1)).getDate();
            CustomPeriodDialog dlg = new CustomPeriodDialog(view.getViewSite().getShell(), chart.getSecurity().getHistory(), begin, end);
            if (dlg.open() == Dialog.OK)
                view.setPeriod(dlg.getBeginDate(), dlg.getEndDate());
            else
                view.updateActionBars();
        }
    }
}
