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

package org.eclipsetrader.repository.local.internal.types;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

@XmlRootElement(name = "day")
public class HistoryDayType {

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity security;

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date date;

    @XmlElementRef
    private Set<HistoryType> periods = new TreeSet<HistoryType>();

    HistoryDayType() {
    }

    public HistoryDayType(ISecurity security, Date date) {
        this.security = security;
        this.date = date;
    }

    public void addHistory(HistoryType historyType) {
        for (Iterator<HistoryType> iter = periods.iterator(); iter.hasNext();) {
            if (iter.next().getPeriod().equals(historyType.getPeriod())) {
                iter.remove();
            }
        }
        periods.add(historyType);
    }

    public void removeHistory(HistoryType historyType) {
        for (Iterator<HistoryType> iter = periods.iterator(); iter.hasNext();) {
            if (iter.next().getPeriod().equals(historyType.getPeriod())) {
                iter.remove();
            }
        }
    }

    public void removeHistory(TimeSpan timeSpan) {
        for (Iterator<HistoryType> iter = periods.iterator(); iter.hasNext();) {
            if (iter.next().getPeriod().equals(timeSpan)) {
                iter.remove();
            }
        }
    }

    public void removeAll() {
        periods.clear();
    }

    @XmlTransient
    public ISecurity getSecurity() {
        return security;
    }

    @XmlTransient
    public Date getDate() {
        return date;
    }

    @XmlTransient
    public HistoryType[] getPeriods() {
        return periods.toArray(new HistoryType[periods.size()]);
    }

    public HistoryType getPeriod(TimeSpan period) {
        for (HistoryType type : periods) {
            if (type.getPeriod() == period) {
                return type;
            }
        }
        return null;
    }
}
