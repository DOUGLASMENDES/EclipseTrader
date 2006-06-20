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

package net.sourceforge.eclipsetrader.trading.internal;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Transaction;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

public class SimpleAccount extends Account
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.accounts.simple";
    public static final String PREFS_FIXEDCOMMISSIONS = "fixedCommissions";
    public static final String PREFS_VARIABLECOMMISSIONS = "variableCommissions";
    public static final String PREFS_MINIMUMCOMMISSION = "minimumCommission";
    public static final String PREFS_MAXIMUMCOMMISSION = "maximumCommission";

    public SimpleAccount()
    {
        setPluginId(PLUGIN_ID);
    }

    public SimpleAccount(Integer id)
    {
        super(id);
        setPluginId(PLUGIN_ID);
    }

    protected SimpleAccount(Account account)
    {
        super(account);
    }

    public double getFixedCommissions()
    {
        return getPreferenceStore().getDouble(PREFS_FIXEDCOMMISSIONS); //$NON-NLS-1$
    }

    public double getVariableCommissions()
    {
        return getPreferenceStore().getDouble(PREFS_VARIABLECOMMISSIONS); //$NON-NLS-1$
    }

    public double getMaximumCommission()
    {
        return getPreferenceStore().getDouble(PREFS_MAXIMUMCOMMISSION); //$NON-NLS-1$
    }

    public double getMinimumCommission()
    {
        return getPreferenceStore().getDouble(PREFS_MINIMUMCOMMISSION); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.Account#getBalance()
     */
    public double getBalance()
    {
        double result = getInitialBalance();

        Object[] objs = getTransactions().toArray();
        for (int i = 0; i < objs.length; i++)
        {
            Transaction transaction = (Transaction)objs[i];
            double amount = transaction.getAmount();
            if (getCurrency() != null && !getCurrency().equals(transaction.getSecurity().getCurrency()))
                amount = CurrencyConverter.getInstance().convert(amount, getCurrency(), transaction.getSecurity().getCurrency());
            result += amount;
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.Account#getExpenses(net.sourceforge.eclipsetrader.core.db.Security, int, double)
     */
    public double getExpenses(Security security, int quantity, double price)
    {
        double expenses = 0;
        
        Quote quote = security.getQuote();
        if (quote != null)
        {
            double value = Math.abs(quantity) * quote.getLast();
            expenses = getFixedCommissions() + (value / 100.0 * getVariableCommissions());
        }
        if (expenses < getMinimumCommission())
            expenses = getMinimumCommission();
        if (getMaximumCommission() != 0 && expenses > getMaximumCommission())
            expenses = getMaximumCommission();
        
        return expenses;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.Account#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        return new SimpleAccount(this);
    }
}
