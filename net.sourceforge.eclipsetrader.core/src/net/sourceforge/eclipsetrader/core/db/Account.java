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

package net.sourceforge.eclipsetrader.core.db;

import java.util.Currency;

import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;

public class Account extends PersistentObject
{
    private String serviceId;
    private String description;
    private Currency currency;
    private AccountGroup group;
    private double initialBalance = 0;
    private double fixedCommissions = 0;
    private double variableCommissions = 0;
    private ObservableList transactions = new ObservableList();
    private ICollectionObserver transactionsObserver = new ICollectionObserver() {
        public void itemAdded(Object o)
        {
            setChanged();
        }

        public void itemRemoved(Object o)
        {
            setChanged();
        }
    };

    public Account()
    {
        transactions.addCollectionObserver(transactionsObserver);
    }

    public Account(Integer id)
    {
        super(id);
        transactions.addCollectionObserver(transactionsObserver);
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(String serviceId)
    {
        this.serviceId = serviceId;
        setChanged();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        setChanged();
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public void setCurrency(Currency currency)
    {
        this.currency = currency;
        setChanged();
    }

    public AccountGroup getGroup()
    {
        return group;
    }

    public void setGroup(AccountGroup group)
    {
        this.group = group;
        setChanged();
    }

    public double getInitialBalance()
    {
        return initialBalance;
    }

    public void setInitialBalance(double initbalance)
    {
        this.initialBalance = initbalance;
        setChanged();
    }

    public double getFixedCommissions()
    {
        return fixedCommissions;
    }

    public void setFixedCommissions(double fixedCommissions)
    {
        this.fixedCommissions = fixedCommissions;
    }

    public double getVariableCommissions()
    {
        return variableCommissions;
    }

    public void setVariableCommissions(double variableCommissions)
    {
        this.variableCommissions = variableCommissions;
    }

    public ObservableList getTransactions()
    {
        return transactions;
    }

    /**
     * Gets the balance of the account
     * 
     * @return the account's balance
     */
    public double getBalance()
    {
        double result = initialBalance;

        Object[] objs = getTransactions().toArray();
        for (int i = 0; i < objs.length; i++)
        {
            Transaction transaction = (Transaction)objs[i];
            double amount = Math.abs(transaction.getQuantity() * transaction.getPrice());
            if (transaction.getQuantity() >= 0)
                result -= amount + transaction.getExpenses();
            else
                result += amount - transaction.getExpenses();
        }
        
        return result;
    }

    /**
     * Return the number of stocks held for the given security.
     * 
     * @param security the security to search
     * @return holded quantity
     */
    public int getPosition(Security security)
    {
        int result = 0;
        
        Object[] objs = getTransactions().toArray();
        for (int i = 0; i < objs.length; i++)
        {
            Transaction transaction = (Transaction)objs[i];
            if (transaction.getSecurity().equals(security))
                result += transaction.getQuantity();
        }
        
        return result;
    }
}