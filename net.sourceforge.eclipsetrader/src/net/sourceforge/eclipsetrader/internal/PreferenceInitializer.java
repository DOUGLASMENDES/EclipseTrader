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
package net.sourceforge.eclipsetrader.internal;

import java.util.Properties;

import net.sourceforge.eclipsetrader.IDataStore;
import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initialize the default preferences for the core plugin.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

  /* (non-Javadoc)
   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  public void initializeDefaultPreferences()
  {
    // Set the default preferences
    IPreferenceStore ps = TraderPlugin.getDefault().getPreferenceStore();
    ps.setDefault("net.sourceforge.eclipsetrader.dataStore", "net.sourceforge.eclipsetrader.xml_data_store");
    ps.setDefault("net.sourceforge.eclipsetrader.dataProvider", IPreferenceStore.STRING_DEFAULT_DEFAULT);
    ps.setDefault("net.sourceforge.eclipsetrader.promptOnExit", true);
    
    // Timing 
    ps.setDefault("net.sourceforge.eclipsetrader.timing.session1", true);
    ps.setDefault("net.sourceforge.eclipsetrader.timing.startTime1", 9 * 60 + 5);
    ps.setDefault("net.sourceforge.eclipsetrader.timing.stopTime1", 17 * 60 + 25);
    ps.setDefault("net.sourceforge.eclipsetrader.timing.session2", true);
    ps.setDefault("net.sourceforge.eclipsetrader.timing.startTime2", 18 * 60 + 0);
    ps.setDefault("net.sourceforge.eclipsetrader.timing.stopTime2", 20 * 60 + 30);
    ps.setDefault("net.sourceforge.eclipsetrader.rtchart.period", 2);

    // Sets the one and only avalable plugin as selected
    if (ps.getString("net.sourceforge.eclipsetrader.dataProvider").length() == 0)
    {
      if (getPluginCount("net.sourceforge.eclipsetrader.dataProvider") == 1)
        ps.setValue("net.sourceforge.eclipsetrader.dataProvider", getFirstPlugin("net.sourceforge.eclipsetrader.dataProvider"));
    }
    
    // Sets the proxy preferences
    if (ps.getInt("PROXY_ENABLED") == 1)
    {
      Properties prop = System.getProperties();
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

    // Listen for changes on the plugin's preferences
    ps.addPropertyChangeListener(TraderPlugin.getDefault());
    
    // Load the data store plugin and reads the stored data
    IDataStore dataStore = (IDataStore)TraderPlugin.getDefault().activatePlugin("net.sourceforge.eclipsetrader.dataStore");
    if (dataStore != null)
      dataStore.load();
    TraderPlugin.setDataStore(dataStore);
  }

  private int getPluginCount(String ep)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return 0;
    
    return extensionPoint.getConfigurationElements().length;
  }

  private String getFirstPlugin(String ep)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return "";
    
    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    if (members.length == 0)
      return "";
    IConfigurationElement member = members[0];
    return member.getAttribute("id");
  }

}
