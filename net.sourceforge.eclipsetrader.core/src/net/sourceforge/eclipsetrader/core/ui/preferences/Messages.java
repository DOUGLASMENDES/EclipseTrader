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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.ui.preferences.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String CurrencyPreferencesPage_AutoUpdate;
	public static String DataPreferencesPage_Days;
	public static String DataPreferencesPage_DeleteCanceledOrders;
	public static String DataPreferencesPage_DeleteFilledOrders;
	public static String DataPreferencesPage_HistoryRange;
    public static String DataPreferencesPage_HistoryRangeTooltip;
	public static String DataPreferencesPage_Years;
    public static String DividendsPage_Add;
    public static String DividendsPage_Date;
    public static String DividendsPage_Delete;
    public static String DividendsPage_Title;
    public static String DividendsPage_Value;
    public static String LoggerPreferencesPage_All;
    public static String LoggerPreferencesPage_Debug;
    public static String LoggerPreferencesPage_Default;
    public static String LoggerPreferencesPage_Error;
    public static String LoggerPreferencesPage_Fatal;
    public static String LoggerPreferencesPage_Format;
    public static String LoggerPreferencesPage_General;
    public static String LoggerPreferencesPage_Info;
    public static String LoggerPreferencesPage_Levels;
    public static String LoggerPreferencesPage_Off;
    public static String LoggerPreferencesPage_Warn;
    public static String LoggerPreferencesPage_WriteToConsole;
    public static String LoggerPreferencesPage_WriteToFile;
	public static String SecurityPropertiesDialog_Name;
    public static String SecurityPropertiesDialog_TradeSourceTitle;
    public static String FeedOptions_Feed;
    public static String FeedOptions_Exchange;
    public static String FeedOptions_Symbol;
    public static String SecurityPropertiesDialog_Title;
    public static String SecurityPropertiesDialog_GeneralPage;
    public static String SecurityPropertiesDialog_Code;
    public static String SecurityPropertiesDialog_Description;
    public static String SecurityPropertiesDialog_Currency;
    public static String SecurityPropertiesDialog_ClearHistory;
    public static String SecurityPropertiesDialog_ClearIntraday;
    public static String SecurityPropertiesDialog_QuotePage;
    public static String SecurityPropertiesDialog_Level2Page;
    public static String SecurityPropertiesDialog_HistoryPage;
    public static String SecurityPropertiesDialog_IntradayChartsPage;
    public static String CurrencyPreferencesPage_Country;
    public static String CurrencyPreferencesPage_Code;
    public static String CurrencyPreferencesPage_JobName;
    public static String IntradayDataOptions_Enable;
    public static String IntradayDataOptions_Begin;
    public static String IntradayDataOptions_End;
    public static String IntradayDataOptions_WeekDays;
    public static String IntradayDataOptions_Sun;
    public static String IntradayDataOptions_Mon;
    public static String IntradayDataOptions_Tue;
    public static String IntradayDataOptions_Wed;
    public static String IntradayDataOptions_Thu;
    public static String IntradayDataOptions_Fri;
    public static String IntradayDataOptions_Sat;
    public static String IntradayDataOptions_KeepDays;
    public static String SplitsPage_Add;
    public static String SplitsPage_Date;
    public static String SplitsPage_Delete;
    public static String SplitsPage_From;
    public static String SplitsPage_Title;
    public static String SplitsPage_To;
    public static String TradeSourceOptions_Account;
    public static String TradeSourceOptions_Exchange;
    public static String TradeSourceOptions_Provider;
    public static String TradeSourceOptions_Quantity;
    public static String TradeSourceOptions_Symbol;
}
