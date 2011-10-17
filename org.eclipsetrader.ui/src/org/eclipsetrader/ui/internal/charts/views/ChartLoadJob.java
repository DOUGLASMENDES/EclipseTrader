/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts.views;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartLoadJob extends Job {

    ISecurity security;
    TimeSpan timeSpan;
    TimeSpan resolutionTimeSpan;

    IHistory history;
    IHistory subsetHistory;

    public ChartLoadJob(ISecurity security) {
        super(""); //$NON-NLS-1$

        this.security = security;
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

    public void setResolutionTimeSpan(TimeSpan resolutionTimeSpan) {
        this.resolutionTimeSpan = resolutionTimeSpan;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        try {
            buildHistory();
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, 0, Messages.ChartLoadJob_ExceptionMessage + security.getName(), e);
            ChartsUIActivator.log(status);
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    void buildHistory() {
        history = subsetHistory = getHistoryFor(security);

        if (timeSpan != null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);

            if (timeSpan.getUnits() == Units.Days) {
                int index = history.getOHLC().length - timeSpan.getLength();
                if (index < 0) {
                    index = 0;
                }
                Date firstDate = history.getOHLC()[index].getDate();
                subsetHistory = history.getSubset(firstDate, null, resolutionTimeSpan);
            }
            else {
                c = Calendar.getInstance();
                if (history.getLast() != null) {
                    c.setTime(history.getLast().getDate());
                }
                else {
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                }
                switch (timeSpan.getUnits()) {
                    case Months:
                        c.add(Calendar.MONTH, -timeSpan.getLength());
                        if (resolutionTimeSpan != null) {
                            subsetHistory = history.getSubset(c.getTime(), null, resolutionTimeSpan);
                        }
                        else {
                            subsetHistory = history.getSubset(c.getTime(), null);
                        }
                        break;
                    case Years:
                        c.add(Calendar.YEAR, -timeSpan.getLength());
                        subsetHistory = history.getSubset(c.getTime(), null);
                        break;
                }
            }
        }
    }

    IHistory getHistoryFor(ISecurity security) {
        IRepositoryService repository = ChartsUIActivator.getDefault().getRepositoryService();
        return repository.getHistoryFor(security);
    }

    public IHistory getHistory() {
        return history;
    }

    public IHistory getSubsetHistory() {
        return subsetHistory;
    }

    public TimeSpan getResolutionTimeSpan() {
        return resolutionTimeSpan;
    }
}
