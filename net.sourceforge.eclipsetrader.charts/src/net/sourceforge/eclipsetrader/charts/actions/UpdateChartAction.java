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

import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Chart;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 */
public class UpdateChartAction implements IViewActionDelegate
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
        if (view instanceof ChartView)
        {
            String id = "";
            final Chart chart = ((ChartView)view).getChart();
            if (chart.getSecurity().getHistoryFeed() != null)
                id = chart.getSecurity().getHistoryFeed().getId();
            final IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(id);
            if (feed != null)
            {
                Job job = new Job("Update chart data") {
                    protected IStatus run(IProgressMonitor monitor)
                    {
                        monitor.beginTask("Updating " + chart.getSecurity().getDescription(), 1);
                        int interval = IHistoryFeed.INTERVAL_DAILY;
                        if (((ChartView)view).getInterval() < BarData.INTERVAL_DAILY)
                            interval = IHistoryFeed.INTERVAL_MINUTE;
                        feed.updateHistory(chart.getSecurity(), interval);
                        monitor.worked(1);
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
                job.setUser(true);
                job.schedule();
            }
            else
                MessageDialog.openError(view.getSite().getShell(), "Chart Update", "History feed invalid or not set !");
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
    }
}
