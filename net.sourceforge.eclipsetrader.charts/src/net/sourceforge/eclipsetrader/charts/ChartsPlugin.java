package net.sourceforge.eclipsetrader.charts;

import java.text.NumberFormat;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ChartsPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts";
    public static final String INDICATORS_EXTENSION_POINT = PLUGIN_ID + ".indicators";
    public static final String OBJECTS_EXTENSION_POINT = PLUGIN_ID + ".objects";
    private static ChartsPlugin plugin;
    private static NumberFormat numberFormat;
    private static NumberFormat percentageFormat;
    private static NumberFormat priceFormat;

    /**
     * The constructor.
     */
    public ChartsPlugin()
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
    public static ChartsPlugin getDefault()
    {
        return plugin;
    }

    public static NumberFormat getNumberFormat()
    {
        if (numberFormat == null)
        {
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
            numberFormat.setMinimumIntegerDigits(1);
            numberFormat.setMinimumFractionDigits(0);
            numberFormat.setMaximumFractionDigits(0);
        }
        return numberFormat;
    }

    public static NumberFormat getPercentageFormat()
    {
        if (percentageFormat == null)
        {
            percentageFormat = NumberFormat.getInstance();
            percentageFormat.setGroupingUsed(false);
            percentageFormat.setMinimumIntegerDigits(1);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setMaximumFractionDigits(2);
        }
        return percentageFormat;
    }

    public static NumberFormat getPriceFormat()
    {
        if (priceFormat == null)
        {
            priceFormat = NumberFormat.getInstance();
            priceFormat.setGroupingUsed(true);
            priceFormat.setMinimumIntegerDigits(1);
            priceFormat.setMinimumFractionDigits(4);
            priceFormat.setMaximumFractionDigits(4);
        }
        return priceFormat;
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
    
    public static IIndicatorPlugin createIndicatorPlugin(String id)
    {
        IConfigurationElement item = getIndicatorPlugin(id);
        if (item != null)
        {
            try {
                Object obj = item.createExecutableExtension("class");
                if (obj instanceof IIndicatorPlugin)
                    return (IIndicatorPlugin)obj;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    public static IConfigurationElement getIndicatorPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.INDICATORS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item;
            }
        }
        
        return null;
    }
    
    public static ObjectPlugin createObjectPlugin(String id)
    {
        IConfigurationElement item = getObjectPlugin(id);
        if (item != null)
        {
            try {
                Object obj = item.createExecutableExtension("class");
                if (obj instanceof ObjectPlugin)
                    return (ObjectPlugin)obj;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    public static IConfigurationElement getObjectPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.OBJECTS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item;
            }
        }
        
        return null;
    }
}
