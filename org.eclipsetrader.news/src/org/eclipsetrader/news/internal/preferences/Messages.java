/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.news.internal.preferences.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String RSSFeedDialog_Title;
    public static String RSSFeedDialog_Source;
    public static String RSSFeedDialog_URL;

    public static String NewsPreferencesPage_RunAtStartup;
    public static String NewsPreferencesPage_FollowQuoteFeed;
    public static String NewsPreferencesPage_DaysToKeep;
    public static String NewsPreferencesPage_EnableDecorators;
    public static String NewsPreferencesPage_Providers;

    public static String RSSPreferencesPage_AutoUpdate;
    public static String RSSPreferencesPage_Minutes;
    public static String RSSPreferencesPage_Subscriptions;
    public static String RSSPreferencesPage_Add;
    public static String RSSPreferencesPage_Edit;
    public static String RSSPreferencesPage_Remove;
}
