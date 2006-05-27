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

package net.sourceforge.eclipsetrader.trading.alerts;

import java.text.NumberFormat;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Event;
import net.sourceforge.eclipsetrader.trading.AlertPlugin;

public class TargetPrice extends AlertPlugin
{
    public static final int LAST = 0;
    public static final int BID = 1;
    public static final int ASK = 2;
    private int field = LAST;
    private double price = 0;
    private boolean cross = false;
    private double initialValue = 0;
    private NumberFormat priceFormatter = NumberFormat.getInstance();

    public TargetPrice()
    {
        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(4);
        priceFormatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#init(java.util.Map)
     */
    public void init(Map params)
    {
        String value = (String)params.get("field");
        if (value != null)
            field = Integer.parseInt(value);
        value = (String)params.get("price");
        if (value != null)
            price = Double.parseDouble(value);
        value = (String)params.get("cross");
        if (value != null)
            cross = new Boolean(value).booleanValue();
        
        if (getSecurity().getQuote() != null)
        {
            switch(field)
            {
                case LAST:
                    initialValue = getSecurity().getQuote().getLast();
                    break;
                case BID:
                    initialValue = getSecurity().getQuote().getBid();
                    break;
                case ASK:
                    initialValue = getSecurity().getQuote().getAsk();
                    break;
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#getDescription()
     */
    public String getDescription()
    {
        String s = "Last";
        if (field == BID)
            s = "Bid";
        else if (field == ASK)
            s = "Ask";
        s += " price ";
        s += cross ? "crosses" : "reaches";
        return s + " " + priceFormatter.format(price); 
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#apply()
     */
    public boolean apply()
    {
        double value = 0;

        if (getSecurity().getQuote() == null)
            return false;

        switch(field)
        {
            case LAST:
                value = getSecurity().getQuote().getLast();
                break;
            case BID:
                value = getSecurity().getQuote().getBid();
                break;
            case ASK:
                value = getSecurity().getQuote().getAsk();
                break;
        }
        
        if (initialValue == 0)
            initialValue = value;
        
        boolean result = (price >= initialValue ? (value > price) : (value < price));
        if (!cross)
            result = (value == price);
        
        if (result)
        {
            Event event = new Event();
            event.setSecurity(getSecurity());
            if (!cross)
                event.setMessage("Target price " + priceFormatter.format(price) + " reached");
            else
                event.setMessage("Target price " + priceFormatter.format(price) + " crossed");
            CorePlugin.getRepository().save(event);
        }
        
        return result;
    }
}
