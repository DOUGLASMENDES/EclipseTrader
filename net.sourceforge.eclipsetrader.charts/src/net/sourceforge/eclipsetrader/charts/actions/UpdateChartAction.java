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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.visitors.ChartVisitorAdapter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
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
            final Chart chart = ((ChartView)view).getChart();

            final List securities = new ArrayList();
            chart.accept(new ChartVisitorAdapter() {
                public void visit(Chart chart)
                {
                    securities.add(chart.getSecurity());
                }

                public void visit(ChartIndicator indicator)
                {
                    String securityId = (String)indicator.getParameters().get("securityId"); //$NON-NLS-1$
                    if (securityId != null)
                    {
                        Security security = (Security)CorePlugin.getRepository().load(Security.class, new Integer(securityId));
                        if (security != null)
                            securities.add(security);
                    }
                }
            });
            
            Job job = new Job(Messages.UpdateChartAction_UpdateChartData) {
                protected IStatus run(IProgressMonitor monitor)
                {
                    monitor.beginTask(Messages.UpdateChartAction_UpdateChartData, securities.size());

                    for (int i = 0; i < securities.size(); i++)
                    {
                        Security security = (Security)securities.get(i);
                        monitor.subTask(Messages.UpdateChartAction_Updating + security.getDescription().replaceAll("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$
                        
                        int interval = IHistoryFeed.INTERVAL_DAILY;
                        if (((ChartView)view).getInterval() < BarData.INTERVAL_DAILY)
                            interval = IHistoryFeed.INTERVAL_MINUTE;
                        
                        String id = ""; //$NON-NLS-1$
                        if (security.getHistoryFeed() != null)
                            id = security.getHistoryFeed().getId();
                        IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(id);
                        if (feed != null)
                        {
                            Observer observer = new Observer() {
                                public void update(Observable o, Object arg)
                                {
                                    chart.setChanged();
                                }
                            };
                            security.addObserver(observer);
                            feed.updateHistory(security, interval);
                            security.deleteObserver(observer);
                        }
                        
                        monitor.worked(1);
                    }
                    
                    chart.notifyObservers();
                    monitor.done();

                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
    }
}
