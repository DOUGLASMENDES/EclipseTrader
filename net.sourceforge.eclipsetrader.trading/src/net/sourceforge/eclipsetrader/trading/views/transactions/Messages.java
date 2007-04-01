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

package net.sourceforge.eclipsetrader.trading.views.transactions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.trading.views.transactions.messages"; //$NON-NLS-1$
    public static String TransactionsView_Code;
    public static String TransactionsView_DateTime;
    public static String TransactionsView_DateTimeFormat;
    public static String TransactionsView_Delete;
    public static String TransactionsView_DeleteConfirmMessage;
    public static String TransactionsView_DeleteTooltip;
    public static String TransactionsView_Description;
    public static String TransactionsView_Edit;
    public static String TransactionsView_Expenses;
    public static String TransactionsView_Operation;
    public static String TransactionsView_Price;
    public static String TransactionsView_Quantity;
    public static String TransactionsView_Total;
    public static String TransactionsView_Buy;
    public static String TransactionsView_Sell;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
