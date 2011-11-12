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

package org.eclipsetrader.directa.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.directa.internal.ui.wizards.messages"; //$NON-NLS-1$
    public static String DataImportJob_Title;
    public static String DataImportWizard_Title;
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
    public static String ImportDataPage_SecuritiesSelectedBelow;
    public static String ImportDataPage_Title;
    public static String ImportDataPage_To;
    public static String ImportDataPage_Type;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
