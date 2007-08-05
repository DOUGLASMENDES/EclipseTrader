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

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.internal.ui.views.explorer.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String BulkChangesAction_HistoryFeedLabel;
	public static String BulkChangesAction_IntradayChartLabel;
	public static String BulkChangesAction_Label;
	public static String BulkChangesAction_Level2FeedLabel;
	public static String BulkChangesAction_QuoteFeedLabel;
	public static String BulkChangesAction_TradingLabel;
	public static String FlatLabelProvider_Separator;
	public static String GroupsPresentationAction_FlatLabel;
	public static String GroupsPresentationAction_HierarchicalLabel;
	public static String GroupsPresentationAction_Label;
	public static String SecurityExplorer_CollapseAllAction;
	public static String SecurityExplorer_CreateGroupAction;
	public static String SecurityExplorer_DeleteAction;
	public static String SecurityExplorer_ExpandAllAction;
	public static String SecurityExplorer_LinkSelectionAction;
	public static String DeleteSecurityAction_Message;
	public static String DeleteSecurityAction_Title;
}
