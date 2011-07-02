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

package org.eclipsetrader.directa.internal.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.directa.internal.Activator;

public class ImageResource {

    public static final String IMG_CLCL_NAV_BACKWARD = "IMG_CLCL_NAV_BACKWARD"; //$NON-NLS-1$
    public static final String IMG_CLCL_NAV_FORWARD = "IMG_CLCL_NAV_FORWARD"; //$NON-NLS-1$
    public static final String IMG_CLCL_NAV_STOP = "IMG_CLCL_NAV_STOP"; //$NON-NLS-1$
    public static final String IMG_CLCL_NAV_REFRESH = "IMG_CLCL_NAV_REFRESH"; //$NON-NLS-1$
    public static final String IMG_CLCL_NAV_GO = "IMG_CLCL_NAV_GO"; //$NON-NLS-1$
    public static final String IMG_CLCL_NAV_HOME = "IMG_CLCL_NAV_HOME"; //$NON-NLS-1$
    public static final String IMG_CLCL_NAV_PRINT = "IMG_CLCL_NAV_PRINT"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_BACKWARD = "IMG_ELCL_NAV_BACKWARD"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_FORWARD = "IMG_ELCL_NAV_FORWARD"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_STOP = "IMG_ELCL_NAV_STOP"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_REFRESH = "IMG_ELCL_NAV_REFRESH"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_GO = "IMG_ELCL_NAV_GO"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_HOME = "IMG_ELCL_NAV_HOME"; //$NON-NLS-1$
    public static final String IMG_ELCL_NAV_PRINT = "IMG_ELCL_NAV_PRINT"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_BACKWARD = "IMG_DLCL_NAV_BACKWARD"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_FORWARD = "IMG_DLCL_NAV_FORWARD"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_STOP = "IMG_DLCL_NAV_STOP"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_REFRESH = "IMG_DLCL_NAV_REFRESH"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_GO = "IMG_DLCL_NAV_GO"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_HOME = "IMG_DLCL_NAV_HOME"; //$NON-NLS-1$
    public static final String IMG_DLCL_NAV_PRINT = "IMG_DLCL_NAV_PRINT"; //$NON-NLS-1$
    private static ImageRegistry imageRegistry;
    private static Map<String, ImageDescriptor> imageDescriptors;
    private static Image[] busyImages;
    private static IPath ICON_BASE_URL = new Path("$nl$/icons/"); //$NON-NLS-1$
    private static final String URL_CLCL = "clcl16/"; //$NON-NLS-1$
    private static final String URL_ELCL = "elcl16/"; //$NON-NLS-1$
    private static final String URL_DLCL = "dlcl16/"; //$NON-NLS-1$
    private static final String URL_OBJ = "obj16/"; //$NON-NLS-1$

    /**
     * Cannot construct an ImageResource. Use static methods only.
     */
    private ImageResource() {
    }

    /**
     * Returns the busy images for the Web browser.
     *
     * @return org.eclipse.swt.graphics.Image[]
     */
    public static Image[] getBusyImages() {
        if (imageRegistry == null) {
            initializeImageRegistry();
        }
        return busyImages;
    }

    /**
     * Return the image with the given key.
     *
     * @param key java.lang.String
     * @return org.eclipse.swt.graphics.Image
     */
    public static Image getImage(String key) {
        if (imageRegistry == null) {
            initializeImageRegistry();
        }
        return imageRegistry.get(key);
    }

    /**
     * Return the image descriptor with the given key.
     *
     * @param key java.lang.String
     * @return org.eclipse.jface.resource.ImageDescriptor
     */
    public static ImageDescriptor getImageDescriptor(String key) {
        if (imageRegistry == null) {
            initializeImageRegistry();
        }
        return imageDescriptors.get(key);
    }

    /**
     * Initialize the image resources.
     */
    protected static void initializeImageRegistry() {
        imageRegistry = new ImageRegistry();
        imageDescriptors = new HashMap<String, ImageDescriptor>();

        // load Web browser images
        registerImage(IMG_ELCL_NAV_BACKWARD, URL_ELCL + "nav_backward.gif"); //$NON-NLS-1$
        registerImage(IMG_ELCL_NAV_FORWARD, URL_ELCL + "nav_forward.gif"); //$NON-NLS-1$
        registerImage(IMG_ELCL_NAV_STOP, URL_ELCL + "nav_stop.gif"); //$NON-NLS-1$
        registerImage(IMG_ELCL_NAV_REFRESH, URL_ELCL + "nav_refresh.gif"); //$NON-NLS-1$
        registerImage(IMG_ELCL_NAV_GO, URL_ELCL + "nav_go.gif"); //$NON-NLS-1$
        registerImage(IMG_ELCL_NAV_HOME, URL_ELCL + "nav_home.gif"); //$NON-NLS-1$
        registerImage(IMG_ELCL_NAV_PRINT, URL_ELCL + "nav_print.gif"); //$NON-NLS-1$

        registerImage(IMG_CLCL_NAV_BACKWARD, URL_CLCL + "nav_backward.gif"); //$NON-NLS-1$
        registerImage(IMG_CLCL_NAV_FORWARD, URL_CLCL + "nav_forward.gif"); //$NON-NLS-1$
        registerImage(IMG_CLCL_NAV_STOP, URL_CLCL + "nav_stop.gif"); //$NON-NLS-1$
        registerImage(IMG_CLCL_NAV_REFRESH, URL_CLCL + "nav_refresh.gif"); //$NON-NLS-1$
        registerImage(IMG_CLCL_NAV_GO, URL_CLCL + "nav_go.gif"); //$NON-NLS-1$
        registerImage(IMG_CLCL_NAV_HOME, URL_CLCL + "nav_home.gif"); //$NON-NLS-1$
        registerImage(IMG_CLCL_NAV_PRINT, URL_CLCL + "nav_print.gif"); //$NON-NLS-1$

        registerImage(IMG_DLCL_NAV_BACKWARD, URL_DLCL + "nav_backward.gif"); //$NON-NLS-1$
        registerImage(IMG_DLCL_NAV_FORWARD, URL_DLCL + "nav_forward.gif"); //$NON-NLS-1$
        registerImage(IMG_DLCL_NAV_STOP, URL_DLCL + "nav_stop.gif"); //$NON-NLS-1$
        registerImage(IMG_DLCL_NAV_REFRESH, URL_DLCL + "nav_refresh.gif"); //$NON-NLS-1$
        registerImage(IMG_DLCL_NAV_GO, URL_DLCL + "nav_go.gif"); //$NON-NLS-1$
        registerImage(IMG_DLCL_NAV_HOME, URL_DLCL + "nav_home.gif"); //$NON-NLS-1$
        registerImage(IMG_DLCL_NAV_PRINT, URL_DLCL + "nav_print.gif"); //$NON-NLS-1$

        // busy images
        busyImages = new Image[13];
        for (int i = 0; i < 13; i++) {
            registerImage("busy" + i, URL_OBJ + "busy/" + (i + 1) + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            busyImages[i] = getImage("busy" + i); //$NON-NLS-1$
        }
    }

    /**
     * Register an image with the registry.
     *
     * @param key java.lang.String
     * @param partialURL java.lang.String
     */
    private static void registerImage(String key, String partialURL) {
        try {
            URL iconURL = FileLocator.find(Activator.getDefault().getBundle(), ICON_BASE_URL.append(partialURL), null);
            ImageDescriptor id = ImageDescriptor.createFromURL(iconURL);
            imageRegistry.put(key, id);
            imageDescriptors.put(key, id);
        } catch (Exception e) {
            // TODO Activator.logException(e);
        }
    }
}
