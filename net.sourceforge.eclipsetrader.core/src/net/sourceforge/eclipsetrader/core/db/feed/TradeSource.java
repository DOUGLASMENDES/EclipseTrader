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

package net.sourceforge.eclipsetrader.core.db.feed;

public class TradeSource
{
    String tradingProviderId = ""; //$NON-NLS-1$
    String exchange = ""; //$NON-NLS-1$
    String symbol = ""; //$NON-NLS-1$
    Integer accountId;
    int quantity = 1;
    
    public TradeSource()
    {
    }
    
    public String getTradingProviderId()
    {
        return tradingProviderId;
    }
    
    public void setTradingProviderId(String id)
    {
        this.tradingProviderId = id;
    }
    
    public String getExchange()
    {
        return exchange;
    }

    public void setExchange(String exchange)
    {
        this.exchange = (exchange != null) ? exchange : ""; //$NON-NLS-1$
    }

    public String getSymbol()
    {
        return symbol;
    }
    
    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public Integer getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Integer accountId)
    {
        this.accountId = accountId;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof TradeSource))
            return false;
        TradeSource that = (TradeSource)obj;
        return this.tradingProviderId.equals(that.tradingProviderId) && 
            this.symbol.equals(that.symbol) && 
            this.exchange.equals(that.exchange) && 
            (this.accountId != null && this.accountId.equals(that.accountId));
    }
}
