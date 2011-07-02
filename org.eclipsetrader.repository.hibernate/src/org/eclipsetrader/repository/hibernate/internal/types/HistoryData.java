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

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "histories_data")
public class HistoryData implements IOHLC {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "date")
    private Date date;

    @Column(name = "timespan", nullable = true)
    @Target(TimeSpanTypeAdapter.class)
    private TimeSpan timeSpan;

    @Column(name = "open_price")
    private Double open;

    @Column(name = "high_price")
    private Double high;

    @Column(name = "low_price")
    private Double low;

    @Column(name = "close_price")
    private Double close;

    @Column(name = "volume")
    private Long volume;

    @ManyToOne
    @Index(name = "history_id_fkey")
    @SuppressWarnings("unused")
    private HistoryStore history;

    public HistoryData() {
    }

    public HistoryData(HistoryStore history, IOHLC ohlc, TimeSpan timeSpan) {
        this.history = history;
        this.date = ohlc.getDate();
        this.open = ohlc.getOpen();
        this.high = ohlc.getHigh();
        this.low = ohlc.getLow();
        this.close = ohlc.getClose();
        this.volume = ohlc.getVolume();
        this.timeSpan = timeSpan;
    }

    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getOpen()
     */
    @Override
    public Double getOpen() {
        return open;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getHigh()
     */
    @Override
    public Double getHigh() {
        return high;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getLow()
     */
    @Override
    public Double getLow() {
        return low;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getClose()
     */
    @Override
    public Double getClose() {
        return close;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IOHLC#getVolume()
     */
    @Override
    public Long getVolume() {
        return volume;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IOHLC) {
            IOHLC other = (IOHLC) obj;
            return (getDate() == other.getDate() || getDate() != null && getDate().equals(other.getDate())) && (getOpen() == other.getOpen() || getOpen() != null && getOpen().equals(other.getOpen())) && (getHigh() == other.getHigh() || getHigh() != null && getHigh().equals(other.getHigh())) && (getLow() == other.getLow() || getLow() != null && getLow().equals(other.getLow())) && (getClose() == other.getClose() || getClose() != null && getClose().equals(other.getClose())) && (getVolume() == other.getVolume() || getVolume() != null && getVolume().equals(other.getVolume()));
        }
        if (obj instanceof HistoryData) {
            HistoryData other = (HistoryData) obj;
            return (getDate() == other.getDate() || getDate() != null && getDate().equals(other.getDate())) && (getOpen() == other.getOpen() || getOpen() != null && getOpen().equals(other.getOpen())) && (getHigh() == other.getHigh() || getHigh() != null && getHigh().equals(other.getHigh())) && (getLow() == other.getLow() || getLow() != null && getLow().equals(other.getLow())) && (getClose() == other.getClose() || getClose() != null && getClose().equals(other.getClose())) && (getVolume() == other.getVolume() || getVolume() != null && getVolume().equals(other.getVolume())) && (getTimeSpan() == other.getTimeSpan() || getTimeSpan() != null && getTimeSpan().equals(other.getTimeSpan()));
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (date != null ? date.hashCode() : 0) + 7 * (open != null ? open.hashCode() : 0) + 11 * (high != null ? high.hashCode() : 0) + 13 * (low != null ? low.hashCode() : 0) + 17 * (close != null ? close.hashCode() : 0) + 19 * (volume != null ? volume.hashCode() : 0) + 37 * (timeSpan != null ? timeSpan.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + date + " O=" + open + " H=" + high + " L=" + low + " C=" + close + " V=" + volume + "]";
    }
}
