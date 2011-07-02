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

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "day")
@XmlType(name = "org.eclipsetrader.core.markets.MarketHoliday")
public class MarketHoliday implements Comparable<MarketHoliday> {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date date;

    @XmlValue
    private String description;

    @XmlAttribute(name = "open")
    @XmlJavaTypeAdapter(TimeAdapter.class)
    private Date openTime;

    @XmlAttribute(name = "close")
    @XmlJavaTypeAdapter(TimeAdapter.class)
    private Date closeTime;

    protected MarketHoliday() {
    }

    public MarketHoliday(Date date, String description) {
        this(date, description, null, null);
    }

    public MarketHoliday(Date openTime, Date closeTime, String description) {
        this(openTime, description, openTime, closeTime);
    }

    public MarketHoliday(Date date, String description, Date openTime, Date closeTime) {
        this.date = date;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public String getDescription() {
        return description;
    }

    @XmlTransient
    public Date getDate() {
        return date;
    }

    @XmlTransient
    public Date getOpenTime() {
        if (this.openTime == null) {
            return null;
        }
        Calendar today = Calendar.getInstance();
        if (this.date != null) {
            today.setTime(this.date);
        }
        Calendar openTime = Calendar.getInstance();
        openTime.setTime(this.openTime);
        openTime.set(Calendar.DATE, today.get(Calendar.DATE));
        openTime.set(Calendar.MONTH, today.get(Calendar.MONTH));
        openTime.set(Calendar.YEAR, today.get(Calendar.YEAR));
        openTime.set(Calendar.SECOND, 0);
        openTime.set(Calendar.MILLISECOND, 0);
        return openTime.getTime();
    }

    @XmlTransient
    public Date getCloseTime() {
        if (this.closeTime == null) {
            return null;
        }
        Calendar today = Calendar.getInstance();
        if (this.date != null) {
            today.setTime(this.date);
        }
        Calendar closeTime = Calendar.getInstance();
        closeTime.setTime(this.closeTime);
        closeTime.set(Calendar.DATE, today.get(Calendar.DATE));
        closeTime.set(Calendar.MONTH, today.get(Calendar.MONTH));
        closeTime.set(Calendar.YEAR, today.get(Calendar.YEAR));
        closeTime.set(Calendar.SECOND, 0);
        closeTime.set(Calendar.MILLISECOND, 0);
        return closeTime.getTime();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MarketHoliday o) {
        return date.compareTo(o.date);
    }
}
