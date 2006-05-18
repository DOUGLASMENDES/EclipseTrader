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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.internal.XMLRepository;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CorePlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.core";
    public static final String FEED_EXTENSION_POINT = PLUGIN_ID + ".feeds";
    public static final String PATTERN_EXTENSION_POINT = PLUGIN_ID + ".patterns";
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
    private IPropertyChangeListener feedPropertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(CorePlugin.FEED_RUNNING))
            {
                if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING))
                {
                    Job job = new Job("Update currencies") {
                        protected IStatus run(IProgressMonitor monitor)
                        {
                            return CurrencyConverter.getInstance().updateExchanges(monitor);
                        }
                    };
                    job.setUser(false);
                    job.schedule();
                }
            }
        }
    };

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
        CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(feedPropertyListener);
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
        CorePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(feedPropertyListener);
        
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
    
    public static List getAllPatternPlugins()
    {
        List list = new ArrayList();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(PATTERN_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
                list.add(members[i]);
        }
        
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                String s0 = ((IConfigurationElement) arg0).getAttribute("name");
                String s1 = ((IConfigurationElement) arg1).getAttribute("name");
                return s0.compareTo(s1);
            }
        });
        
        return list;
    }
    
    public static IPattern createPatternPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(PATTERN_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                {
                    try {
                        Object obj = members[i].createExecutableExtension("class");
                        return (IPattern)obj;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
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
