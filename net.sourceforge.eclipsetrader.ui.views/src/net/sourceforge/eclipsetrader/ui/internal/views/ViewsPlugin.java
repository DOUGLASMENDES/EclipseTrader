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

    IPreferenceStore ps = getPreferenceStore();
    
    ps.setDefault("portfolio.display", "code,ticker,description,price,variance,bid_price,bid_qty,ask_price,ask_qty,volume,open_price,high_price,low_price,close_price,time,");
    PreferenceConverter.setDefault(ps, "portfolio.text_color", new RGB(0, 0, 0));
    PreferenceConverter.setDefault(ps, "portfolio.even_row_background", new RGB(255, 255, 255));
    PreferenceConverter.setDefault(ps, "portfolio.odd_row_background", new RGB(255, 255, 224));
    PreferenceConverter.setDefault(ps, "portfolio.negative_value_color", new RGB(200, 0, 0));
    PreferenceConverter.setDefault(ps, "portfolio.positive_value_color", new RGB(0, 190, 0));
    PreferenceConverter.setDefault(ps, "portfolio.total_row_background", new RGB(255, 255, 0));

    PreferenceConverter.setDefault(ps, "book.negative_value_color", new RGB(200, 0, 0));
    PreferenceConverter.setDefault(ps, "book.positive_value_color", new RGB(0, 190, 0));
    PreferenceConverter.setDefault(ps, "trendbar.indicator", new RGB(0, 0, 0));
    PreferenceConverter.setDefault(ps, "trendbar.band1_color", new RGB(0, 0, 255));
    PreferenceConverter.setDefault(ps, "trendbar.band2_color", new RGB(255, 0, 0));
    PreferenceConverter.setDefault(ps, "trendbar.band3_color", new RGB(0, 255, 255));
    PreferenceConverter.setDefault(ps, "trendbar.band4_color", new RGB(0, 255, 0));
    PreferenceConverter.setDefault(ps, "trendbar.band5_color", new RGB(255, 255, 0));
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
