/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Stephen Bate     - Dukascopy plugin
 *******************************************************************************/
package net.sourceforge.eclipsetrader.dukascopy;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class DukascopyPlugin extends AbstractUIPlugin
{
  public static final String PLUGIN_ID = DukascopyPlugin.class.getPackage().getName();

  //The shared instance.
  private static DukascopyPlugin plugin;

  //Resource bundle.
  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public DukascopyPlugin()
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.dukascopy.DukascopyPluginResources"); //$NON-NLS-1$
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
  public static DukascopyPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = DukascopyPlugin.getDefault().getResourceBundle();
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

  public void log(String message)
  {
    log(Status.INFO, message);
  }

  private void log(int severity, String message)
  {
    // I'm also printing this to the standard output to be consistent
    // with other EclipseTrader plugins.
    System.out.println(message);
    getLog().log(new Status(severity, DukascopyPlugin.PLUGIN_ID, 0, message, null));
  }

  public void log(String message, Throwable exception)
  {
    // I'm also printing this to the standard output to be consistent
    // with other EclipseTrader plugins.
    System.err.println(message);
    exception.printStackTrace();
    getLog().log(new Status(Status.ERROR, DukascopyPlugin.PLUGIN_ID, 0, message, exception));
  }
}