/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
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
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.charts.ChartView;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartLoadJob extends Job {
	ISecurity security;
	ChartView view;
	TimeSpan timeSpan;
	TimeSpan resolutionTimeSpan;

	IHistory history;

	public ChartLoadJob(ISecurity security, ChartView view) {
		super("");

		this.security = security;
		this.view = view;
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

			IOHLC[] values = history.getAdjustedOHLC();
			view.setRootDataSeries(new OHLCDataSeries(security.getName(), values, resolutionTimeSpan));
		} catch(Exception e) {
			Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, "Exception while loading chart " + security.getName(), e);
			ChartsUIActivator.log(status);
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	void buildHistory() {
		history = getHistoryFor(security);

    	if (timeSpan != null) {
    		Calendar c = Calendar.getInstance();
    		c.set(Calendar.HOUR_OF_DAY, 23);
    		c.set(Calendar.MINUTE, 59);
    		c.set(Calendar.SECOND, 59);
    		c.set(Calendar.MILLISECOND, 999);
    		Date lastDate = c.getTime();

    		if (timeSpan.getUnits() == Units.Days) {
    			int index = history.getOHLC().length - timeSpan.getLength();
    			if (index < 0)
    				index = 0;
    			Date firstDate = history.getOHLC()[index].getDate();
    			history = history.getSubset(firstDate, lastDate, resolutionTimeSpan);
    		}
    		else {
        		c = Calendar.getInstance();
        		if (history.getLast() != null)
        			c.setTime(history.getLast().getDate());
        		else {
            		c.set(Calendar.HOUR_OF_DAY, 0);
            		c.set(Calendar.MINUTE, 0);
            		c.set(Calendar.SECOND, 0);
            		c.set(Calendar.MILLISECOND, 0);
        		}
        		switch(timeSpan.getUnits()) {
        			case Months:
                		c.add(Calendar.MONTH, - timeSpan.getLength());
                		if (resolutionTimeSpan != null)
                			history = history.getSubset(c.getTime(), lastDate, resolutionTimeSpan);
                		else
                			history = history.getSubset(c.getTime(), lastDate);
        				break;
        			case Years:
                		c.add(Calendar.YEAR, - timeSpan.getLength());
                		history = history.getSubset(c.getTime(), lastDate);
        				break;
        		}
    		}
    	}
	}

	IHistory getHistoryFor(ISecurity security) {
		IRepositoryService repository = ChartsUIActivator.getDefault().getRepositoryService();
    	return repository.getHistoryFor(security);
    }

	public ChartView getView() {
    	return view;
    }

	public IHistory getHistory() {
    	return history;
    }
}
