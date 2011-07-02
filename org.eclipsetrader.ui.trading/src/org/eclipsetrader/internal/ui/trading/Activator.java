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

package org.eclipsetrader.internal.ui.trading;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipsetrader.ui.trading"; //$NON-NLS-1$

    public static final String ALERT_NOTIFICATION_IMAGE = "alert_notification_image"; //$NON-NLS-1$
    public static final String ALERT_ADD_IMAGE = "alert_add_image"; //$NON-NLS-1$
    public static final String ALERT_DELETE_IMAGE = "alert_delete_image"; //$NON-NLS-1$
    public static final String ALERT_WIZARD_IMAGE = "alert_wizard_image"; //$NON-NLS-1$

    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static void log(IStatus status) {
        if (plugin == null) {
            if (status.getException() != null) {
                status.getException().printStackTrace();
            }
            return;
        }
        plugin.getLog().log(status);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(ALERT_NOTIFICATION_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/eview16/bell.png"))); //$NON-NLS-1$

        reg.put(ALERT_WIZARD_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/wizban/newfile_wiz.gif"))); //$NON-NLS-1$
        reg.put(ALERT_ADD_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/bell_add.png"))); //$NON-NLS-1$
        reg.put(ALERT_DELETE_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/delete.gif"))); //$NON-NLS-1$
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
