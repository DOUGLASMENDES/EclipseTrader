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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StopFeedAction implements IWorkbenchWindowActionDelegate {

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
    				IFeedConnector[] connector = getFeedService().getConnectors();
    				for (int i = 0; i < connector.length; i++)
    					connector[i].disconnect();

    				IBroker[] brokerConnector = getTradingService().getBrokerConnectors();
    				for (int i = 0; i < brokerConnector.length; i++)
    					brokerConnector[i].disconnect();
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

    protected IFeedService getFeedService() {
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
    		IFeedService service = (IFeedService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading feed service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
    }

    protected ITradingService getTradingService() {
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
    		ITradingService service = (ITradingService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading trading service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
    }
}
