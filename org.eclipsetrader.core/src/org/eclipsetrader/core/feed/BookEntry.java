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
 * Default implementation of the the <code>IBookEntry</code> interface.
 *
 * @since 1.0
 */
public class BookEntry implements IBookEntry, Serializable {

    private static final long serialVersionUID = -1219296257841084810L;

    private Date time;
    private Double price;
    private Long quantity;
    private Long proposals;
    private String marketMaker;

    protected BookEntry() {
    }

    public BookEntry(Date time, Double price, Long quantity, Long proposals, String marketMaker) {
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.proposals = proposals;
        this.marketMaker = marketMaker;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBookEntry#getMarketMaker()
     */
    @Override
    public String getMarketMaker() {
        return marketMaker;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBookEntry#getPrice()
     */
    @Override
    public Double getPrice() {
        return price;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBookEntry#getProposals()
     */
    @Override
    public Long getProposals() {
        return proposals;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBookEntry#getQuantity()
     */
    @Override
    public Long getQuantity() {
        return quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBookEntry#getTime()
     */
    @Override
    public Date getTime() {
        return time;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (marketMaker != null ? marketMaker.hashCode() : 0) + 7 * (price != null ? price.hashCode() : 0) + 11 * (proposals != null ? proposals.hashCode() : 0) + 13 * (quantity != null ? quantity.hashCode() : 0) + 17 * (time != null ? time.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IBookEntry)) {
            return false;
        }
        IBookEntry other = (IBookEntry) obj;
        return (marketMaker == other.getMarketMaker() || marketMaker != null && marketMaker.equals(other.getMarketMaker())) && (price == other.getPrice() || price != null && price.equals(other.getPrice())) && (proposals == other.getProposals() || proposals != null && proposals.equals(other.getProposals())) && (quantity == other.getQuantity() || quantity != null && quantity.equals(other.getQuantity())) && (time == other.getTime() || time != null && time.equals(other.getTime()));
    }
}
