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

package org.eclipsetrader.core.internal.markets;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "exclude")
@XmlType(name = "org.eclipsetrader.core.markets.MarketTimeExclude")
public class MarketTimeExclude implements Comparable<MarketTimeExclude> {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date date;

    @XmlAttribute(name = "from")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date fromDate;

    @XmlAttribute(name = "to")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date toDate;

    protected MarketTimeExclude() {
    }

    public MarketTimeExclude(Date date) {
        this.date = date;
    }

    public MarketTimeExclude(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @XmlTransient
    public Date getFromDate() {
        return date != null ? date : fromDate;
    }

    @XmlTransient
    public Date getToDate() {
        return date != null ? date : toDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MarketTimeExclude o) {
        Date d1 = date != null ? date : fromDate;
        Date d2 = o.date != null ? o.date : o.fromDate;
        return d1.compareTo(d2);
    }
}
