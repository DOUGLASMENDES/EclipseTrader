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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.ILauncher;
import org.eclipsetrader.news.core.INewsProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewsServiceLauncher implements ILauncher, IExecutableExtension {
	private String id;
	private String name;

	public NewsServiceLauncher() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#launch(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(IProgressMonitor monitor) {
		/*Display.getDefault().syncExec(new Runnable() {
            public void run() {
            	IWorkbenchPage[] page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
            	for (int p = 0; p < page.length; p++) {
                	IViewReference[] viewReference = page[p].getViewReferences();
                	for (int i = 0; i < viewReference.length; i++) {
                		if (viewReference[i].getId().equals(HeadLineViewer.VIEW_ID))
                			viewReference[i].getView(true);
                	}
            	}
            }
		});*/

		try {
    		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(NewsService.class.getName());
    		if (serviceReference != null) {
    			NewsService newsService = (NewsService) context.getService(serviceReference);

    			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    			for (INewsProvider newsProvider : newsService.getProviders()) {
    				if (store.getBoolean(newsProvider.getId()))
    					newsProvider.start();
    			}
    		}
    		context.ungetService(serviceReference);
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading news service", e);
    		Activator.getDefault().getLog().log(status);
    	}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#terminate(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void terminate(IProgressMonitor monitor) {
    	try {
    		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(NewsService.class.getName());
    		if (serviceReference != null) {
    			NewsService newsService = (NewsService) context.getService(serviceReference);
    			for (INewsProvider newsProvider : newsService.getProviders())
					newsProvider.stop();
    		}
    		context.ungetService(serviceReference);
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading news service", e);
    		Activator.getDefault().getLog().log(status);
    	}
	}
}
