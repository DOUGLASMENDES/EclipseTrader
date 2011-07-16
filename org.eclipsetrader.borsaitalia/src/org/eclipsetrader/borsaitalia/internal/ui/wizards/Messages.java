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

package org.eclipsetrader.borsaitalia.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.borsaitalia.internal.ui.wizards.messages"; //$NON-NLS-1$
    public static String DataImportJob_ErrorDownloadingDataFor;
    public static String DataImportJob_MissingDataFor;
    public static String DataImportJob_Name;
    public static String DataImportWizard_WindowTitle;
    public static String ImportDataPage_Aggregation;
    public static String ImportDataPage_AllSecurities;
    public static String ImportDataPage_Days;
    public static String ImportDataPage_Description;
    public static String ImportDataPage_Full;
    public static String ImportDataPage_FullIncremental;
    public static String ImportDataPage_Import;
    public static String ImportDataPage_Incremental;
    public static String ImportDataPage_Minutes;
    public static String ImportDataPage_Period;
    public static String ImportDataPage_SecuritiesListedBelow;
    public static String ImportDataPage_To;
    public static String ImportDataPage_Type;
    public static String InstrumentsImportWizard_WindowTitle;
    public static String InstrumentsPage_Description;
    public static String InstrumentsPage_Instruments;
    public static String InstrumentsPage_Name;
    public static String MarketsPage_Description;
    public static String MarketsPage_Markets;
    public static String MarketsPage_Name;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
