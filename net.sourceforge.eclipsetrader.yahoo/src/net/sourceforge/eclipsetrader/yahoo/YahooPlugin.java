/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.yahoo;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class YahooPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.yahoo"; //$NON-NLS-1$
    public static final String PREFS_SHOW_SUBSCRIBERS_ONLY = "SHOW_SUBSCRIBERS_ONLY"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_HISTORY = "UPDATE_HISTORY"; //$NON-NLS-1$
    private static YahooPlugin plugin;

    /**
     * The constructor.
     */
    public YahooPlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        
        // Delete outdated securities lists
        String[] files = new String[] { 
        		"securities.xml", "securities_de.xml", "securities_fr.xml", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i = 0; i < files.length; i++) {
			URL url = context.getBundle().getEntry("/data/" + files[i]); //$NON-NLS-1$
			if (url != null) {
				File bundledList = new File(url.toExternalForm());
				File userList = getStateLocation().append(files[i]).toFile();
				if (bundledList.lastModified() > userList.lastModified())
					userList.delete();
			}
		}
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static YahooPlugin getDefault()
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
}
