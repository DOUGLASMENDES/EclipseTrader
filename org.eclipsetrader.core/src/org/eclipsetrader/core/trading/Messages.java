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

package org.eclipsetrader.core.trading;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.core.trading.messages"; //$NON-NLS-1$

    public static String IOrderSide_Buy;
    public static String IOrderSide_BuyCover;
    public static String IOrderSide_Sell;
    public static String IOrderSide_SellShort;

    public static String IOrderStatus_Canceled;
    public static String IOrderStatus_Expired;
    public static String IOrderStatus_Filled;
    public static String IOrderStatus_New;
    public static String IOrderStatus_Partial;
    public static String IOrderStatus_PendingCancel;
    public static String IOrderStatus_PendingNew;
    public static String IOrderStatus_Rejected;

    public static String IOrderType_Limit;
    public static String IOrderType_Market;
    public static String IOrderType_Stop;
    public static String IOrderType_StopLimit;

    public static String IOrderValidity_AtClosing;
    public static String IOrderValidity_AtOpening;
    public static String IOrderValidity_Day;
    public static String IOrderValidity_GoodTillCancel;
    public static String IOrderValidity_GoodTillDate;
    public static String IOrderValidity_ImmediateOrCancel;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
