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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.TradingProvider;


public class Order extends PersistentObject
{
    public static final int SIDE_BUY = 1;
    public static final int SIDE_SELL = 2;
    public static final int SIDE_SELLSHORT = 3;
    public static final int SIDE_BUYCOVER = 4;
    public static final int TYPE_MARKET = 1;
    public static final int TYPE_LIMIT = 2;
    public static final int TYPE_STOP = 3;
    public static final int TYPE_STOPLIMIT = 4;
    public static final int STATUS_NEW = 0;
    public static final int STATUS_PARTIAL = 1;
    public static final int STATUS_FILLED = 2;
    public static final int STATUS_CANCELED = 3;
    public static final int STATUS_REJECTED = 4;
    public static final int STATUS_PENDING_CANCEL = 5;
    public static final int STATUS_PENDING_NEW = 6;
    String pluginId = "";
    TradingProvider provider;
    String exchange = "";
    String orderId = "";
    Date date = Calendar.getInstance().getTime();
    Security security;
    int side;
    int type;
    int quantity;
    double price;
    double stopPrice;
    int filledQuantity;
    double averagePrice;
    int status;
    Map params = new HashMap();

    public Order()
    {
    }

    public Order(Integer id)
    {
        super(id);
    }

    public String getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(String pluginId)
    {
        this.pluginId = pluginId;
    }

    public TradingProvider getProvider()
    {
        return provider;
    }

    public void setProvider(TradingProvider source)
    {
        this.provider = source;
    }

    public double getAveragePrice()
    {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice)
    {
        if (this.averagePrice != averagePrice)
            setChanged();
        this.averagePrice = averagePrice;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
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
        if (this.quantity != quantity)
            setChanged();
        this.quantity = quantity;
    }

    public int getFilledQuantity()
    {
        return filledQuantity;
    }

    public void setFilledQuantity(int filledQuantity)
    {
        if (this.filledQuantity != filledQuantity)
            setChanged();
        this.filledQuantity = filledQuantity;
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        this.security = security;
    }

    public int getSide()
    {
        return side;
    }

    public void setSide(int side)
    {
        this.side = side;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        if (this.status != status)
            setChanged();
        this.status = status;
    }

    public double getStopPrice()
    {
        return stopPrice;
    }

    public void setStopPrice(double stopPrice)
    {
        this.stopPrice = stopPrice;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getExchange()
    {
        return exchange;
    }

    public void setExchange(String exchange)
    {
        this.exchange = exchange;
    }

    public Map getParams()
    {
        return params;
    }

    public void setParams(Map params)
    {
        this.params = params;
    }
    
    public void cancelRequest()
    {
        if (provider != null)
            provider.sendCancelRequest(this);
        else
        {
            setStatus(STATUS_CANCELED);
            getRepository().save(this);
        }
    }
}
