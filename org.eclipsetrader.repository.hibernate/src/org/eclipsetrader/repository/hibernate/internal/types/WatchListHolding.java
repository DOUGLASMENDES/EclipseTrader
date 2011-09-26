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

package org.eclipsetrader.repository.hibernate.internal.types;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.repository.hibernate.internal.stores.WatchListStore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "watchlists_elements")
public class WatchListHolding implements IHolding {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "instrument")
    @Target(SecurityType.class)
    private ISecurity security;

    @Column(name = "[position]")
    private Long position;

    @Column(name = "price")
    private Double purchasePrice;

    @Column(name = "date")
    private Date date;

    @Column(name = "index")
    @SuppressWarnings("unused")
    private int index;

    @ManyToOne
    @SuppressWarnings("unused")
    private WatchListStore watchlist;

    public WatchListHolding() {
    }

    public WatchListHolding(IHolding element, WatchListStore watchlist, int index) {
        this.security = element.getSecurity();
        this.position = element.getPosition();
        this.purchasePrice = element.getPurchasePrice();
        this.date = element.getDate();
        this.watchlist = watchlist;
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getPosition()
     */
    @Override
    public Long getPosition() {
        return position;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getPurchasePrice()
     */
    @Override
    public Double getPurchasePrice() {
        return purchasePrice;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (security != null) {
            if (security.getIdentifier() != null && adapter.isAssignableFrom(security.getIdentifier().getClass())) {
                return security;
            }
            if (adapter.isAssignableFrom(security.getClass())) {
                return security;
            }
        }
        return null;
    }
}
