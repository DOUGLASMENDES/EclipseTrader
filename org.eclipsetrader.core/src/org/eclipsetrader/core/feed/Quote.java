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
 * Default implementation of the <code>IQuote</code> interface.
 *
 * @since 1.0
 * @see org.eclipsetrader.core.feed.IQuote
 */
public class Quote implements IQuote, Serializable {

    private static final long serialVersionUID = 4237311627623138246L;

    private Double bid;
    private Double ask;
    private Long bidSize;
    private Long askSize;

    public Quote(Double bid, Double ask, Long bidSize, Long askSize) {
        this.bid = bid;
        this.ask = ask;
        this.bidSize = bidSize;
        this.askSize = askSize;
    }

    public Quote(Double bid, Double ask) {
        this.bid = bid;
        this.ask = ask;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IQuote#getAsk()
     */
    @Override
    public Double getAsk() {
        return ask;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IQuote#getAskSize()
     */
    @Override
    public Long getAskSize() {
        return askSize;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IQuote#getBid()
     */
    @Override
    public Double getBid() {
        return bid;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IQuote#getBidSize()
     */
    @Override
    public Long getBidSize() {
        return bidSize;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IQuote)) {
            return false;
        }
        IQuote other = (IQuote) obj;
        return equals(getBid(), other.getBid()) && equals(getAsk(), other.getAsk()) && equals(getBidSize(), other.getBidSize()) && equals(getAskSize(), other.getAskSize());
    }

    protected boolean equals(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (bid != null ? bid.hashCode() : 0) + 7 * (ask != null ? ask.hashCode() : 0) + 11 * (bidSize != null ? bidSize.hashCode() : 0) + 13 * (askSize != null ? askSize.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[Quote:" + " B=" + bid + " BS=" + bidSize + " A=" + ask + " AS=" + askSize + "]";
    }
}
