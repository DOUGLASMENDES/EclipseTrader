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

package net.sourceforge.eclipsetrader.trading.patterns;

import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.db.Bar;

public class LindahlBuyRule implements IPattern
{
    private int complete = 0;
    private boolean bullish = false;

    public LindahlBuyRule()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#applies(net.sourceforge.eclipsetrader.core.db.Bar[])
     */
    public boolean applies(Bar[] recs)
    {
        bullish = true;

        if (recs.length < 3)
            return false;

        int a = 0, b = 1, d = 1, e = 1;

        // a must be the absolute low
        while (b < 9 && b < recs.length)
        {
            if (recs[a].getHigh() < recs[b].getHigh() && recs[b].getLow() > recs[a].getLow())
                break;
            ++b;
        }

        // d must take out the low the preceeding bar
        d = b + 1; // start at b
        while (d < 9 && d < recs.length)
        {
            if (recs[d - 1].getLow() > recs[d].getLow() && recs[d].getLow() > recs[a].getLow() && recs[d - 1].getLow() > recs[a].getLow())
                break;
            ++d;
        }

        // e must take out the high of the preceeding bar
        // and close above the previous bar's close
        // and close above its own open price

        e = d + 1;

        while (e < 9 && e < recs.length)
        {
            if (recs[e].getHigh() > recs[e - 1].getHigh() && recs[e].getClose() > recs[e - 1].getClose() && recs[e].getClose() > recs[e].getOpen() && recs[e].getLow() > recs[a].getLow() && recs[e - 1].getLow() > recs[a].getLow())
            {
                complete = e;
                return true;
            }
            ++e;
        } 

        return false;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#isBullish()
     */
    public boolean isBullish()
    {
        return bullish;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#getComplete()
     */
    public int getComplete()
    {
        return complete;
    }
}
