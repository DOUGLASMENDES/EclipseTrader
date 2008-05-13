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

package org.eclipsetrader.news.internal.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.news.core.HeadLineStatus;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceListener;
import org.eclipsetrader.news.core.NewsEvent;
import org.eclipsetrader.news.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewsDecorator implements ILightweightLabelDecorator {
	private ImageDescriptor unreadedDescriptor;
	private ImageDescriptor readedDescriptor;

	private INewsService newsService;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private Set<IViewItem> decoratedObjects = new HashSet<IViewItem>();

	private INewsServiceListener newsListener = new INewsServiceListener() {
        public void newsServiceUpdate(NewsEvent event) {
        	Set<IViewItem> updatedObjects = new HashSet<IViewItem>();

        	HeadLineStatus[] status = event.getStatus();
        	for (int i = 0; i < status.length; i++) {
        		for (IViewItem element : decoratedObjects) {
        			ISecurity security = (ISecurity) element.getAdapter(ISecurity.class);
        			if (status[i].getHeadLine().contains(security))
        				updatedObjects.add(element);
        		}
        	}

        	if (updatedObjects.size() != 0)
        		fireLabelProviderChanged(new LabelProviderChangedEvent(NewsDecorator.this, updatedObjects.toArray()));
        }
	};

	public NewsDecorator() {
		if (Activator.getDefault() != null) {
			unreadedDescriptor = Activator.getDefault().getImageRegistry().getDescriptor("unreaded_ovr");
			readedDescriptor = Activator.getDefault().getImageRegistry().getDescriptor("readed_ovr");
		}
		newsService = getNewsService();
		newsService.addNewsServiceListener(newsListener);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    	listeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
		newsService.removeNewsServiceListener(newsListener);
		decoratedObjects.clear();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    	listeners.remove(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    public void decorate(Object element, IDecoration decoration) {
    	if (element instanceof IViewItem) {
    		IViewItem viewItem = (IViewItem) element;
			ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
			if (security != null) {
	    		if (newsService.hasUnreadedHeadLinesFor(security))
	    			decoration.addOverlay(unreadedDescriptor);
	    		else if (newsService.hasHeadLinesFor(security))
	    			decoration.addOverlay(readedDescriptor);
	    		decoratedObjects.add(viewItem);
			}
    	}
    }

	protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
    	Display.getDefault().asyncExec(new Runnable() {
            public void run() {
        		Object[] listeners = NewsDecorator.this.listeners.getListeners();
        		for (int i = 0; i < listeners.length; ++i) {
        			final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
        			SafeRunnable.run(new SafeRunnable() {
        				public void run() {
        					l.labelProviderChanged(event);
        				}
        			});

        		}
            }
    	});
	}

	protected INewsService getNewsService() {
		if (newsService == null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(INewsService.class.getName());
			if (serviceReference != null) {
				newsService = (INewsService) context.getService(serviceReference);
				context.ungetService(serviceReference);
			}
		}
		return newsService;
	}
}
