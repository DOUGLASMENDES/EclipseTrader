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

package org.eclipsetrader.directa.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.directa.internal.core.messages"; //$NON-NLS-1$
    public static String BrokerConnector_30Days;
    public static String BrokerConnector_InvalidOrderSide;
    public static String BrokerConnector_InvalidOrderType;
    public static String BrokerConnector_InvalidOrderValidity;
    public static String BrokerConnector_Liquidity;
    public static String OrderMonitor_InvalidOrder;
    public static String OrderMonitor_ModifyNotAllowed;
    public static String OrderMonitor_UnableToLogin;
    public static String WebConnector_DefaultAccount;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
