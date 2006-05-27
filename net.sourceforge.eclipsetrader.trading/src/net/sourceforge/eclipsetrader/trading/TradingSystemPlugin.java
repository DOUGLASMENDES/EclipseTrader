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

package net.sourceforge.eclipsetrader.trading;

import java.util.Map;
import java.util.Observable;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;

public abstract class TradingSystemPlugin extends Observable
{
    private Account account;
    private Security security;
    private double maxExposure = 0;
    private double maxAmount = 0;
    private double minAmount = 0;
    private int signal = TradingSystem.SIGNAL_NONE;
    private int quantity = 0;

    public TradingSystemPlugin()
    {
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        this.security = security;
    }

    public double getMaxAmount()
    {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmountPerTransaction)
    {
        this.maxAmount = maxAmountPerTransaction;
    }

    public double getMaxExposure()
    {
        return maxExposure;
    }

    public void setMaxExposure(double maxExposure)
    {
        this.maxExposure = maxExposure;
    }

    public double getMinAmount()
    {
        return minAmount;
    }

    public void setMinAmount(double minAmountPerTransaction)
    {
        this.minAmount = minAmountPerTransaction;
    }

    public void setParameters(Map parameters)
    {
    }

    public abstract void run();

    public int getSignal()
    {
        return signal;
    }

    public void setSignal(int signal)
    {
        this.signal = signal;
    }

    public int getQuantity()
    {
        return quantity;
    }

    protected void fireOpenLongSignal()
    {
        PortfolioPosition position = getAccount().getPortfolio(getSecurity());
        Bar lastQuote = (Bar)getSecurity().getHistory().get(getSecurity().getHistory().size() - 1);
        
        if (maxExposure == 0 || position.getValue() < maxExposure)
        {
            double amount = maxAmount;
            if (amount == 0)
                amount = maxExposure;
            if (amount == 0)
                amount = getAccount().getBalance();
            if (maxExposure != 0 && (amount + position.getValue()) > maxExposure)
                amount = maxExposure - position.getValue(); 
            
            if (amount >= minAmount && amount <= getAccount().getBalance())
            {
                quantity = (int)(amount / lastQuote.getClose());
                if (quantity != 0)
                {
                    setSignal(TradingSystem.SIGNAL_BUY);
                    setChanged();
                    notifyObservers();
                }
            }
        }
    }
    
    protected void fireCloseLongSignal()
    {
        PortfolioPosition position = getAccount().getPortfolio(getSecurity());

        if (position.getQuantity() != 0)
        {
            quantity = position.getQuantity();
            setSignal(TradingSystem.SIGNAL_SELL);
            setChanged();
            notifyObservers();
        }
    }
}
