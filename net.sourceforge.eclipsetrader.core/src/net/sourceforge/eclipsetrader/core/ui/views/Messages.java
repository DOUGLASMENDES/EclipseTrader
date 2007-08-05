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

package net.sourceforge.eclipsetrader.core.ui.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.ui.views.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String EventsView_RemoveActionTooltip;
	public static String EventsView_RemoveAllActionTooltip;
	public static String EventsView_Date;
	public static String EventsView_Time;
	public static String EventsView_Security;
	public static String EventsView_Message;
	public static String WebBrowser_Back;
	public static String WebBrowser_Forward;
	public static String WebBrowser_Go;
	public static String WebBrowser_Refresh;
	public static String WebBrowser_Stop;
}
