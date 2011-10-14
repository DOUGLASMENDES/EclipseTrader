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

package org.eclipsetrader.core.ats;

import java.util.Date;

import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

public class BarFactoryEvent {

    public Date date;

    public Double open;
    public Double high;
    public Double low;
    public Double close;
    public Long volume;

    public ISecurity security;
    public TimeSpan timeSpan;
    public IBarFactory factory;

    public BarFactoryEvent() {
    }

    public BarFactoryEvent(ISecurity security, TimeSpan timeSpan, Date date, Double open) {
        this.security = security;
        this.timeSpan = timeSpan;
        this.date = date;
        this.open = open;
    }

    public BarFactoryEvent(ISecurity security, TimeSpan timeSpan, Date date, Double open, Double high, Double low, Double close, Long volume) {
        this.security = security;
        this.timeSpan = timeSpan;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[BarFactoryEvent:");
        sb.append(" instrument=" + security.getName());
        sb.append(" date=" + date);
        if (open != null) {
            sb.append(" open=" + open);
        }
        if (high != null) {
            sb.append(" high=" + high);
        }
        if (low != null) {
            sb.append(" low=" + low);
        }
        if (close != null) {
            sb.append(" close=" + close);
        }
        if (volume != null) {
            sb.append(" volume=" + volume);
        }
        return sb.toString();
    }
}
