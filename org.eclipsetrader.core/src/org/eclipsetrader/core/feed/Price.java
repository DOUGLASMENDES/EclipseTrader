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
 * Implementation of the <code>IPrice</code> interface.
 *
 * @since 1.0
 * @see org.eclipsetrader.core.feed.IPrice
 */
public class Price implements IPrice, Serializable {

    private static final long serialVersionUID = -2797699531211443331L;

    private Date time;
    private Double price;

    public Price(Date time, Double price) {
        this.time = time;
        this.price = price;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ITrade#getTime()
     */
    @Override
    public Date getTime() {
        return time;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ITrade#getPrice()
     */
    @Override
    public Double getPrice() {
        return price;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IPrice)) {
            return false;
        }
        IPrice other = (IPrice) obj;
        return (getTime() == other.getTime() || getTime() != null && getTime().equals(other.getTime())) && (getPrice() == other.getPrice() || getPrice() != null && getPrice().equals(other.getPrice()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (time != null ? time.hashCode() : 0) + 7 * (price != null ? price.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Price [" + time + " P=" + price + "]";
    }
}
