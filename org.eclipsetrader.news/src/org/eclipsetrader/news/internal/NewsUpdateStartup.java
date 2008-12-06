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

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.internal.ui.HeadLineViewer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewsUpdateStartup implements IStartup {

	public NewsUpdateStartup() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		Display.getDefault().syncExec(new Runnable() {
            public void run() {
            	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            	IViewReference[] viewReference = page.getViewReferences();
            	for (int i = 0; i < viewReference.length; i++) {
            		if (viewReference[i].getId().equals(HeadLineViewer.VIEW_ID))
            			viewReference[i].getView(true);
            	}
            }
		});

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(Activator.PREFS_UPDATE_ON_STARTUP)) {
	    	try {
	    		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
	    		ServiceReference serviceReference = context.getServiceReference(NewsService.class.getName());
	    		if (serviceReference != null) {
	    			NewsService newsService = (NewsService) context.getService(serviceReference);
	    			for (INewsProvider newsProvider : newsService.getProviders())
						newsProvider.start();
	    		}
	    		context.ungetService(serviceReference);
	    	} catch(Exception e) {
	    		Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading news service", e);
	    		Activator.getDefault().getLog().log(status);
	    	}
		}
	}
}
