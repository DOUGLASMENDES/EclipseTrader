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

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.dialogs.CustomPeriodDialog;
import net.sourceforge.eclipsetrader.charts.views.ChartView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 */
public class SetCustomPeriodAction implements IViewActionDelegate
{
    private IViewPart view;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        if (view instanceof ChartView && action.isChecked())
        {
            String begin = ChartsPlugin.getDefault().getPreferenceStore().getString(ChartView.PERIOD_BEGIN + view.getViewSite().getSecondaryId());
            String end = ChartsPlugin.getDefault().getPreferenceStore().getString(ChartView.PERIOD_END + view.getViewSite().getSecondaryId());
            CustomPeriodDialog dlg = new CustomPeriodDialog(begin, end);
            if (dlg.open() == Dialog.OK)
                ((ChartView)view).setPeriod(dlg.getBeginDate(), dlg.getEndDate());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        if (view instanceof ChartView)
            action.setChecked(((ChartView)view).getPeriod() == ChartView.PERIOD_CUSTOM);
    }
}
