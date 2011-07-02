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

import java.io.Serializable;

/**
 * Holds today's OHL values (open, high and low prices).
 *
 * @since 1.0
 * @see org.eclipsetrader.core.feed.ITodayOHL
 */
public class TodayOHL implements ITodayOHL, Serializable {

    private static final long serialVersionUID = -1991345879969731137L;

    private Double open;
    private Double high;
    private Double low;

    public TodayOHL(Double open, Double high, Double low) {
        this.open = open;
        this.high = high;
        this.low = low;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ITodayOHL#getOpen()
     */
    @Override
    public Double getOpen() {
        return open;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ITodayOHL#getHigh()
     */
    @Override
    public Double getHigh() {
        return high;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ITodayOHL#getLow()
     */
    @Override
    public Double getLow() {
        return low;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ITodayOHL)) {
            return false;
        }
        ITodayOHL other = (ITodayOHL) obj;
        return (getOpen() == other.getOpen() || getOpen() != null && getOpen().equals(other.getOpen())) && (getHigh() == other.getHigh() || getHigh() != null && getHigh().equals(other.getHigh())) && (getLow() == other.getLow() || getLow() != null && getLow().equals(other.getLow()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (open != null ? open.hashCode() : 0) + 7 * (high != null ? high.hashCode() : 0) + 11 * (low != null ? low.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[OHL:" + " O=" + open + " H=" + high + " L=" + low + "]";
    }
}
