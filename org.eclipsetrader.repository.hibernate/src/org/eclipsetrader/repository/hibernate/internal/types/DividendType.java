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

import org.eclipsetrader.core.feed.Dividend;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.repository.hibernate.internal.stores.SecurityStore;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "securities_dividends")
public class DividendType {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "ex_date")
    private Date exDate;

    @Column(name = "value")
    private Double value;

    @ManyToOne
    @SuppressWarnings("unused")
    private SecurityStore security;

    public DividendType() {
    }

    public DividendType(SecurityStore security, IDividend dividend) {
        this.security = security;
        this.exDate = dividend.getExDate();
        this.value = dividend.getValue();
    }

    public IDividend getDividend() {
        return new Dividend(exDate, value);
    }

    public Date getExDate() {
        return exDate;
    }
}
