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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.sourceforge.eclipsetrader.core.internal.XMLRepository;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CorePlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.core";
    public static final String FEED_EXTENSION_POINT = PLUGIN_ID + ".feeds";
    public static final String FEED_RUNNING = "FEED_RUNNING";
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
        getPreferenceStore().setDefault(FEED_RUNNING, false);
     
        copyWorkspaceFile("charts/default.xml");
        copyWorkspaceFile("securities.xml");
        copyWorkspaceFile("watchlists.xml");
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

    private void copyWorkspaceFile(String file)
    {
        File f = new File(Platform.getLocation().toFile(), file);
        if (!f.exists())
        {
            f.getParentFile().mkdirs();
            try
            {
                byte[] buffer = new byte[10240];
                OutputStream os = new FileOutputStream(f);
                InputStream is = openStream(new Path("data/" + file));
                int readed = 0;
                do
                {
                    readed = is.read(buffer);
                    os.write(buffer, 0, readed);
                } while (readed == buffer.length);
                os.close();
                is.close();
            }
            catch (Exception e) {
                CorePlugin.logException(e);
            }
        }
    }

    public static void logException(Exception e)
    {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 0, msg, e));
        e.printStackTrace();
    }
}
