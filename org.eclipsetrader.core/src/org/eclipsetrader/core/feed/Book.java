/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.feed;

/**
 * Default implementation of the the <code>IBook</code> interface.
 *
 * @since 1.0
 */
public class Book implements IBook {
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
	public IBookEntry[] getBidProposals() {
		return bid;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBook#getAskProposals()
	 */
	public IBookEntry[] getAskProposals() {
		return ask;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof IBook))
    		return false;
	    return super.equals(obj);
    }

    protected boolean equals(IBookEntry[] oldEntries, IBookEntry[] newEntries) {
    	if (oldEntries == newEntries)
    		return true;
    	if ((oldEntries == null && newEntries != null) || (oldEntries != null && newEntries == null))
    		return false;
    	if (oldEntries.length != newEntries.length)
    		return false;
    	for (int i = 0; i < oldEntries.length; i++) {
    		if (!oldEntries[i].equals(newEntries[i]))
    			return false;
    	}
    	return true;
    }
}
