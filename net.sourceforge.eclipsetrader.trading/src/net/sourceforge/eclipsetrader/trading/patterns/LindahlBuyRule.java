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

public class LindahlBuyRule implements IPattern
{
    List bars = new ArrayList();
    int minimumBars = 9;
    int maximumBars = 9;
    Sentiment sentiment = Sentiment.INVALID;

    public LindahlBuyRule()
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
                    sentiment = Sentiment.BULLISH;
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
