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

package net.sourceforge.eclipsetrader.yahoo.internal.updater;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.yahoo.internal.updater.messages"; //$NON-NLS-1$

	public static String FrenchListUpdateJob_Name;

	public static String FrenchListUpdateJob_TaskName;

	public static String GermanyListUpdateJob_Name;

	public static String GermanyListUpdateJob_TaskName;

	public static String ListUpdateJob_Name;

	public static String USListUpdateJob_Name;

	public static String USListUpdateJob_TaskName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
