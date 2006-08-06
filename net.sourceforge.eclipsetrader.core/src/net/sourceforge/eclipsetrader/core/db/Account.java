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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class Account extends PersistentObject implements Cloneable
{
    private String pluginId = "";
    private String description = "";
    private Currency currency = null;
    private double initialBalance = 0;
    private AccountGroup group = null;
    private PersistentPreferenceStore preferenceStore = new PersistentPreferenceStore();
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
    private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            setChanged();
        }
    };

    public Account()
    {
        transactions.addCollectionObserver(transactionsObserver);
        preferenceStore.addPropertyChangeListener(propertyChangeListener);
    }

    public Account(Integer id)
    {
        super(id);
        transactions.addCollectionObserver(transactionsObserver);
        preferenceStore.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Creates a copy of the given account.
     */
    protected Account(Account account)
    {
        setDescription(account.getDescription());
        setCurrency(account.getCurrency());
        setInitialBalance(account.getBalance());
        setGroup(account.getGroup());
        setPreferenceStore(new PersistentPreferenceStore(account.getPreferenceStore()));
    }

    public String getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(String serviceId)
    {
        Assert.isNotNull(serviceId);
        this.pluginId = serviceId;
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

    public double getInitialBalance()
    {
        return initialBalance;
    }

    public void setInitialBalance(double initbalance)
    {
        this.initialBalance = initbalance;
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

    public PersistentPreferenceStore getPreferenceStore()
    {
        return preferenceStore;
    }

    public void setPreferenceStore(PersistentPreferenceStore preferenceStore)
    {
        Assert.isNotNull(preferenceStore);
        this.preferenceStore.removePropertyChangeListener(propertyChangeListener);
        this.preferenceStore = preferenceStore;
        this.preferenceStore.addPropertyChangeListener(propertyChangeListener);
    }

    public ObservableList getTransactions()
    {
        return transactions;
    }

    public void setTransactions(List transactions)
    {
        Assert.isNotNull(transactions);
        if (this.transactions != null)
            this.transactions.removeCollectionObserver(transactionsObserver);
        this.transactions = new ObservableList(transactions);
        this.transactions.addCollectionObserver(transactionsObserver);
    }

    /**
     * Gets the balance of the account
     * 
     * @return the account's balance
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
                amount = CurrencyConverter.getInstance().convert(transaction.getDate(), amount, transaction.getSecurity().getCurrency(), getCurrency());
            result += amount;
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
    
    public List getPortfolio()
    {
        List result = new ArrayList();
        
        Map map = new HashMap();
        for (Iterator iter2 = getTransactions().iterator(); iter2.hasNext(); )
        {
            Transaction transaction = (Transaction)iter2.next();
            PortfolioPosition position = (PortfolioPosition)map.get(transaction.getSecurity());
            if (position == null)
                map.put(transaction.getSecurity(), new PortfolioPosition(this, transaction.getSecurity(), transaction.getQuantity(), transaction.getAmount()));
            else
                position.add(transaction.getQuantity(), transaction.getAmount());
        }

        List list = new ArrayList(map.keySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Security)arg0).getDescription().compareTo(((Security)arg1).getDescription());
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Security security = (Security)iter.next();
            PortfolioPosition position = (PortfolioPosition)map.get(security);
            if (position.getQuantity() != 0)
                result.add(position);
        }

        return result;
    }
    
    public PortfolioPosition getPortfolio(Security security)
    {
        PortfolioPosition position = new PortfolioPosition(this, security, 0, 0);
        
        for (Iterator iter2 = getTransactions().iterator(); iter2.hasNext(); )
        {
            Transaction transaction = (Transaction)iter2.next();
            if (position.getSecurity().equals(transaction.getSecurity()))
                position.add(transaction.getQuantity(), transaction.getAmount());
        }

        return position;
    }
    
    public double getExpenses(Security security, int quantity, double price)
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
}
