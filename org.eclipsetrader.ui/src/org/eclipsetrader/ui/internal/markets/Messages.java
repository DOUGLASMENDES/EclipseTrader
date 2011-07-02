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

package org.eclipsetrader.ui.internal.markets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.ui.internal.markets.messages"; //$NON-NLS-1$
    public static String MarketLabelProvider_Closed;
    public static String MarketLabelProvider_ClosedFor;
    public static String MarketLabelProvider_ClosesInHours;
    public static String MarketLabelProvider_ClosesInLessThanOneMinute;
    public static String MarketLabelProvider_ClosesInMinutes;
    public static String MarketLabelProvider_Open;
    public static String MarketLabelProvider_OpenDate;
    public static String MarketLabelProvider_OpenInLessThanOneMinute;
    public static String MarketLabelProvider_OpensInHours;
    public static String MarketLabelProvider_OpensInMinutes;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
