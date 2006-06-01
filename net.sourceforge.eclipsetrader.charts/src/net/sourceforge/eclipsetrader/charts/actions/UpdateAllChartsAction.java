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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 */
public class UpdateAllChartsAction implements IWorkbenchWindowActionDelegate, IViewActionDelegate
{

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        Job job = new Job("Update chart data") {
            protected IStatus run(IProgressMonitor monitor)
            {
                Object[] objs = CorePlugin.getRepository().allSecurities().toArray();
                monitor.beginTask("Updating charts", objs.length);
                for (int i = 0; i < objs.length; i++)
                {
                    Security security = (Security)objs[i];
                    Chart chart = (Chart)CorePlugin.getRepository().load(Chart.class, security.getId());
                    if (chart != null && security.getHistoryFeed() != null)
                    {
                        IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(security.getHistoryFeed().getId());
                        if (feed != null)
                        {
                            monitor.subTask("Updating " + security.getDescription());
                            feed.updateHistory(chart.getSecurity(), IHistoryFeed.INTERVAL_DAILY);
                        }
                    }
                    monitor.worked(1);
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
    }
}
