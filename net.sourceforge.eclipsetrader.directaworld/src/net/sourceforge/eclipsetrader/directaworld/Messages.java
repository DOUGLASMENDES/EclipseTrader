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

package net.sourceforge.eclipsetrader.directaworld;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.directaworld.messages"; //$NON-NLS-1$
    public static String GeneralPreferencesPage_Password;
    public static String GeneralPreferencesPage_UserName;
    public static String GeneralPreferencesPage_WarningMessage;
    public static String LoginDialog_Caption;
    public static String LoginDialog_Description;
    public static String LoginDialog_Password;
    public static String LoginDialog_SavePassword;
    public static String LoginDialog_Title;
    public static String LoginDialog_UserName;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
