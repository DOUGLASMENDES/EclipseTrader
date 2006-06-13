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

package net.sourceforge.eclipsetrader.charts.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.charts.internal.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ChartView_NewObject;
    public static String ChartView_Period;
    public static String ChartView_SetInterval;
    public static String ChartView_NoDataMessage;
    public static String ChartView_UpdateChartMessage;
    public static String ChartView_UpdatingMessage;
    public static String ChartView_DeleteMessagePrefix;
    public static String ChartView_DeleteMessageSuffix;
    public static String DataView_Date;
    public static String DataView_Open;
    public static String DataView_High;
    public static String DataView_Low;
    public static String DataView_Close;
}
