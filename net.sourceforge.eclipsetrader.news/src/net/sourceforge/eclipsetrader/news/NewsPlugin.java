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

package net.sourceforge.eclipsetrader.news;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.INewsProvider;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.views.WebBrowser;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class NewsPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.news"; //$NON-NLS-1$
    public static final String PROVIDER_EXTENSION_POINT = CorePlugin.PLUGIN_ID + ".news"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_ON_STARTUP = "UPDATE_ON_STARTUP"; //$NON-NLS-1$
    public static final String PREFS_FOLLOW_QUOTE_FEED = "FOLLOW_QUOTE_FEED"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_INTERVAL = "UPDATE_INTERVAL"; //$NON-NLS-1$
    public static final String FEED_RUNNING = "NEWS_FEED_RUNNING"; //$NON-NLS-1$
    private static NewsPlugin plugin;
    private IPropertyChangeListener feedPropertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(CorePlugin.FEED_RUNNING))
            {
                if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING))
                    startFeed();
                else
                    stopFeed();
            }
            else if (event.getProperty().equals(PREFS_FOLLOW_QUOTE_FEED))
            {
                if (getPreferenceStore().getBoolean(PREFS_FOLLOW_QUOTE_FEED))
                    CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(feedPropertyListener);
                else
                    CorePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(feedPropertyListener);
            }
        }
    };

    /**
     * The constructor.
     */
    public NewsPlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(PREFS_UPDATE_INTERVAL, 15);

        getPreferenceStore().setValue(NewsPlugin.FEED_RUNNING, false);
        if (store.getBoolean(PREFS_UPDATE_ON_STARTUP))
            startFeed();
        if (store.getBoolean(PREFS_FOLLOW_QUOTE_FEED))
            CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(feedPropertyListener);
        store.addPropertyChangeListener(feedPropertyListener);

        CorePlugin.getDefault().getPreferenceStore().setValue(WebBrowser.VIEW_ID + ":" + PLUGIN_ID, Messages.NewsPlugin_BrowserTitle); //$NON-NLS-1$
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        CorePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(feedPropertyListener);
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static NewsPlugin getDefault()
    {
        return plugin;
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
    
    public void startFeed()
    {
        IPreferenceStore store = getPreferenceStore();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(NewsPlugin.PROVIDER_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++)
            {
                String id = elements[i].getAttribute("id"); //$NON-NLS-1$
                if (store.getBoolean(id))
                {
                    try {
                        INewsProvider provider = (INewsProvider) elements[i].createExecutableExtension("class"); //$NON-NLS-1$
                        provider.start();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        store.setValue(NewsPlugin.FEED_RUNNING, true);
    }
    
    public void startFeedSnapshot()
    {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(NewsPlugin.FEED_RUNNING, true);

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(NewsPlugin.PROVIDER_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++)
            {
                String id = elements[i].getAttribute("id"); //$NON-NLS-1$
                if (store.getBoolean(id))
                {
                    try {
                        INewsProvider provider = (INewsProvider) elements[i].createExecutableExtension("class"); //$NON-NLS-1$
                        provider.snapshot();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        store.setValue(NewsPlugin.FEED_RUNNING, false);
    }
    
    public void startFeedSnapshot(Security security)
    {
        IPreferenceStore store = getPreferenceStore();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(NewsPlugin.PROVIDER_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++)
            {
                String id = elements[i].getAttribute("id"); //$NON-NLS-1$
                if (store.getBoolean(id))
                {
                    try {
                        INewsProvider provider = (INewsProvider) elements[i].createExecutableExtension("class"); //$NON-NLS-1$
                        provider.snapshot(security);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        store.setValue(NewsPlugin.FEED_RUNNING, true);
    }
    
    public void stopFeed()
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(NewsPlugin.PROVIDER_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++)
            {
                try {
                    INewsProvider provider = (INewsProvider) elements[i].createExecutableExtension("class"); //$NON-NLS-1$
                    provider.stop();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        getPreferenceStore().setValue(NewsPlugin.FEED_RUNNING, false);
    }
}
