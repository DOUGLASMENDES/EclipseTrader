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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipsetrader.core.ILauncher;

public class StopFeedAction implements IWorkbenchWindowActionDelegate {
	public static final String LAUNCHERS_EXTENSION_ID = "org.eclipsetrader.core.launchers";

	public StopFeedAction() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		Job job = new Job("Feed Shutdown") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
            	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
            	try {
            		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCHERS_EXTENSION_ID);
            		if (extensionPoint != null) {
                		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();

                		boolean startAll = UIActivator.getDefault().getPreferenceStore().getBoolean("RUN_ALL_LAUNCHERS");
                		Set<String> set = new HashSet<String>(Arrays.asList(UIActivator.getDefault().getPreferenceStore().getString("RUN_LAUNCHERS").split(";")));

                    	for (int j = 0; j < configElements.length; j++) {
                			String id = configElements[j].getAttribute("id"); //$NON-NLS-1$
                			if (startAll || set.contains(id)) {
                    			try {
                    				ILauncher launcher = (ILauncher) configElements[j].createExecutableExtension("class");
                    				if (launcher != null)
                    					launcher.terminate(monitor);
                    			} catch (Exception e) {
                    	    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error launching " + id, e);
                    	    		UIActivator.getDefault().getLog().log(status);
                    			}
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
