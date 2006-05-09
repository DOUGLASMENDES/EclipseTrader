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

public class HighLowReversal implements IPattern
{
    private int complete = 0;
    private boolean bullish = false;
    private double difference = 0.05;

    public HighLowReversal()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#applies(net.sourceforge.eclipsetrader.core.db.Bar[])
     */
    public boolean applies(Bar[] recs)
    {
        if (recs.length < 2)
            return false;

        complete = 1; // always 2 bars

        if (recs[0].getClose() >= recs[0].getHigh() - difference && recs[1].getClose() <= recs[1].getLow() + difference)
        {
            bullish = false;
            return true;
        }
        else if (recs[1].getClose() >= recs[1].getHigh() - difference && recs[0].getClose() <= recs[0].getLow() + difference)
        {
            bullish = true;
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
}
