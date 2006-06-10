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

import net.sourceforge.eclipsetrader.trading.AlertPlugin;

public class PriceChange extends AlertPlugin
{
    public static final int LAST = 0;
    public static final int BID = 1;
    public static final int ASK = 2;
    private int field = LAST;
    private double change = 0;
    private double reference = 0;
    private NumberFormat priceFormatter = NumberFormat.getInstance();
    private NumberFormat percentFormatter = NumberFormat.getInstance();

    public PriceChange()
    {
        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(4);
        priceFormatter.setMaximumFractionDigits(4);

        percentFormatter.setGroupingUsed(true);
        percentFormatter.setMinimumIntegerDigits(1);
        percentFormatter.setMinimumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#init(java.util.Map)
     */
    public void init(Map params)
    {
        String value = (String)params.get("field");
        if (value != null)
            field = Integer.parseInt(value);
        value = (String)params.get("reference");
        if (value != null)
            reference = Double.parseDouble(value);
        value = (String)params.get("change");
        if (value != null)
            change = Double.parseDouble(value);
        
        if (getSecurity().getQuote() != null)
        {
            switch(field)
            {
                case LAST:
                    reference = getSecurity().getQuote().getLast();
                    break;
                case BID:
                    reference = getSecurity().getQuote().getBid();
                    break;
                case ASK:
                    reference = getSecurity().getQuote().getAsk();
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
        return s + " price changes by " + percentFormatter.format(change) + "% from " + priceFormatter.format(reference); 
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
        
        if (reference == 0)
            reference = value;

        double percent = Math.abs(value - reference) / reference * 100.0; 
        boolean result = percent >= change;
        
        if (result)
            fireEvent("Price change of " + percentFormatter.format(change) + "% at " + priceFormatter.format(value));
        
        return result;
    }
}
