/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * <p></p>
 * 
 * @since 1.0
 */
public class TraderPlugin extends AbstractUIPlugin implements IPropertyChangeListener 
{
	private static TraderPlugin plugin;
	private ResourceBundle resourceBundle;
  private IDataStore dataStore;
  private IBasicDataProvider dataProvider;
  private IChartDataProvider chartDataProvider;
	
	/**
	 * The constructor.
	 */
	public TraderPlugin() 
  {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.PluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

  // Static methods that returns application-wide objects
  public static IExtendedData[] getData() { return plugin.dataStore.getData(); }
  public static IDataStore getDataStore() { return plugin.dataStore; }

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception 
  {
    super.start(context);

    // Set the default preferences
    IPreferenceStore ps = getPreferenceStore();
    ps.setDefault("net.sourceforge.eclipsetrader.dataStore", "net.sourceforge.eclipsetrader.xml_data_store");
    ps.setDefault("net.sourceforge.eclipsetrader.dataProvider", "");
    ps.setDefault("net.sourceforge.eclipsetrader.promptOnExit", true);

    // Sets the one and only avalable plugin as selected
    if (ps.getString("net.sourceforge.eclipsetrader.dataProvider").length() == 0)
    {
      if (getPluginCount("net.sourceforge.eclipsetrader.dataProvider") == 1)
        ps.setValue("net.sourceforge.eclipsetrader.dataProvider", getFirstPlugin("net.sourceforge.eclipsetrader.dataProvider"));
    }

    // Listen for changes on the plugin's preferences
    getPreferenceStore().addPropertyChangeListener(this);
    
    // Load the data store plugin and reads the stored data
    dataStore = (IDataStore)activatePlugin("net.sourceforge.eclipsetrader.dataStore");
    if (dataStore != null)
      dataStore.load();
	}

  public static IBasicDataProvider getDataProvider()
  {
    if (plugin.dataProvider == null)
    {
      // Load the data provider plugin
      plugin.dataProvider = (IBasicDataProvider)plugin.activatePlugin("net.sourceforge.eclipsetrader.dataProvider");
      if (plugin.dataProvider != null)
        plugin.dataProvider.setData(plugin.dataStore.getData());
    }
    
    return plugin.dataProvider;
  }

  public static IChartDataProvider getChartDataProvider() 
  { 
    if (plugin.chartDataProvider == null)
    {
      // Load the chart data provider plugin
      plugin.chartDataProvider = (IChartDataProvider)plugin.activatePlugin("net.sourceforge.eclipsetrader.chartDataProvider");
    }

    return plugin.chartDataProvider; 
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.dataProvider") == true)
    {
      if (dataProvider != null)
        dataProvider.dispose();
      dataProvider = (IBasicDataProvider)activatePlugin("net.sourceforge.eclipsetrader.dataProvider");
      if (dataProvider != null)
        dataProvider.setData(dataStore.getData());
    }
  }

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception 
  {
    if (dataProvider != null)
      dataProvider.stopStreaming();
    if (dataStore != null)
      dataStore.store();

    super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static TraderPlugin getDefault() { return plugin; }

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) 
  {
		ResourceBundle bundle = TraderPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() { return resourceBundle; }

  /**
   * Load the specified plugin id from the given extension point.<br>
   * 
   * @return plugin Object or null if plugin cannot be instantiated or no
   * plugins are present.
   */
  public Object activatePlugin(String ep)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return null;
    
    String id = getPreferenceStore().getString(ep);
    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    for (int m = 0; m < members.length; m++)
    {
      IConfigurationElement member = members[m];
      IExtension extension = member.getDeclaringExtension();
      if (id.equalsIgnoreCase(member.getAttribute("id")))
        try {
          return member.createExecutableExtension("class");
        } catch(Exception x) { x.printStackTrace(); };
    }
    
    // If we are here, then the configured plugin is no more available, so
    // reset the preference to avoid future problems.
    getPreferenceStore().setValue(ep, "");
    
    return null;
  }

  public int getPluginCount(String ep)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return 0;
    
    return extensionPoint.getConfigurationElements().length;
  }

  public String getFirstPlugin(String ep)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return "";
    
    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    if (members.length == 0)
      return "";
    IConfigurationElement member = members[0];
    IExtension extension = member.getDeclaringExtension();
    return member.getAttribute("id");
  }
}
