package net.sourceforge.eclipsetrader.archipelago;

import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class ArchipelagoPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ArchipelagoPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
  private static boolean loggedIn = false;
	
	/**
	 * The constructor.
	 */
	public ArchipelagoPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.archipelago.ArchipelagoPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static ArchipelagoPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ArchipelagoPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

  /**
   * Method to return the loggedIn field.<br>
   *
   * @return Returns the loggedIn.
   */
  public static boolean isLoggedIn()
  {
    return loggedIn;
  }
  /**
   * Method to set the loggedIn field.<br>
   * 
   * @param loggedIn The loggedIn to set.
   */
  public static void setLoggedIn(boolean value)
  {
    loggedIn = value;
  }
}
