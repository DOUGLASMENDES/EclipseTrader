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
package net.sourceforge.eclipsetrader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseTraderPlugin extends AbstractUIPlugin
{
    public static final String PROMPT_ON_EXIT = "PROMPT_ON_EXIT";
    private static EclipseTraderPlugin plugin;

    /**
     * The constructor.
     */
    public EclipseTraderPlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        getPreferenceStore().setDefault(PROMPT_ON_EXIT, true);
        copyWorkspaceFile("defaultChart.xml");
        copyWorkspaceFile("currencies.xml");
        copyWorkspaceFile("securities.xml");
        copyWorkspaceFile("watchlists.xml");
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
    public static EclipseTraderPlugin getDefault()
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
        return AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipsetrader", path);
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
                InputStream is = FileLocator.openStream(getBundle(), new Path("data/" + file), false);
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
}
