/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.yahoo.internal.updater;

import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public class ListUpdateJob extends Job {
	AbstractListUpdateJob[] jobs;

	public ListUpdateJob(AbstractListUpdateJob[] jobs) {
		super(Messages.ListUpdateJob_Name);
		this.jobs = jobs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(YahooPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
		
		monitor.beginTask(getName(), jobs.length * 100);
		
		for (int i = 0; i < jobs.length; i++) {
			try {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
				IStatus s = jobs[i].run(subMonitor);
				status.add(s);
			} catch (Exception e) {
				status.add(new Status(IStatus.WARNING, YahooPlugin.PLUGIN_ID, -1, "", e)); //$NON-NLS-1$
			}
		}
		
		monitor.done();
		
		return status;
	}
}
