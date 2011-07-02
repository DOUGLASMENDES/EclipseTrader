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

public class LastClose implements ILastClose, Serializable {

    private static final long serialVersionUID = -5423072173577093589L;

    private Double price;
    private Date date;

    public LastClose(Double price, Date date) {
        this.price = price;
        this.date = date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ILastClose#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ILastClose#getPrice()
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
        if (!(obj instanceof ILastClose)) {
            return false;
        }
        ILastClose other = (ILastClose) obj;
        return (getDate() == other.getDate() || getDate() != null && getDate().equals(other.getDate())) && (getPrice() == other.getPrice() || getPrice() != null && getPrice().equals(other.getPrice()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (date != null ? date.hashCode() : 0) + 7 * (price != null ? price.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[LastClose: " + date + " P=" + price + "]";
    }
}
