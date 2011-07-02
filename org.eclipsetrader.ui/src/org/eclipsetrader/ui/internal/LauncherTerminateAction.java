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

package org.eclipsetrader.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipsetrader.core.ILauncher;

public class LauncherTerminateAction extends Action {

    private ILauncher launcher;

    public LauncherTerminateAction(ILauncher launcher) {
        super(launcher.getName());
        this.launcher = launcher;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Job job = new Job(launcher.getName() + " Shutdown") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
                try {
                    launcher.terminate(monitor);
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }
}
