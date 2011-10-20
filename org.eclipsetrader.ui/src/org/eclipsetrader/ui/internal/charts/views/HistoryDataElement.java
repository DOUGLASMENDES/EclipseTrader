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

package org.eclipsetrader.ui.internal.charts.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;

public class HistoryDataElement {

    public static final String PROP_DATE = "date";
    public static final String PROP_OPEN = "open";
    public static final String PROP_HIGH = "high";
    public static final String PROP_LOW = "low";
    public static final String PROP_CLOSE = "close";
    public static final String PROP_VOLUME = "volume";

    private Date date;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public HistoryDataElement() {
    }

    public HistoryDataElement(IOHLC ohlc) {
        date = ohlc.getDate();
        open = ohlc.getOpen();
        high = ohlc.getHigh();
        low = ohlc.getLow();
        close = ohlc.getClose();
        volume = ohlc.getVolume();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        changeSupport.firePropertyChange(PROP_DATE, this.date, this.date = date);
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        changeSupport.firePropertyChange(PROP_OPEN, this.open, this.open = open);
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        changeSupport.firePropertyChange(PROP_HIGH, this.high, this.high = high);
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        changeSupport.firePropertyChange(PROP_LOW, this.low, this.low = low);
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        changeSupport.firePropertyChange(PROP_CLOSE, this.close, this.close = close);
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        changeSupport.firePropertyChange(PROP_VOLUME, this.volume, this.volume = volume);
    }

    public IOHLC toOHLC() {
        return new OHLC(date, open, high, low, close, volume);
    }

    public boolean isEmpty() {
        return date == null && open == null && high == null && low == null && close == null && volume == null;
    }

    public boolean isValid() {
        return date != null && open != null && high != null && low != null && close != null && volume != null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HistoryDataElement)) {
            return false;
        }
        HistoryDataElement other = (HistoryDataElement) obj;
        if ((date != null && !date.equals(other.date)) || date == null && other.date != null) {
            return false;
        }
        if ((open != null && !open.equals(other.open)) || open == null && other.open != null) {
            return false;
        }
        if ((high != null && !high.equals(other.high)) || high == null && other.high != null) {
            return false;
        }
        if ((low != null && !low.equals(other.low)) || low == null && other.low != null) {
            return false;
        }
        if ((close != null && !close.equals(other.close)) || close == null && other.close != null) {
            return false;
        }
        if ((volume != null && !volume.equals(other.volume)) || volume == null && other.volume != null) {
            return false;
        }
        return true;
    }

    public boolean equalsTo(IOHLC ohlc) {
        if ((date != null && !date.equals(ohlc.getDate())) || date == null && ohlc.getDate() != null) {
            return false;
        }
        if ((open != null && !open.equals(ohlc.getOpen())) || open == null && ohlc.getOpen() != null) {
            return false;
        }
        if ((high != null && !high.equals(ohlc.getHigh())) || high == null && ohlc.getHigh() != null) {
            return false;
        }
        if ((low != null && !low.equals(ohlc.getLow())) || low == null && ohlc.getLow() != null) {
            return false;
        }
        if ((close != null && !close.equals(ohlc.getClose())) || close == null && ohlc.getClose() != null) {
            return false;
        }
        if ((volume != null && !volume.equals(ohlc.getVolume())) || volume == null && ohlc.getVolume() != null) {
            return false;
        }
        return true;
    }
}
