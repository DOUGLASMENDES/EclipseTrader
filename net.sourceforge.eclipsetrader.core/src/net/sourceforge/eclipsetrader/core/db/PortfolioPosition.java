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

import net.sourceforge.eclipsetrader.core.db.feed.Quote;

public class PortfolioPosition
{
    private Account account;
    private Security security;
    private int quantity;
    private double price;

    public PortfolioPosition(Account account, Security security, int quantity, double amount)
    {
        this.account = account;
        this.security = security;
        this.quantity = quantity;
        this.price = amount == 0 ? 0 : Math.abs(amount / quantity);
    }
    
    public void add(int quantity, double amount)
    {
        double total = this.quantity * this.price - amount;
        this.quantity += quantity;
        this.price = this.quantity == 0 ? 0 : Math.abs(total / this.quantity);
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        this.security = security;
    }
    
    public double getValue()
    {
        return Math.abs(quantity) * price;
    }
    
    public double getMarketValue()
    {
        double result = 0;
        
        Quote quote = security.getQuote();
        if (quote != null)
        {
            result = Math.abs(quantity) * quote.getLast();
            result -= account.getExpenses(security, quantity, price);
        }
        
        return result;
    }
}
