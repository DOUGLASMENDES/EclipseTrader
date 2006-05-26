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

package net.sourceforge.eclipsetrader.core.db.trading;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;

public class TradingSystem extends PersistentObject
{
    public static final int SIGNAL_NONE = 0;
    public static final int SIGNAL_BUY = 1;
    public static final int SIGNAL_SELL = 2;
    public static final int SIGNAL_HOLD = 3;
    public static final int SIGNAL_NEUTRAL = 4;
    private TradingSystemGroup group;
    private String pluginId;
    private Security security;
    private Account account;
    private double maxExposure = 0;
    private double maxAmount = 0;
    private double minAmount = 0;
    private Date date;
    private int signal = SIGNAL_NONE;
    private Map parameters = new HashMap();

    public TradingSystem()
    {
    }

    public TradingSystem(Integer id)
    {
        super(id);
    }

    public TradingSystemGroup getGroup()
    {
        return group;
    }

    public void setGroup(TradingSystemGroup group)
    {
        this.group = group;
        setChanged();
    }

    public String getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(String pluginId)
    {
        this.pluginId = pluginId;
        setChanged();
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
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

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public int getSignal()
    {
        return signal;
    }

    public void setSignal(int signal)
    {
        this.signal = signal;
        setChanged();
    }

    public Map getParameters()
    {
        return parameters;
    }

    public void setParameters(Map parameters)
    {
        this.parameters = parameters;
        setChanged();
    }

    public double getMaxAmount()
    {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmountPerTransaction)
    {
        this.maxAmount = maxAmountPerTransaction;
        setChanged();
    }

    public double getMaxExposure()
    {
        return maxExposure;
    }

    public void setMaxExposure(double maxExposure)
    {
        this.maxExposure = maxExposure;
        setChanged();
    }

    public double getMinAmount()
    {
        return minAmount;
    }

    public void setMinAmount(double minAmountPerTransaction)
    {
        this.minAmount = minAmountPerTransaction;
        setChanged();
    }
}
