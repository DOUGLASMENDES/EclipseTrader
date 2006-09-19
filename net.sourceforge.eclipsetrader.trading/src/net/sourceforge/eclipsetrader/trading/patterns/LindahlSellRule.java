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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.Sentiment;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

public class LindahlSellRule implements IPattern
{
    List bars = new ArrayList();
    int minimumBars = 9;
    int maximumBars = 9;
    Sentiment sentiment = Sentiment.INVALID;

    public LindahlSellRule()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#init(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void init(Security security)
    {
        bars = new ArrayList();
        sentiment = Sentiment.INVALID;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#add(net.sourceforge.eclipsetrader.core.db.Bar)
     */
    public void add(Bar bar)
    {
        bars.add(bar);
        if (bars.size() > maximumBars)
            bars.remove(0);

        if (bars.size() >= minimumBars)
        {
            sentiment = Sentiment.NEUTRAL;
            Bar[] recs = (Bar[])bars.toArray(new Bar[bars.size()]);

            int a = 0, b = 1, d = 1, e = 1;

            double ahigh = recs[a].getHigh();
            int size = Math.min(8, recs.length);

            // a must be the absolute high
            // b must take out the low of a
            while (b < size)
            {
                if (recs[a].getLow() > recs[b].getLow() && recs[b].getHigh() < ahigh)
                    break;
                ++b;
            }

            // d must take out the high the preceeding bar
            d = b + 1; // start at b
            while (d < size)
            {
                if (recs[d].getHigh() < ahigh && recs[d - 1].getHigh() < ahigh && recs[d - 1].getHigh() < recs[d].getHigh())
                    break;
                ++d;
            }

            // e must take out the low of the preceeding bar
            // and close below the previous bar's close
            // and close below its own open price

            e = d + 1;

            while (e < size)
            {
                Bar rece = recs[e];
                Bar recf = recs[e - 1];
                if (rece.getLow() < recf.getLow() // take low of prev bare
                        && rece.getClose() < recf.getClose() // close below prev bar
                        && rece.getClose() < rece.getOpen() // close down
                        && rece.getHigh() < ahigh && recf.getHigh() < ahigh)
                {
                    sentiment = Sentiment.BEARISH;
                    return;
                }
                ++e;
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IPattern#getSentiment()
     */
    public Sentiment getSentiment()
    {
        return sentiment;
    }
}
