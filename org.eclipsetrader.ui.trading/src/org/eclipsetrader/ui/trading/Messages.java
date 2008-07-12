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

package org.eclipsetrader.ui.trading;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipsetrader.ui.trading.messages"; //$NON-NLS-1$

	public static String SideColumn_Buy;
	public static String SideColumn_BuyCover;
	public static String SideColumn_Sell;
	public static String SideColumn_SellShort;

	public static String StatusColumn_Canceled;
	public static String StatusColumn_Expired;
	public static String StatusColumn_Filled;
	public static String StatusColumn_New;
	public static String StatusColumn_Partial;
	public static String StatusColumn_PendingCancel;
	public static String StatusColumn_PendingNew;
	public static String StatusColumn_Rejected;

	public static String TypeColumn_Limit;
	public static String TypeColumn_Market;
	public static String TypeColumn_Stop;
	public static String TypeColumn_StopLimit;

	public static String ValidityColumn_AtClosing;
	public static String ValidityColumn_AtOpening;
	public static String ValidityColumn_Day;
	public static String ValidityColumn_GoodTillCancel;
	public static String ValidityColumn_ImmediateOrCancel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
