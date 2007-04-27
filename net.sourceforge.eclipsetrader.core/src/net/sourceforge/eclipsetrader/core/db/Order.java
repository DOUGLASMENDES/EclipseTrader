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

package net.sourceforge.eclipsetrader.core.db;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.ITradingProvider;

public class Order extends PersistentObject
{
    String pluginId = "";
    ITradingProvider provider;
    Account account;
    OrderRoute exchange;
    String orderId = "";
    Date date = Calendar.getInstance().getTime();
    Security security;
    OrderSide side;
    OrderType type;
    int quantity;
    double price;
    double stopPrice;
    int filledQuantity;
    double averagePrice;
    Date expire;
    OrderValidity validity;
    OrderStatus status = OrderStatus.NEW;
    String text;
    String message;
    Map params = new HashMap();

    public Order()
    {
    }

    public Order(Integer id)
    {
        super(id);
    }

    /**
     * Returns the id of the trading provider plugin that
     * handles this order.
     * 
     * @return the plugin id
     */
    public String getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(String pluginId)
    {
        this.pluginId = pluginId;
    }

    public ITradingProvider getProvider()
    {
        return provider;
    }

    public void setProvider(ITradingProvider source)
    {
        this.provider = source;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
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

    public OrderSide getSide()
    {
        return side;
    }

    public void setSide(OrderSide side)
    {
        this.side = side;
    }

    public OrderStatus getStatus()
    {
        return status;
    }

    public void setStatus(OrderStatus status)
    {
        if (!this.status.equals(status))
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

    public OrderType getType()
    {
        return type;
    }

    public void setType(OrderType type)
    {
        this.type = type;
    }

    public OrderRoute getExchange()
    {
        return exchange;
    }

    public void setExchange(OrderRoute exchange)
    {
        this.exchange = exchange;
    }

    public Date getExpire()
    {
        return expire;
    }

    public void setExpire(Date expire)
    {
        this.expire = expire;
    }

    public OrderValidity getValidity()
    {
        return validity;
    }

    public void setValidity(OrderValidity validity)
    {
        this.validity = validity;
    }

    public Map getParams()
    {
        return params;
    }

    public void setParams(Map params)
    {
        this.params = params;
    }

    /**
     * Returns the user-defined text.
     * 
     * @return the text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Sets a user-defined defined text.
     * 
     * @param text - the text.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Returns the message set by the trading provider.
     * 
     * @return the text.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Allows the trading provider to set a message.
     * 
     * @oaram message - the text.
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public void sendNew()
    {
        if (provider == null)
            throw new RuntimeException("Invalid argument");
        provider.sendNew(this);
    }

    public void cancelRequest()
    {
        if (provider == null)
            throw new RuntimeException("Invalid argument");
        provider.sendCancelRequest(this);
    }

    public void replaceRequest()
    {
        if (provider == null)
            throw new RuntimeException("Invalid argument");
        provider.sendReplaceRequest(this);
    }
}
