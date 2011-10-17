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

package org.eclipsetrader.ui.internal.trading;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.trading.Activator;
import org.eclipsetrader.core.trading.AlertEvent;
import org.eclipsetrader.core.trading.IAlertListener;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WatchlistAlertDecorator implements ILightweightLabelDecorator, IAlertListener {

    private Color foreground;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private ServiceReference serviceReference;
    private IAlertService alertService;

    public WatchlistAlertDecorator() {
        this(Activator.getDefault().getBundle().getBundleContext());
    }

    protected WatchlistAlertDecorator(BundleContext context) {
        serviceReference = context.getServiceReference(IAlertService.class.getName());
        alertService = (IAlertService) context.getService(serviceReference);
        alertService.addAlertListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IAdaptable) {
            element = ((IAdaptable) element).getAdapter(ISecurity.class);
        }

        if (element == null || !(element instanceof ISecurity)) {
            return;
        }

        decoration.addPrefix("*"); //$NON-NLS-1$
        if (alertService.hasTriggeredAlerts((ISecurity) element)) {
            if (foreground == null) {
                foreground = new Color(Display.getDefault(), 255, 0, 0);
            }
            decoration.setBackgroundColor(foreground);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        alertService.removeAlertListener(this);
        context.ungetService(serviceReference);

        if (foreground != null) {
            foreground.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertListener#alertTriggered(org.eclipsetrader.core.trading.AlertEvent)
     */
    @Override
    public void alertTriggered(AlertEvent event) {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }

    protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
        final Object[] l = listeners.getListeners();
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < l.length; i++) {
                    try {
                        ((ILabelProviderListener) l[i]).labelProviderChanged(event);
                    } catch (Throwable t) {
                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error notifying listeners", t); //$NON-NLS-1$
                        Activator.log(status);
                    }
                }
            }
        });
    }
}
