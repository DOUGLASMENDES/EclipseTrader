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
import java.util.Arrays;

/**
 * Default implementation of the the <code>IBook</code> interface.
 *
 * @since 1.0
 */
public class Book implements IBook, Serializable {

    private static final long serialVersionUID = -5987580457341623002L;

    private IBookEntry[] bid;
    private IBookEntry[] ask;

    protected Book() {
    }

    public Book(IBookEntry[] bid, IBookEntry[] ask) {
        this.bid = bid;
        this.ask = ask;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBook#getBidProposals()
     */
    @Override
    public IBookEntry[] getBidProposals() {
        return bid;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBook#getAskProposals()
     */
    @Override
    public IBookEntry[] getAskProposals() {
        return ask;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IBook)) {
            return false;
        }
        IBook other = (IBook) obj;
        if (!Arrays.equals(bid, other.getBidProposals())) {
            return false;
        }
        if (!Arrays.equals(ask, other.getAskProposals())) {
            return false;
        }
        return true;
    }
}
