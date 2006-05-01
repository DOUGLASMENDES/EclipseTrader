/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core;

import net.sourceforge.eclipsetrader.core.internal.XMLRepository;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CorePlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.core";
    public static final String FEED_EXTENSION_POINT = PLUGIN_ID + ".feeds";
    public static final String FEED_RUNNING = "FEED_RUNNING";
    public static final String PREFS_ENABLE_HTTP_PROXY = "ENABLE_HTTP_PROXY";
    public static final String PREFS_PROXY_HOST_ADDRESS = "PROXY_HOST_ADDRESS";
    public static final String PREFS_PROXY_PORT_ADDRESS = "PROXY_PORT_ADDRESS";
    public static final String PREFS_ENABLE_PROXY_AUTHENTICATION = "ENABLE_PROXY_AUTHENTICATION";
    public static final String PREFS_PROXY_USER = "PROXY_USER";
    public static final String PREFS_PROXY_PASSWORD = "PROXY_PASSWORD";
    public static final String PREFS_HISTORICAL_PRICE_RANGE = "HISTORICAL_PRICE_RANGE";
    public static final String PREFS_NEWS_DATE_RANGE = "NEWS_DATE_RANGE";
    private static CorePlugin plugin;
    private static Repository repository;

    public CorePlugin()
    {
        plugin = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        
        IPreferenceStore preferenceStore = getPreferenceStore();
        preferenceStore.setDefault(FEED_RUNNING, false);
        if (preferenceStore.getDefaultInt(PREFS_HISTORICAL_PRICE_RANGE) == 0)
            preferenceStore.setDefault(PREFS_HISTORICAL_PRICE_RANGE, 5);
        if (preferenceStore.getDefaultInt(PREFS_NEWS_DATE_RANGE) == 0)
            preferenceStore.setDefault(PREFS_NEWS_DATE_RANGE, 3);

        preferenceStore.setValue(FEED_RUNNING, false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
        FeedMonitor.stop();
        Level2FeedMonitor.stop();
        if (repository != null)
            repository.dispose();

        getPreferenceStore().setValue(FEED_RUNNING, false);
        
        super.stop(context);
        
        plugin = null;
    }

    public static CorePlugin getDefault()
    {
        return plugin;
    }

    public static Repository getRepository()
    {
        if (repository == null)
            repository = new XMLRepository();
        return repository;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    public static IHistoryFeed createHistoryFeedPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                {
                    members = item.getChildren();
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        if (members[ii].getName().equals("history"))
                            try {
                                Object obj = members[ii].createExecutableExtension("class");
                                return (IHistoryFeed)obj;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                    }
                    break;
                }
            }
        }
        
        return null;
    }
    
    public static IFeed createQuoteFeedPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                {
                    members = item.getChildren();
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        if (members[ii].getName().equals("quote"))
                            try {
                                Object obj = members[ii].createExecutableExtension("class");
                                return (IFeed)obj;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                    }
                    break;
                }
            }
        }
        
        return null;
    }
    
    public static ILevel2Feed createLevel2FeedPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                {
                    members = item.getChildren();
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        if (members[ii].getName().equals("level2"))
                            try {
                                Object obj = members[ii].createExecutableExtension("class");
                                return (ILevel2Feed)obj;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                    }
                    break;
                }
            }
        }
        
        return null;
    }

    public static void logException(Exception e)
    {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 0, msg, e));
        e.printStackTrace();
    }
}
