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
import java.util.Date;

/**
 * Default implementation of the <code>IBarOpen</code> interface.
 *
 * @since 1.0
 * @see org.eclipsetrader.core.feed.IBarOpen
 */
public class BarOpen implements IBarOpen, Serializable {

    private static final long serialVersionUID = -2597107324979019758L;

    private Date date;
    private TimeSpan timeSpan;
    private Double open;

    public BarOpen(Date date, TimeSpan timeSpan, Double open) {
        this.date = date;
        this.timeSpan = timeSpan;
        this.open = open;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getTimeSpan()
     */
    @Override
    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getOpen()
     */
    @Override
    public Double getOpen() {
        return open;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IBarOpen)) {
            return false;
        }
        IBarOpen other = (IBarOpen) obj;
        return (getDate() == other.getDate() || getDate() != null && getDate().equals(other.getDate())) && (getTimeSpan() == other.getTimeSpan() || getTimeSpan() != null && getTimeSpan().equals(other.getTimeSpan())) && (getOpen() == other.getOpen() || getOpen() != null && getOpen().equals(other.getOpen()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (date != null ? date.hashCode() : 0) + 7 * (timeSpan != null ? timeSpan.hashCode() : 0) + 11 * (open != null ? open.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[BarOpen: " + date + " TS=" + timeSpan.toString() + " O=" + open + "]";
    }
}
