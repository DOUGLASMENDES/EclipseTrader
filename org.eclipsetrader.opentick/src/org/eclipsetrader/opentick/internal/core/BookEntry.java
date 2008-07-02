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

package org.eclipsetrader.opentick.internal.core;

import java.util.Date;

import org.eclipsetrader.core.feed.IBookEntry;
import org.otfeed.event.TradeSideEnum;

public class BookEntry implements IBookEntry, Comparable<BookEntry> {
	private String id;
	private Date time;
	private String marketMaker;
	private Double price;
	private Long quantity;
	private Long proposals;
	private TradeSideEnum side;

	public BookEntry(String id, Date time, double price, int quantity, TradeSideEnum side) {
	    this.id = id;
	    this.time = time;
	    this.price = price;
	    this.quantity = new Long(quantity);
	    this.proposals = 1L;
	    this.side = side;
    }

	public BookEntry(Double price, Long quantity, Long proposals, TradeSideEnum side) {
	    this.price = price;
	    this.quantity = quantity;
	    this.proposals = proposals;
	    this.side = side;
    }

	public String getId() {
    	return id;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBookEntry#getMarketMaker()
	 */
	public String getMarketMaker() {
		return marketMaker;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBookEntry#getPrice()
	 */
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
    	this.price = price;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBookEntry#getProposals()
	 */
	public Long getProposals() {
		return proposals;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBookEntry#getQuantity()
	 */
	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
    	this.quantity = new Long(quantity);
    }

	public void setQuantity(Long quantity) {
    	this.quantity = quantity;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBookEntry#getTime()
	 */
	public Date getTime() {
		return time;
	}

	public TradeSideEnum getSide() {
    	return side;
    }

	/* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(BookEntry o) {
    	if (side == TradeSideEnum.SELLER) {
        	if (price < o.price)
        		return -1;
        	else if (price > o.price)
        		return 1;
    	    return time != null && o.time != null ? time.compareTo(o.time) : 0;
    	}
    	else {
        	if (price < o.price)
        		return 1;
        	else if (price > o.price)
        		return -1;
    	    return o.time != null && time != null ? o.time.compareTo(time) : 0;
    	}
    }
}
