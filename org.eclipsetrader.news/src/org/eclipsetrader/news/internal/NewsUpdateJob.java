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

package org.eclipsetrader.news.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceRunnable;
import org.eclipsetrader.news.internal.connectors.YahooNewsFetcher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewsUpdateJob extends Job {

	public NewsUpdateJob() {
		super("News Update");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("News Update", IProgressMonitor.UNKNOWN);
		try {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(INewsService.class.getName());
			if (serviceReference != null) {
				final INewsService service = (INewsService) context.getService(serviceReference);
				if (service != null) {
					YahooNewsFetcher fetcher = new YahooNewsFetcher();
					final IHeadLine[] headLines = fetcher.fetchHeadLines(getRepositoryService().getSecurities());
					service.runInService(new INewsServiceRunnable() {
                        public IStatus run(IProgressMonitor monitor) throws Exception {
                        	service.addHeadLines(headLines);
	                        return Status.OK_STATUS;
                        }
					}, monitor);
				}
				context.ungetService(serviceReference);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected IRepositoryService getRepositoryService() {
		try {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
			return service;
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e); //$NON-NLS-1$
			Activator.getDefault().getLog().log(status);
		}
		return null;
	}
}
