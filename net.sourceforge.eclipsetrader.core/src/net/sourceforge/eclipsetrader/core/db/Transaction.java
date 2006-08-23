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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Transaction extends PersistentObject
{
    private Date date;
    private Security security;
    private double price = 0;
    private int quantity = 0;
    private double expenses = 0;
    Map params = new HashMap();

    public Transaction()
    {
    }

    public Transaction(Integer id)
    {
        super(id);
    }

    public Transaction(Date date, Security security, int quantity, double price)
    {
        this.date = date;
        this.security = security;
        this.quantity = quantity;
        this.price = price;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
        setChanged();
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
        setChanged();
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
        setChanged();
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        this.security = security;
        setChanged();
    }

    public double getExpenses()
    {
        return expenses;
    }

    public void setExpenses(double expenses)
    {
        this.expenses = expenses;
        setChanged();
    }
    
    public double getAmount()
    {
        double amount = Math.abs(getQuantity() * getPrice());
        if (getQuantity() >= 0)
            return -(amount + getExpenses());
        else
            return amount - getExpenses();
    }

    public Map getParams()
    {
        return params;
    }

    public void setParams(Map params)
    {
        this.params = params;
    }
    
    public void setParam(String key, String value)
    {
        params.put(key, value);
    }
    
    public String getParam(String key)
    {
        return (String)params.get(key);
    }
}
