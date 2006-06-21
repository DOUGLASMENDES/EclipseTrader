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

package net.sourceforge.eclipsetrader.core.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.ui.internal.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String CurrencyConversionDialog_Title;
    public static String CurrencyConversionDialog_Convert;
    public static String CurrencyConversionDialog_To;
    public static String CurrencyConversionDialog_Equal;
    public static String EventDetailsDialog_Title;
    public static String EventDetailsDialog_Date;
    public static String EventDetailsDialog_Security;
    public static String EventDetailsDialog_Message;
    public static String EventDetailsDialog_Details;
    public static String EventsView_RemoveActionTooltip;
    public static String EventsView_RemoveAllActionTooltip;
    public static String EventsView_Date;
    public static String EventsView_Time;
    public static String EventsView_Security;
    public static String EventsView_Message;
    public static String SecuritiesView_Code;
    public static String SecuritiesView_Description;
    public static String SecuritiesView_Currency;
    public static String DeleteSecurityAction_Title;
    public static String DeleteSecurityAction_Message;
    public static String SecurityWizard_Title;
    public static String SecurityWizard_QuoteFeedTitle;
    public static String SecurityWizard_QuoteFeedDescription;
    public static String SecurityWizard_Level2FeedTitle;
    public static String SecurityWizard_Level2FeedDescription;
    public static String SecurityWizard_HistoryFeedTitle;
    public static String SecurityWizard_HistoryFeedDescription;
    public static String SecurityWizard_IntradayChartsTitle;
    public static String SecurityWizard_IntradayChartsDescription;
    public static String SecurityPage_Title;
    public static String SecurityPage_Description;
    public static String SecurityPage_CodeColumn;
    public static String SecurityPage_DescriptionColumn;
    public static String SecurityPage_CurrencyColumn;
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
    public static String CSVExport_Title;
    public static String CSVExport_Description;
    public static String CSVExport_JobName;
    public static String CSVExport_ErrorMessage;
    public static String CSVExport_HistoricalTask;
    public static String CSVExport_IntradayTask;
    public static String CSVExport_LastTask;
    public static String ExportSelectionPage_All;
    public static String ExportSelectionPage_Selected;
    public static String ExportSelectionPage_SelectLabel;
    public static String ExportSelectionPage_Historical;
    public static String ExportSelectionPage_intraday;
    public static String ExportSelectionPage_Last;
    public static String ExportSelectionPage_DestinationLabel;
    public static String ExportSelectionPage_FileLabel;
    public static String ExportSelectionPage_BrowseButton;
    public static String ExportSelectionPage_FileDialogTitle;
}
