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

package net.sourceforge.eclipsetrader.core;

public class Sentiment
{
    public static final Sentiment INVALID = new Sentiment(-1);
    public static final Sentiment NEUTRAL = new Sentiment(0);
    public static final Sentiment BULLISH = new Sentiment(1);
    public static final Sentiment BEARISH = new Sentiment(2);
    int ordinal;

    private Sentiment(int ordinal)
    {
        this.ordinal = ordinal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof Sentiment))
            return false;
        return ordinal == ((Sentiment)o).ordinal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return ordinal;
    }
}
