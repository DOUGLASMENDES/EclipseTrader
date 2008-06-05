/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts;

import java.net.URI;
import java.util.UUID;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class ChartsUIActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipsetrader.ui.charts";

	// The extension points IDs
	public static final String INDICATORS_EXTENSION_ID = "org.eclipsetrader.ui.indicators";

	// The shared instance
	private static ChartsUIActivator plugin;

	/**
	 * The constructor
	 */
	public ChartsUIActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ChartsUIActivator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		if (plugin != null)
			plugin.getLog().log(status);
	}

	public IChartObjectFactory getChartObjectFactory(String targetID) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(INDICATORS_EXTENSION_ID);
		if (extensionPoint == null)
			return null;

		IConfigurationElement targetElement = null;
		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
		for (int j = 0; j < configElements.length; j++) {
			String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
			if (targetID.equals(strID)) {
				targetElement = configElements[j];
				break;
			}
		}
		if (targetElement == null)
			return null;

		try {
			return (IChartObjectFactory) targetElement.createExecutableExtension("class");
		} catch (Exception e) {
			Status status = new Status(Status.WARNING, PLUGIN_ID, 0, "Unable to create indicator with id " + targetID, e);
			getLog().log(status);
		}

		return null;
	}

	public IDialogSettings getDialogSettingsForView(URI uri) {
		String uriString = uri.toString();

		IDialogSettings rootSettings = getDialogSettings().getSection("Views");
		if (rootSettings == null)
			rootSettings = getDialogSettings().addNewSection("Views");

		IDialogSettings[] sections = rootSettings.getSections();
		for (int i = 0; i < sections.length; i++) {
			if (uriString.equals(sections[i].get("uri")))
				return sections[i];
		}

		String uuid = UUID.randomUUID().toString();
		IDialogSettings dialogSettings = rootSettings.addNewSection(uuid);
		dialogSettings.put("uri", uriString);
		dialogSettings.put("template", "basic-template.xml");

		return dialogSettings;
	}

	public IRepositoryService getRepositoryService() {
		BundleContext context = getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
		context.ungetService(serviceReference);
		return service;
	}
}
