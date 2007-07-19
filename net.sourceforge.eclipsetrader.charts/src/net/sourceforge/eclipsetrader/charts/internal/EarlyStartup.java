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

package net.sourceforge.eclipsetrader.charts.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

public class EarlyStartup implements IStartup
{
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$

    public EarlyStartup()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup()
    {
        if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.PREFS_UPDATE_HISTORY))
        {
            if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.PREFS_UPDATE_HISTORY_ONCE))
            {
                Calendar today = Calendar.getInstance();
                Calendar last = Calendar.getInstance();
                try
                {
                    last.setTime(dateFormat.parse(CorePlugin.getDefault().getPreferenceStore().getString(CorePlugin.PREFS_UPDATE_HISTORY_LAST)));
                    if (today.get(Calendar.DAY_OF_MONTH) == last.get(Calendar.DAY_OF_MONTH) &&
                            today.get(Calendar.MONTH) == last.get(Calendar.MONTH) &&
                            today.get(Calendar.YEAR) == last.get(Calendar.YEAR))
                            return;
                }
                catch (ParseException e) {
                }
                CorePlugin.getDefault().getPreferenceStore().setValue(CorePlugin.PREFS_UPDATE_HISTORY_LAST, dateFormat.format(today.getTime()));
            }
            
            Job job = new Job(Messages.EarlyStartup_UpdateChartData) {
                protected IStatus run(IProgressMonitor monitor)
                {
                    Object[] objs = CorePlugin.getRepository().allSecurities().toArray();
                    monitor.beginTask(Messages.EarlyStartup_UpdatingCharts, objs.length);
                    for (int i = 0; i < objs.length; i++)
                    {
                        Security security = (Security)objs[i];
                        Chart chart = (Chart)CorePlugin.getRepository().load(Chart.class, security.getId());
                        if (chart != null && security.getHistoryFeed() != null)
                        {
                            IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(security.getHistoryFeed().getId());
                            if (feed != null)
                            {
                                monitor.subTask(Messages.EarlyStartup_Updating + security.getDescription());
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
            job.setUser(false);
            job.schedule();
        }
    }
}
