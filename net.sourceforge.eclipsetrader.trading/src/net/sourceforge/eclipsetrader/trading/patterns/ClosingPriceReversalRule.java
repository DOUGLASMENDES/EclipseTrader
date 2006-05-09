/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Williamson <mswilliamson@uwaterloo.ca> - original AST implementation
 *     Marco Maccaferri - porting to EclipseTrader
 */

package net.sourceforge.eclipsetrader.trading.patterns;

import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.db.Bar;

public class ClosingPriceReversalRule implements IPattern
{
    private int complete = 0;
    private boolean bullish = false;

    public ClosingPriceReversalRule()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#applies(net.sourceforge.eclipsetrader.core.db.Bar[])
     */
    public boolean applies(Bar[] recs)
    {
        if (recs.length <= 3)
            return false;

        if (recs[1].getLow() < recs[0].getLow() // lower low
                && recs[1].getClose() > recs[0].getClose()) //higher close
        {
            int n = findGoodUpClose(recs.length, recs);
            complete = n;
            bullish = true;
            return n != -1;
        }
        else if (recs[1].getHigh() >= recs[0].getHigh() // higher high
                && recs[1].getClose() <= recs[0].getClose()) // lower close
        {
            int n = findGoodDownClose(recs.length, recs);
            complete = n;
            bullish = false;
            return n != -1;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#getComplete()
     */
    public int getComplete()
    {
        return complete;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#isBullish()
     */
    public boolean isBullish()
    {
        return bullish;
    }

    private boolean closesInTopQuarter(Bar rec)
    {
        return rec.getClose() >= (rec.getLow() + (rec.getHigh() - rec.getLow()) * 3 / 4);
    }

    private boolean closesInBottomQuarter(Bar rec)
    {
        return rec.getClose() <= (rec.getLow() + (rec.getHigh() - rec.getLow()) / 4);
    }

    private int findGoodUpClose(int size, Bar[] recs)
    {
        if (size >= 3 && closesInTopQuarter(recs[2]))
            return 2;
        else if (size >= 4 && closesInTopQuarter(recs[3]))
            return 3;
        else if (size >= 5 && closesInTopQuarter(recs[4]))
            return 4;
        return -1;
    }

    private int findGoodDownClose(int size, Bar[] recs)
    {
        if (size >= 3 && closesInBottomQuarter(recs[2]))
            return 2;
        else if (size >= 4 && closesInBottomQuarter(recs[3]))
            return 3;
        else if (size >= 5 && closesInBottomQuarter(recs[4]))
            return 4;
        return -1;
    }
}
