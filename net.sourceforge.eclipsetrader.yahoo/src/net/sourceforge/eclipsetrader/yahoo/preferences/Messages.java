/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.yahoo.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.yahoo.preferences.messages"; //$NON-NLS-1$

	public static String FrenchNewsPreferencesPage_GetSubscribersOnly;

	public static String FrenchNewsPreferencesPage_ProviderColumnName;

	public static String ItalianNewsPreferencesPage_ProviderColumnName;

	public static String NewsPreferencesPage_GetSubscribersOnly;

	public static String NewsPreferencesPage_ProviderColumnName;

	public static String PluginPreferencesPage_SnapshotUpdate;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
