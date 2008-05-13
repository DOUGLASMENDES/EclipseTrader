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

import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.news.core.INewsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipsetrader.news"; //$NON-NLS-1$

	public static final String PROVIDER_EXTENSION_POINT = "org.eclipsetrader.news.providers"; //$NON-NLS-1$

	public static final String PREFS_UPDATE_INTERVAL = "UPDATE_INTERVAL"; //$NON-NLS-1$
	public static final String PREFS_DATE_RANGE = "DATE_RANGE"; //$NON-NLS-1$
	public static final String PREFS_UPDATE_ON_STARTUP = "UPDATE_ON_STARTUP"; //$NON-NLS-1$
	public static final String PREFS_FOLLOW_QUOTE_FEED = "FOLLOW_QUOTE_FEED"; //$NON-NLS-1$

	// The shared instance
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

		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		context.getService(serviceReference);
		context.ungetService(serviceReference);

		NewsService service = new NewsService();
		context.registerService(new String[] { INewsService.class.getName(), NewsService.class.getName() }, service, new Hashtable<Object,Object>());
		service.startUp(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		ServiceReference serviceReference = context.getServiceReference(NewsService.class.getName());
		if (serviceReference != null) {
			NewsService service = (NewsService) context.getService(serviceReference);
			if (service != null)
				service.shutDown(null);
			context.ungetService(serviceReference);
		}

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		if (plugin != null)
			plugin.getLog().log(status);
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
	    reg.put("readed_ovr", ImageDescriptor.createFromURL(getBundle().getResource("icons/ovr16/readed_ovr.gif")));
	    reg.put("unreaded_ovr", ImageDescriptor.createFromURL(getBundle().getResource("icons/ovr16/unreaded_ovr.gif")));
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
