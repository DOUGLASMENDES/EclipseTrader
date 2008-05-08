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

package org.eclipsetrader.ui.internal.application;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipsetrader.ui.application";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
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
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Logs the given message to the platform log.
	 *
	 * If you have an exception in hand, call log(String, Throwable) instead.
	 *
	 * If you have a status object in hand call log(String, IStatus) instead.
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 */
	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	/**
	 * Logs the given message and throwable to the platform log.
	 *
	 * If you have a status object in hand call log(String, IStatus) instead.
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 * @param t
	 *            The throwable from where the problem actually occurred.
	 */
	public static void log(String message, Throwable t) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, t));
	}

	/**
	 * Logs the given throwable to the platform log, indicating the class and
	 * method from where it is being logged (this is not necessarily where it
	 * occurred).
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param clazz
	 *            The calling class.
	 * @param methodName
	 *            The calling method name.
	 * @param t
	 *            The throwable from where the problem actually occurred.
	 */
	public static void log(Class<?> clazz, String methodName, Throwable t) {
		String msg = MessageFormat.format("Exception in {0}.{1}: {2}", new Object[] { clazz.getName(), methodName, t }); //$NON-NLS-1$
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, t));
	}

	/**
	 * Logs the given message and status to the platform log.
	 *
	 * This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 *            May be <code>null</code>.
	 * @param status
	 *            The status describing the problem. Must not be null.
	 */
	public static void log(String message, IStatus status) {
		getDefault().getLog().log(new Status(status.getSeverity(), PLUGIN_ID, status.getCode(), message, status.getException()));
	}
}
