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

package org.eclipsetrader.core.views;

import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Default implementation of the <code>IHolding</code> interface.
 * Clients may subclass.
 *
 * @since 1.0
 */
public class Holding implements IHolding {
	private ISecurity security;
	private Long position;
	private Double purchasePrice;
	private Date date;

	protected Holding() {
	}

	public Holding(ISecurity security) {
	    this(security, null, null, null);
    }

	public Holding(ISecurity security, Long position, Double purchasePrice) {
	    this(security, position, purchasePrice, null);
    }

	public Holding(ISecurity security, Long position, Double purchasePrice, Date date) {
	    this.security = security;
	    this.position = position;
	    this.purchasePrice = purchasePrice;
	    this.date = date;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IHolding#getSecurity()
	 */
	public ISecurity getSecurity() {
		return security;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IHolding#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IHolding#getPosition()
	 */
	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
    	this.position = position;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IHolding#getPurchasePrice()
	 */
	public Double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(Double purchasePrice) {
    	this.purchasePrice = purchasePrice;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (security != null) {
			if (security.getIdentifier() != null && adapter.isAssignableFrom(security.getIdentifier().getClass()))
				return security;
			if (adapter.isAssignableFrom(security.getClass()))
				return security;
		}
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
		return null;
	}
}
