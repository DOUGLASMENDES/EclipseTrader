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
package net.sourceforge.eclipsetrader.ui.internal.views;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ViewsPlugin extends AbstractUIPlugin 
{
	private static ViewsPlugin plugin;
	private ResourceBundle resourceBundle;
	
	/**
	 * The plugin's constructor.
	 */
	public ViewsPlugin() 
  {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetrader.ui.views.portfolio.PortfolioPluginResources");
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

    IPreferenceStore pref = getPreferenceStore();
    
    pref.setDefault("portfolio.display", "code,ticker,description,price,variance,bid_price,bid_qty,ask_price,ask_qty,volume,open_price,high_price,low_price,close_price,time,");
    PreferenceConverter.setDefault(pref, "portfolio.text_color", new RGB(0, 0, 0));
    PreferenceConverter.setDefault(pref, "portfolio.even_row_background", new RGB(255, 255, 255));
    PreferenceConverter.setDefault(pref, "portfolio.odd_row_background", new RGB(255, 255, 224));
    PreferenceConverter.setDefault(pref, "portfolio.negative_value_color", new RGB(200, 0, 0));
    PreferenceConverter.setDefault(pref, "portfolio.positive_value_color", new RGB(0, 190, 0));
    PreferenceConverter.setDefault(pref, "portfolio.total_row_background", new RGB(255, 255, 0));

    PreferenceConverter.setDefault(pref, "book.text_color", new RGB(0, 0, 0));
    PreferenceConverter.setDefault(pref, "book.background", new RGB(255, 255, 255));
    PreferenceConverter.setDefault(pref, "book.negative_value_color", new RGB(200, 0, 0));
    PreferenceConverter.setDefault(pref, "book.positive_value_color", new RGB(0, 190, 0));
    PreferenceConverter.setDefault(pref, "book.level1_color", new RGB(0, 0, 192));
    PreferenceConverter.setDefault(pref, "book.level2_color", new RGB(192, 0, 0));
    PreferenceConverter.setDefault(pref, "book.level3_color", new RGB(0, 255, 255));
    PreferenceConverter.setDefault(pref, "book.level4_color", new RGB(0, 255, 0));
    PreferenceConverter.setDefault(pref, "book.level5_color", new RGB(255, 255, 0));

    PreferenceConverter.setDefault(pref, "trendbar.indicator", new RGB(0, 0, 0));
    
    pref.setDefault("news.columnWidth", "105,435,145");
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
	public static ViewsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ViewsPlugin.getDefault().getResourceBundle();
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
}
