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

package net.sourceforge.eclipsetrader.news.internal;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.news.NewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

public class EarlyStartup implements IStartup
{

    public EarlyStartup()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup()
    {
        if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.PREFS_UPDATE_NEWS))
        {
            Job job = new Job(Messages.EarlyStartup_JobName) {
                protected IStatus run(IProgressMonitor monitor)
                {
                    monitor.beginTask(Messages.EarlyStartup_TaskName, 1);
                    NewsPlugin.getDefault().startFeedSnapshot();
                    monitor.worked(1);
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
            job.setUser(false);
            job.schedule();
        }
    }
}
