/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats;

import net.sourceforge.eclipsetrader.ats.core.IComponent;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ATSPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.ats";

	public static final String COMPONENTS_EXTENSION_ID = PLUGIN_ID + ".components";

	public static final String STRATEGIES_EXTENSION_ID = PLUGIN_ID + ".strategies";

	public static final String SYSTEMS_EXTENSION_ID = PLUGIN_ID + ".systems";

	public static final String TRADING_SYSTEM_ICON = "TRADING_SYSTEM";

	public static final String ENTRY_COMPONENT_ICON = "ENTRY_COMPONENT";

	public static final String EXIT_COMPONENT_ICON = "EXIT_COMPONENT";

	public static final String RISK_COMPONENT_ICON = "RISK_COMPONENT";

	public static final String EXPOSURE_COMPONENT_ICON = "EXPOSURE_COMPONENT";

	public static final String MONEY_COMPONENT_ICON = "MONEY_COMPONENT";

	public static final String STRATEGY_ICON = "STRATEGY";

	public static final String SECURITY_ICON = "SECURITY";

	private static ATSPlugin plugin;

	private static Repository repository;

	/**
	 * The constructor
	 */
	public ATSPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		repository = new Repository();
		repository.load();

		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		repository.save();

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ATSPlugin getDefault() {
		return plugin;
	}

	public static Repository getRepository() {
		return repository;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(ENTRY_COMPONENT_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/door_in.png")));
		reg.put(EXIT_COMPONENT_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/door_out.png")));
		reg.put(RISK_COMPONENT_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/shield.png")));
		reg.put(EXPOSURE_COMPONENT_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/error.png")));
		reg.put(MONEY_COMPONENT_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/money.png")));
		reg.put(STRATEGY_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/arrow_branch.png")));
		reg.put(TRADING_SYSTEM_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/cog.png")));
		reg.put(SECURITY_ICON, ImageDescriptor.createFromURL(getBundle().getEntry("icons/full/obj16/database.png")));
	}

	public static IComponent createStrategyPlugin(String id) {
		return (IComponent) createExtensionPlugin(STRATEGIES_EXTENSION_ID, id);
	}

	public static String getStrategyPluginName(String id) {
		return getPluginName(STRATEGIES_EXTENSION_ID, id);
	}

	public static Object createExtensionPlugin(String extensionId, String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
				{
					try {
						return members[i].createExecutableExtension("class"); //$NON-NLS-1$
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		return null;
	}

	public static String getPluginName(String extensionId, String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
					return item.getAttribute("name"); //$NON-NLS-1$
			}
		}

		return null;
	}
}
