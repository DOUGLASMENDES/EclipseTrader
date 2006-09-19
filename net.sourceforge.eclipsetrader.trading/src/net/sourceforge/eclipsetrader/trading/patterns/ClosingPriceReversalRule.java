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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.Sentiment;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

public class ClosingPriceReversalRule implements IPattern
{
    List bars = new ArrayList();
    int minimumBars = 3;
    int maximumBars = 3;
    Sentiment sentiment = Sentiment.INVALID;

    public ClosingPriceReversalRule()
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

            if (recs[1].getLow() < recs[0].getLow() // lower low
                    && recs[1].getClose() > recs[0].getClose()) //higher close
            {
                if (closesInTopQuarter(recs[2]))
                    sentiment = Sentiment.BULLISH;
            }
            else if (recs[1].getHigh() >= recs[0].getHigh() // higher high
                    && recs[1].getClose() <= recs[0].getClose()) // lower close
            {
                if (closesInBottomQuarter(recs[2]))
                    sentiment = Sentiment.BEARISH;
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

    private boolean closesInTopQuarter(Bar rec)
    {
        return rec.getClose() >= (rec.getLow() + (rec.getHigh() - rec.getLow()) * 3 / 4);
    }

    private boolean closesInBottomQuarter(Bar rec)
    {
        return rec.getClose() <= (rec.getLow() + (rec.getHigh() - rec.getLow()) / 4);
    }
}
