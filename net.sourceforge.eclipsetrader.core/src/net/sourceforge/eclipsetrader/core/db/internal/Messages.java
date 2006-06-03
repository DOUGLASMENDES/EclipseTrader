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

package net.sourceforge.eclipsetrader.core.db.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.db.internal.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String AskPrice_Label;
    public static String AskSize_Label;
    public static String Balance_Label;
    public static String BidPrice_Label;
    public static String BidSize_Label;
    public static String Change_Label;
    public static String ChangePercent_Label;
    public static String ClosePrice_Label;
    public static String Code_Label;
    public static String Currency_Label;
    public static String Date_Label;
    public static String DateTime_Label;
    public static String Description_Label;
    public static String Description_Totals;
    public static String HighPrice_Label;
    public static String LastPrice_Label;
    public static String LowPrice_Label;
    public static String OpenPrice_Label;
    public static String PaidPrice_Label;
    public static String Position_Label;
    public static String Time_Label;
    public static String Volume_Label;
}
