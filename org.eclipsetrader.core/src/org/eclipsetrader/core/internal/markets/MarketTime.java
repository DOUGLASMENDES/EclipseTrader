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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "time")
@XmlType(name = "org.eclipsetrader.core.markets.MarketTime")
public class MarketTime implements Comparable<MarketTime> {

    @XmlAttribute(name = "description")
    private String description;

    @XmlAttribute(name = "close")
    @XmlJavaTypeAdapter(TimeAdapter.class)
    private Date closeTime;

    @XmlAttribute(name = "open")
    @XmlJavaTypeAdapter(TimeAdapter.class)
    private Date openTime;

    @XmlElementRef
    private SortedSet<MarketTimeExclude> exclude;

    protected MarketTime() {
    }

    public MarketTime(Date openTime, Date closeTime) {
        this(openTime, closeTime, null);
    }

    public MarketTime(Date openTime, Date closeTime, String description) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
    }

    @XmlTransient
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    @XmlTransient
    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    @XmlTransient
    public MarketTimeExclude[] getExclude() {
        if (exclude == null) {
            return new MarketTimeExclude[0];
        }
        return exclude.toArray(new MarketTimeExclude[exclude.size()]);
    }

    public void setExclude(MarketTimeExclude[] exclude) {
        this.exclude = exclude != null && exclude.length != 0 ? new TreeSet<MarketTimeExclude>(Arrays.asList(exclude)) : null;
    }

    public boolean isExcluded(Date date) {
        if (exclude != null) {
            Calendar day = Calendar.getInstance();
            day.setTime(date);
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
            date = day.getTime();

            for (MarketTimeExclude excludeDay : exclude) {
                if (date.equals(excludeDay.getFromDate()) || date.equals(excludeDay.getToDate())) {
                    return true;
                }
                if (date.after(excludeDay.getFromDate()) && date.before(excludeDay.getToDate())) {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MarketTime o) {
        return openTime.compareTo(o.getOpenTime());
    }
}
