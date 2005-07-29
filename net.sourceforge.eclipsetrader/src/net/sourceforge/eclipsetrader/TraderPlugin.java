/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sourceforge.eclipsetrader.internal.PreferenceInitializer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * <p>
 * </p>
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
  private IBackfillDataProvider backfillDataProvider;
  private static IBookDataProvider bookDataProvider;
  private Object newsProvider;
  private static HashMap pluginMap = new HashMap();

  /**
   * The constructor.
   */
  public TraderPlugin()
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.PluginResources");
    } catch (MissingResourceException x)
    {
      resourceBundle = null;
    }
  }

  // Static methods that returns application-wide objects
  public static IExtendedData[] getData()
  {
    if (plugin == null || plugin.dataStore == null)
      return null;
    IExtendedData[] dataArray = new IExtendedData[plugin.dataStore.getStockwatchData().size()];
    plugin.dataStore.getStockwatchData().toArray(dataArray);
    return dataArray;
  }

  public static IExtendedData getData(String symbol)
  {
    if (plugin == null || plugin.dataStore == null)
      return null;
    IExtendedData[] _data = getData();
    for (int i = 0; i < _data.length; i++)
    {
      if (_data[i].getSymbol().equalsIgnoreCase(symbol) == true)
        return _data[i];
    }
    return null;
  }

  public static IDataStore getDataStore()
  {
    return plugin.dataStore;
  }

  public static void setDataStore(IDataStore store)
  {
    plugin.dataStore = store;
  };

  public static IBasicDataProvider getDataProvider()
  {
    if (plugin.dataProvider == null)
    {
      // Load the data provider plugin
      plugin.dataProvider = (IBasicDataProvider) plugin.activatePlugin("net.sourceforge.eclipsetrader.dataProvider");
      if (plugin.dataProvider != null)
        plugin.dataProvider.setData(getData());
    }

    return plugin.dataProvider;
  }

  /**
   * Return the selected Book / Level II data provider.
   * <p>
   * </p>
   * 
   * @return Instance of the book data provider plugin.
   */
  public static IBookDataProvider getBookDataProvider()
  {
    if (bookDataProvider == null)
      bookDataProvider = (IBookDataProvider) plugin.activatePlugin("net.sourceforge.eclipsetrader.bookDataProvider");

    return bookDataProvider;
  }

  public static IChartDataProvider getChartDataProvider()
  {
    if (plugin.chartDataProvider == null)
    {
      // Load the chart data provider plugin
      plugin.chartDataProvider = (IChartDataProvider) plugin.activatePlugin("net.sourceforge.eclipsetrader.chartDataProvider");
    }

    return plugin.chartDataProvider;
  }

  public static IBackfillDataProvider getBackfillDataProvider()
  {
    if (plugin.backfillDataProvider == null)
    {
      // Load the chart data provider plugin
      plugin.backfillDataProvider = (IBackfillDataProvider) plugin.activatePlugin("net.sourceforge.eclipsetrader.backfillDataProvider");
    }

    return plugin.backfillDataProvider;
  }

  public static Object getNewsProvider()
  {
    if (plugin.newsProvider == null)
    {
      // Load the news provider plugin
      plugin.newsProvider = (Object) plugin.activatePlugin("net.sourceforge.eclipsetrader.newsProvider");
    }

    return plugin.newsProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.dataProvider") == true)
    {
      if (dataProvider != null)
        dataProvider.dispose();
      dataProvider = (IBasicDataProvider) activatePlugin("net.sourceforge.eclipsetrader.dataProvider");
      if (dataProvider != null)
        dataProvider.setData(getData());
    } else if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.bookDataProvider") == true)
    {
      if (bookDataProvider != null)
        bookDataProvider.dispose();
      bookDataProvider = (IBookDataProvider) activatePlugin("net.sourceforge.eclipsetrader.bookDataProvider");
    } else if (property.equalsIgnoreCase("PROXY_ENABLED") == true)
    {
      IPreferenceStore ps = getPreferenceStore();
      Properties prop = System.getProperties();
      Integer newValue = (Integer) event.getNewValue();
      if (newValue.intValue() == 0)
      {
        prop.remove("http.proxyHost");
        prop.remove("http.proxyPort");
        prop.remove("http.proxyUser");
        prop.remove("http.proxyPassword");
        prop.remove("https.proxyHost");
        prop.remove("https.proxyPort");
        prop.remove("https.proxyUser");
        prop.remove("https.proxyPassword");
        prop.remove("socksProxyHost");
        prop.remove("socksProxyPort");
        prop.remove("java.net.socks.username");
        prop.remove("java.net.socks.password");
      } 
      else
      {
        if (ps.getString("HTTP_PROXY_HOST").length() != 0)
          prop.setProperty("http.proxyHost", ps.getString("HTTP_PROXY_HOST"));
        prop.setProperty("http.proxyPort", ps.getString("HTTP_PROXY_PORT"));
        prop.setProperty("http.proxyUser", ps.getString("PROXY_USER_NAME"));
        prop.setProperty("http.proxyPassword", ps.getString("PROXY_PASSWORD"));
        if (ps.getString("HTTPS_PROXY_HOST").length() != 0)
          prop.setProperty("https.proxyHost", ps.getString("HTTPS_PROXY_HOST"));
        prop.setProperty("https.proxyPort", ps.getString("HTTPS_PROXY_PORT"));
        prop.setProperty("https.proxyUser", ps.getString("PROXY_USER_NAME"));
        prop.setProperty("https.proxyPassword", ps.getString("PROXY_PASSWORD"));
        if (ps.getString("SOCKS_PROXY_HOST").length() != 0)
          prop.setProperty("socksProxyHost", ps.getString("SOCKS_PROXY_HOST"));
        prop.setProperty("socksProxyPort", ps.getString("SOCKS_PROXY_PORT"));
        prop.setProperty("java.net.socks.username", ps.getString("PROXY_USER_NAME"));
        prop.setProperty("java.net.socks.password", ps.getString("PROXY_PASSWORD"));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    getStateLocation();
    new PreferenceInitializer().initializeDefaultPreferences();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception
  {
    if (dataProvider != null)
      dataProvider.stopStreaming();
    if (dataStore != null)
      dataStore.terminate();

    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static TraderPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = TraderPlugin.getDefault().getResourceBundle();
    try
    {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e)
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle()
  {
    return resourceBundle;
  }

  /**
   * Load the specified plugin id from the given extension point.<br>
   * 
   * @return plugin Object or null if plugin cannot be instantiated or no
   *         plugins are present.
   */
  public Object activatePlugin(String ep)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return null;

    String id = getPreferenceStore().getString(ep);
    if (id.length() == 0)
      return null;

    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    for (int m = 0; m < members.length; m++)
    {
      IConfigurationElement member = members[m];
      if (id.equalsIgnoreCase(member.getAttribute("id")))
        try
        {
          return member.createExecutableExtension("class");
        } catch (Exception x)
        {
          x.printStackTrace();
        }
      ;
    }

    // If we are here, then the configured plugin is no more available, so
    // reset the preference to avoid future problems.
    getPreferenceStore().setValue(ep, "");

    return null;
  }

  /**
   * Return the singleton instance of the given extension.
   */
  public static Object getExtensionInstance(String extensionPointId, String id)
  {
    Object obj = pluginMap.get(extensionPointId + ":" + id);
    if (obj == null)
    {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionPointId);
      if (extensionPoint == null)
        return null;

      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int m = 0; m < members.length; m++)
      {
        IConfigurationElement member = members[m];
        if (id.equalsIgnoreCase(member.getAttribute("id")))
          try
          {
            obj = member.createExecutableExtension("class");
            pluginMap.put(extensionPointId + ":" + id, obj);
          } catch (Exception x)
          {
            x.printStackTrace();
          }
        ;
      }
    }

    return obj;
  }

  /**
   * Returns the status of the data streaming.
   * 
   * @return true if data streaming is active
   */
  public static boolean isStreaming()
  {
    return getDefault().getPreferenceStore().getBoolean("net.sourceforge.eclipsetrader.streaming");
  }

  /**
   * Log a message in the system log with the default INFO severity.
   * 
   * @param message the message to log
   */
  public static void log(String message)
  {
    plugin.getLog().log(new Status(Status.INFO, "eclipsetrader", 0, message, null));
  }

  /**
   * Log a message in the system log with the default given severity status.
   * 
   * @param severity the severity; one of OK, ERROR, INFO, WARNING, or CANCEL
   * @param message the message to log
   */
  public static void log(int severity, String message)
  {
    plugin.getLog().log(new Status(severity, "eclipsetrader", 0, message, null));
  }
}
