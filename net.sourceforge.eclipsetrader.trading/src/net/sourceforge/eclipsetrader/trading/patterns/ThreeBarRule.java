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

public class ThreeBarRule implements IPattern
{
    private int complete = 0;
    private boolean bullish = false;

    public ThreeBarRule()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.patterns.PatternPlugin#applies(net.sourceforge.eclipsetrader.core.db.Bar[])
     */
    public boolean applies(Bar[] recs)
    {
        // On completion of two consecutive bars in which
        // the price closes in the upper half of the range and the next bar closes
        // in the top 25 percent of its range
        if (recs.length < 3)
            return false;

        if (recs[0].getClose() >= (recs[0].getLow() + (recs[0].getHigh() - recs[0].getLow()) / 2) && recs[1].getClose() >= (recs[1].getLow() + (recs[1].getHigh() - recs[1].getLow()) / 2) && closesInTopQuarter(recs[2]))
        {
            complete = 2;
            bullish = true;
            return true;
        }
        else if (recs[0].getClose() <= (recs[0].getLow() + (recs[0].getHigh() - recs[0].getLow()) / 2) && recs[1].getClose() <= (recs[1].getLow() + (recs[1].getHigh() - recs[1].getLow()) / 2) && recs[2].getClose() <= (recs[2].getLow() + (recs[2].getHigh() - recs[2].getLow()) / 4))
        {
            complete = 2;
            bullish = false;
            return true;
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
}
