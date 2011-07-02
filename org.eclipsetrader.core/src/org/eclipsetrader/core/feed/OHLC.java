/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.feed;

import java.util.Date;

/**
 * Default implementation of the <code>IOHLC</code> interface.
 *
 * @since 1.0
 * @see org.eclipsetrader.core.feed.IOHLC
 */
public class OHLC implements IOHLC {

    private Date date;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;

    public OHLC(Date date, Double open, Double high, Double low, Double close, Long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getOpen()
     */
    @Override
    public Double getOpen() {
        return open;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getHigh()
     */
    @Override
    public Double getHigh() {
        return high;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getLow()
     */
    @Override
    public Double getLow() {
        return low;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getClose()
     */
    @Override
    public Double getClose() {
        return close;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getVolume()
     */
    @Override
    public Long getVolume() {
        return volume;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IOHLC)) {
            return false;
        }
        IOHLC other = (IOHLC) obj;
        return (getDate() == other.getDate() || getDate() != null && getDate().equals(other.getDate())) && (getOpen() == other.getOpen() || getOpen() != null && getOpen().equals(other.getOpen())) && (getHigh() == other.getHigh() || getHigh() != null && getHigh().equals(other.getHigh())) && (getLow() == other.getLow() || getLow() != null && getLow().equals(other.getLow())) && (getClose() == other.getClose() || getClose() != null && getClose().equals(other.getClose())) && (getVolume() == other.getVolume() || getVolume() != null && getVolume().equals(other.getVolume()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (date != null ? date.hashCode() : 0) + 7 * (open != null ? open.hashCode() : 0) + 11 * (high != null ? high.hashCode() : 0) + 13 * (low != null ? low.hashCode() : 0) + 17 * (close != null ? close.hashCode() : 0) + 19 * (volume != null ? volume.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + date + " O=" + open + " H=" + high + " L=" + low + " C=" + close + " V=" + volume + "]";
    }
}
