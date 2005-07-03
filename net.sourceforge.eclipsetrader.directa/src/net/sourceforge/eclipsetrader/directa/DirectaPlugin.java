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
package net.sourceforge.eclipsetrader.directa;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.eclipsetrader.directa.internal.LoginDialog;
import net.sourceforge.eclipsetrader.directa.internal.Streamer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class DirectaPlugin extends AbstractUIPlugin
{
  private static DirectaPlugin plugin;

  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public DirectaPlugin()
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.directa.resources");
    } catch (MissingResourceException x)
    {
      resourceBundle = null;
    }
  }

  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    System.out.println(this.getClass() + ": start");

    // Default preferences
    IPreferenceStore ps = getPreferenceStore();
    ps.setDefault("transact.server", "directatrading.com");
    ps.setDefault("quote.server", "194.177.116.201");
    ps.setDefault("streaming.server", "213.92.13.59");
    ps.setDefault("backfill.server", "213.92.13.15");
    ps.setDefault("user.name", "");
    ps.setDefault("user.password", "");
    ps.setDefault("rtcharts.update", "120");
    PreferenceConverter.setDefault(ps, "trading.background_color", new RGB(222, 253, 254));
    PreferenceConverter.setDefault(ps, "trading.text_color", new RGB(0, 0, 0));
    PreferenceConverter.setDefault(ps, "trading.values_color", new RGB(192, 0, 0));
    PreferenceConverter.setDefault(ps, "orders.background_color", new RGB(255, 255, 255));
    PreferenceConverter.setDefault(ps, "orders.text_color", new RGB(0, 0, 0));
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception
  {
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static DirectaPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = DirectaPlugin.getDefault().getResourceBundle();
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

  public static boolean connectServer()
  {
    Streamer streamer = Streamer.getInstance();
    IPreferenceStore ps = DirectaPlugin.getDefault().getPreferenceStore();
    String userName = ps.getString("user.name");
    String password = ps.getString("user.password");
    if (streamer.login(userName, password) == true)
      return true;

    LoginDialog dlg = new LoginDialog();
    for (;;)
    {
      if (dlg.open() != LoginDialog.OK)
        break;
      if (streamer.login(dlg.getUserName(), dlg.getPassword()) == true)
        return true;
    }

    return false;
  }

}
