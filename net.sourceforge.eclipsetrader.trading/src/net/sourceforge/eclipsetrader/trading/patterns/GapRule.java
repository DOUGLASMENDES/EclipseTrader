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

public class GapRule implements IPattern
{
    private int complete = 0;
    private boolean bullish = false;

    public GapRule()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#applies(net.sourceforge.eclipsetrader.core.db.Bar[])
     */
    public boolean applies(Bar[] recs)
    {
        if (recs.length <= 3)
            return false;

        if (recs[0].getHigh() < recs[1].getLow() // gap up
                && closesInTopQuarter(recs[1]) && closesInTopQuarter(recs[2]))
        {
            complete = 2;
            bullish = true;
            return true;
        }
        else if (recs[1].getHigh() < recs[0].getLow() // gap down
                && closesInBottomQuarter(recs[1]) && closesInBottomQuarter(recs[2]))
        {
            complete = 2;
            bullish = false;
            return true;
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

    private boolean closesInTopQuarter(Bar rec)
    {
        return rec.getClose() >= (rec.getLow() + (rec.getHigh() - rec.getLow()) * 3 / 4);
    }

    private boolean closesInBottomQuarter(Bar rec)
    {
        return rec.getClose() <= (rec.getLow() + (rec.getHigh() - rec.getLow()) / 4);
    }
}
