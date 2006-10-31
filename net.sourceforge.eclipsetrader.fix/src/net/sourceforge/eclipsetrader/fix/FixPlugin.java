package net.sourceforge.eclipsetrader.fix;

import net.sourceforge.eclipsetrader.fix.core.ExecutorStartup;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class FixPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.fix";
    public static final String PREFS_ENABLE_EXECUTOR = "ENABLE_EXECUTOR";
    private static FixPlugin plugin;
    private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(PREFS_ENABLE_EXECUTOR))
            {
                boolean enable = ((Boolean)event.getNewValue()).booleanValue();
                if (enable)
                    ExecutorStartup.getInstance().start();
                else
                    ExecutorStartup.getInstance().stop();
            }
        }
    };

    public FixPlugin()
    {
        plugin = this;
    }

    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
    }

    public void stop(BundleContext context) throws Exception
    {
        getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
        ExecutorStartup.getInstance().stop();
        
        super.stop(context);
        plugin = null;
    }

    public static FixPlugin getDefault()
    {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
