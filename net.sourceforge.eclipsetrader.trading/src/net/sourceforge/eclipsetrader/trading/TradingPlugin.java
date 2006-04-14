package net.sourceforge.eclipsetrader.trading;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TradingPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.trading";
    private static TradingPlugin plugin;

    /**
     * The constructor.
     */
    public TradingPlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
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
    public static TradingPlugin getDefault()
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

    public static void logException(Exception e)
    {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 0, msg, e));
        e.printStackTrace();
    }
}
