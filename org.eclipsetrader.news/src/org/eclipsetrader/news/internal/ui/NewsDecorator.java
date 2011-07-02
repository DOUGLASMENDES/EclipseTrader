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

package org.eclipsetrader.news.internal.ui;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceListener;
import org.eclipsetrader.news.core.NewsEvent;
import org.eclipsetrader.news.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewsDecorator implements ILightweightLabelDecorator {

    private ImageDescriptor unreadedDescriptor;
    private ImageDescriptor readedDescriptor;
    private boolean enabled;

    private INewsService newsService;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private INewsServiceListener newsListener = new INewsServiceListener() {

        @Override
        public void newsServiceUpdate(NewsEvent event) {
            fireLabelProviderChanged(new LabelProviderChangedEvent(NewsDecorator.this));
        }
    };

    IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(Activator.PREFS_ENABLE_DECORATORS)) {
                enabled = ((Boolean) event.getNewValue()).booleanValue();
                fireLabelProviderChanged(new LabelProviderChangedEvent(NewsDecorator.this));
            }
        }
    };

    public NewsDecorator() {
        if (Activator.getDefault() != null) {
            unreadedDescriptor = Activator.getDefault().getImageRegistry().getDescriptor("unreaded_ovr");
            readedDescriptor = Activator.getDefault().getImageRegistry().getDescriptor("readed_ovr");

            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            store.addPropertyChangeListener(propertyChangeListener);
            enabled = store.getBoolean(Activator.PREFS_ENABLE_DECORATORS);
        }
        newsService = getNewsService();
        newsService.addNewsServiceListener(newsListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        if (Activator.getDefault() != null) {
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            store.removePropertyChangeListener(propertyChangeListener);
        }

        newsService.removeNewsServiceListener(newsListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (enabled) {
            if (element instanceof IViewItem) {
                IViewItem viewItem = (IViewItem) element;
                ISecurity security = (ISecurity) viewItem.getAdapter(ISecurity.class);
                if (security != null) {
                    if (newsService.hasUnreadedHeadLinesFor(security)) {
                        decoration.addOverlay(unreadedDescriptor);
                    }
                    else if (newsService.hasHeadLinesFor(security)) {
                        decoration.addOverlay(readedDescriptor);
                    }
                }
            }
        }
    }

    protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                Object[] listeners = NewsDecorator.this.listeners.getListeners();
                for (int i = 0; i < listeners.length; ++i) {
                    final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
                    SafeRunnable.run(new SafeRunnable() {

                        @Override
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
