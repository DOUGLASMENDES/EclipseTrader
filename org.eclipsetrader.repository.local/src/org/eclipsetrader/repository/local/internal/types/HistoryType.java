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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

@XmlRootElement(name = "history")
public class HistoryType implements Comparable<HistoryType> {

    @XmlTransient
    private ISecurity security;

    @XmlAttribute(name = "period")
    @XmlJavaTypeAdapter(TimeSpanAdapter.class)
    private TimeSpan period;

    @XmlElementRef
    @XmlJavaTypeAdapter(OHLCAdapter.class)
    List<IOHLC> data = new ArrayList<IOHLC>();

    @XmlElementRef
    @XmlJavaTypeAdapter(SplitAdapter.class)
    List<ISplit> splits = new ArrayList<ISplit>();

    public HistoryType() {
    }

    public HistoryType(ISecurity security, IOHLC[] data) {
        this(security, data, null, null);
    }

    public HistoryType(ISecurity security, IOHLC[] data, TimeSpan period) {
        this(security, data, null, period);
    }

    public HistoryType(ISecurity security, IOHLC[] data, ISplit[] splits, TimeSpan period) {
        this.security = security;
        if (data != null) {
            this.data = new ArrayList<IOHLC>(Arrays.asList(data));
        }
        if (splits != null) {
            this.splits = new ArrayList<ISplit>(Arrays.asList(splits));
        }
        this.period = period;
    }

    @XmlTransient
    public List<IOHLC> getData() {
        return data;
    }

    public IOHLC[] toArray() {
        return data.toArray(new IOHLC[data.size()]);
    }

    public ISecurity getSecurity() {
        return security;
    }

    public TimeSpan getPeriod() {
        return period;
    }

    public ISplit[] getSplits() {
        return splits.toArray(new ISplit[splits.size()]);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(HistoryType o) {
        if (period.getUnits() == o.period.getUnits()) {
            return period.getLength() - o.period.getLength();
        }
        return period.getUnits().ordinal() - o.period.getUnits().ordinal();
    }
}
