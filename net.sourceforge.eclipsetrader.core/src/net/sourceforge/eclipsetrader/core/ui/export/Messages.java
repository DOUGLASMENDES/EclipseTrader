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

package net.sourceforge.eclipsetrader.core.ui.export;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.ui.export.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
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
