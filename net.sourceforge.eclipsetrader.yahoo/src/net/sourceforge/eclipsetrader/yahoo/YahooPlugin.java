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
package net.sourceforge.eclipsetrader.yahoo;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.eclipsetrader.yahoo.internal.SymbolMapper;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class YahooPlugin extends AbstractUIPlugin implements IPropertyChangeListener 
{
	private static YahooPlugin plugin;
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public YahooPlugin() 
  {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.yahoo.YahooPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception 
  {
		super.start(context);

    // Sets the default preferences
    IPreferenceStore ps = getPreferenceStore();
    ps.setDefault("yahoo.refresh", "15");
    ps.setDefault("yahoo.url", "http://finance.yahoo.com/d/quotes.csv");
    ps.setDefault("yahoo.charts.url", "http://table.finance.yahoo.com/table.csv");
    ps.setDefault("yahoo.mapping", false);
    ps.setDefault("yahoo.suffix", "");
    ps.setDefault("net.sourceforge.eclipsetrader.yahoo.newsSource", "yahoo.news.us");
    ps.setDefault("NEW_CHART_YEARS", "1");
    ps.addPropertyChangeListener(this);
    
    SymbolMapper.setDoMapping(ps.getBoolean("yahoo.mapping"));
    SymbolMapper.setDefaultSuffix(ps.getString("yahoo.suffix"));
    
/*    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageBox msg = new MessageBox(shell, SWT.ICON_INFORMATION|SWT.YES|SWT.NO);
        msg.setMessage("Do you want to start the data streamer ?");
        if (msg.open() == SWT.YES)
        {
          TraderPlugin.getDataProvider().startStreaming();
        }
      }
    });*/
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception 
  {
    getPreferenceStore().removePropertyChangeListener(this);
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static YahooPlugin getDefault() 
  {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) 
  {
		ResourceBundle bundle = YahooPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
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

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
  }
}
