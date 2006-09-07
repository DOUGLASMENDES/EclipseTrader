package net.sourceforge.eclipsetrader.trading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TradingPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.trading";
    public static final String ALERTS_EXTENSION_POINT = PLUGIN_ID + ".alerts";
    public static final String SYSTEMS_EXTENSION_POINT = PLUGIN_ID + ".systems";
    public static final String SYSTEM_WIZARDS_EXTENSION_POINT = PLUGIN_ID + ".systemWizard";
    private static TradingPlugin plugin;
    private DataCollector dataCollector;
    private IPropertyChangeListener feedPropertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(CorePlugin.FEED_RUNNING))
            {
                if (((Boolean)event.getNewValue()).booleanValue())
                {
                    if (dataCollector == null)
                        dataCollector = new DataCollector();
                }
                else
                {
                    if (dataCollector != null)
                        dataCollector.dispose();
                    dataCollector = null;
                }
            }
        }
    };

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
        CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(feedPropertyListener);
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
    
    public static AlertPlugin createAlertPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ALERTS_EXTENSION_POINT);
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
                        return (AlertPlugin)obj;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return null;
    }
    
    public static String getAlertPluginName(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ALERTS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item.getAttribute("name");
            }
        }
        
        return null;
    }
    
    public static IConfigurationElement[] getAlertPluginPreferencePages(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ALERTS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item.getChildren("preferencePage");
            }
        }
        
        return new IConfigurationElement[0];
    }
    
    public static List getTradingSystemPlugins()
    {
        List list = new ArrayList();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(SYSTEMS_EXTENSION_POINT);
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
    
    public static TradingSystemPlugin createTradingSystemPlugin(TradingSystem system)
    {
        TradingSystemPlugin plugin = (TradingSystemPlugin) system.getData();
        if (plugin == null)
        {
            plugin = TradingPlugin.createTradingSystemPlugin(system.getPluginId());
            system.setData(plugin);
        }
        
        plugin.setAccount(system.getAccount());
        plugin.setSecurity(system.getSecurity());
        plugin.setMaxExposure(system.getMaxExposure());
        plugin.setMinAmount(system.getMinAmount());
        plugin.setMaxAmount(system.getMaxAmount());
        
        plugin.setParameters(system.getParameters());
        
        return plugin;
    }
    
    public static TradingSystemPlugin createTradingSystemPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(SYSTEMS_EXTENSION_POINT);
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
                        return (TradingSystemPlugin)obj;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return null;
    }
    
    public static IConfigurationElement[] getTradingSystemPluginPreferencePages(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(SYSTEMS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item.getChildren("preferencePage");
            }
        }
        
        return new IConfigurationElement[0];
    }
    
    public static void logException(Exception e)
    {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 0, msg, e));
        e.printStackTrace();
    }
}
