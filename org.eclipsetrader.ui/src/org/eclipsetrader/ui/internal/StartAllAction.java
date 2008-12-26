/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipsetrader.core.ILauncher;

public class StartAllAction extends Action {
	public static final String LAUNCHERS_EXTENSION_ID = "org.eclipsetrader.core.launchers";

	public StartAllAction() {
		super("All");
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
		Job job = new Job("Services Startup") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
            	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
            	try {
            		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCHERS_EXTENSION_ID);
            		if (extensionPoint != null) {
                		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();

                		for (int j = 0; j < configElements.length; j++) {
                			String id = configElements[j].getAttribute("id"); //$NON-NLS-1$
                			try {
                				ILauncher launcher = (ILauncher) configElements[j].createExecutableExtension("class");
                				if (launcher != null)
                					launcher.launch(monitor);
                			} catch (Exception e) {
                	    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error launching " + id, e);
                	    		UIActivator.getDefault().getLog().log(status);
                			}
                		}
            		}
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
