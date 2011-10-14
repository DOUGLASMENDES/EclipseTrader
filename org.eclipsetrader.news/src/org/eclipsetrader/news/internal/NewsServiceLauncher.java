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

package org.eclipsetrader.news.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.ILauncher;
import org.eclipsetrader.news.core.HeadLineStatus;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.core.INewsServiceListener;
import org.eclipsetrader.news.core.NewsEvent;
import org.eclipsetrader.ui.INotification;
import org.eclipsetrader.ui.INotificationService;
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
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#launch(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
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
            ServiceReference<NewsService> serviceReference = context.getServiceReference(NewsService.class);
            if (serviceReference != null) {
                NewsService newsService = context.getService(serviceReference);

                newsService.addNewsServiceListener(new INewsServiceListener() {

                    @Override
                    public void newsServiceUpdate(NewsEvent event) {
                        int count = 0;
                        for (HeadLineStatus status : event.getStatus()) {
                            if (status.getKind() == HeadLineStatus.ADDED) {
                                count++;
                            }
                        }
                        if (count != 0) {
                            try {
                                BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                                ServiceReference<INotificationService> serviceReference = context.getServiceReference(INotificationService.class);
                                if (serviceReference != null) {
                                    INotificationService notificationService = context.getService(serviceReference);
                                    notificationService.popupNotification(new INotification[] {
                                        new HeadLineCountNotification(count)
                                    });
                                }
                                context.ungetService(serviceReference);
                            } catch (Exception e) {
                                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading news service", e);
                                Activator.getDefault().getLog().log(status);
                            }
                        }
                    }
                });

                IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                for (INewsProvider newsProvider : newsService.getProviders()) {
                    if (store.getBoolean(newsProvider.getId())) {
                        newsProvider.start();
                    }
                }
            }
            context.ungetService(serviceReference);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading news service", e);
            Activator.getDefault().getLog().log(status);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#terminate(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void terminate(IProgressMonitor monitor) {
        try {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(NewsService.class.getName());
            if (serviceReference != null) {
                NewsService newsService = (NewsService) context.getService(serviceReference);
                for (INewsProvider newsProvider : newsService.getProviders()) {
                    newsProvider.stop();
                }
            }
            context.ungetService(serviceReference);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading news service", e);
            Activator.getDefault().getLog().log(status);
        }
    }
}
