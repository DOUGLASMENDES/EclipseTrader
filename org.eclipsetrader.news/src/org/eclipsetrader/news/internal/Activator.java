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

import java.net.URI;
import java.util.Hashtable;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.news.core.INewsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipsetrader.news"; //$NON-NLS-1$

    public static final String PROVIDER_EXTENSION_POINT = "org.eclipsetrader.news.providers"; //$NON-NLS-1$

    public static final String PREFS_UPDATE_INTERVAL = "UPDATE_INTERVAL"; //$NON-NLS-1$
    public static final String PREFS_DATE_RANGE = "DATE_RANGE"; //$NON-NLS-1$
    public static final String PREFS_FOLLOW_QUOTE_FEED = "FOLLOW_QUOTE_FEED"; //$NON-NLS-1$
    public static final String PREFS_ENABLE_DECORATORS = "ENABLE_DECORATORS"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private NewsServiceFactory newsServiceFactory;
    private ServiceRegistration newsServiceRegistration;

    private IRepositoryService repositoryService;
    private ServiceReference repositoryServiceReference;

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

        newsServiceFactory = new NewsServiceFactory();
        newsServiceRegistration = context.registerService(new String[] {
                INewsService.class.getName(), NewsService.class.getName()
        }, newsServiceFactory, new Hashtable<String, Object>());

        repositoryServiceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (repositoryServiceReference != null) {
            repositoryService = (IRepositoryService) context.getService(repositoryServiceReference);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (newsServiceFactory != null && newsServiceFactory.getServiceInstance() != null) {
            newsServiceFactory.getServiceInstance().shutDown(null);
        }
        if (newsServiceRegistration != null) {
            newsServiceRegistration.unregister();
        }

        if (repositoryServiceReference != null) {
            context.ungetService(repositoryServiceReference);
            repositoryService = null;
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
        if (plugin != null) {
            plugin.getLog().log(status);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put("readed_ovr", ImageDescriptor.createFromURL(getBundle().getResource("icons/ovr16/readed_ovr.gif")));
        reg.put("unreaded_ovr", ImageDescriptor.createFromURL(getBundle().getResource("icons/ovr16/unreaded_ovr.gif")));

        reg.put("normal_icon", ImageDescriptor.createFromURL(getBundle().getResource("icons/eview16/headlines.png")));
        reg.put("new_headlines_icon", ImageDescriptor.createFromURL(getBundle().getResource("icons/eview16/headlines_unread.png")));
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

    public IRepositoryService getRepositoryService() {
        return repositoryService;
    }

    public IDialogSettings getDialogSettingsForView(URI uri) {
        String uriString = uri.toString();

        IDialogSettings rootSettings = getDialogSettings().getSection("Views");
        if (rootSettings == null) {
            rootSettings = getDialogSettings().addNewSection("Views");
        }

        IDialogSettings[] sections = rootSettings.getSections();
        for (int i = 0; i < sections.length; i++) {
            if (uriString.equals(sections[i].get("uri"))) {
                return sections[i];
            }
        }

        String uuid = UUID.randomUUID().toString();
        IDialogSettings dialogSettings = rootSettings.addNewSection(uuid);
        dialogSettings.put("uri", uriString);

        return dialogSettings;
    }
}
