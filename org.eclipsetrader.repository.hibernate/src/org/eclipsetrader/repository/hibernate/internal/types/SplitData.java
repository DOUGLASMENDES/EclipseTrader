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

import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "histories_splits")
public class SplitData implements ISplit {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "date")
    private Date date;

    @Column(name = "old_quantity")
    private Double oldQuantity;

    @Column(name = "new_quantity")
    private Double newQuantity;

    @ManyToOne
    @SuppressWarnings("unused")
    private HistoryStore history;

    public SplitData() {
    }

    public SplitData(HistoryStore history, ISplit split) {
        this.history = history;
        this.date = split.getDate();
        this.oldQuantity = split.getOldQuantity();
        this.newQuantity = split.getNewQuantity();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ISplit#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ISplit#getNewQuantity()
     */
    @Override
    public Double getNewQuantity() {
        return newQuantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ISplit#getOldQuantity()
     */
    @Override
    public Double getOldQuantity() {
        return oldQuantity;
    }
}
