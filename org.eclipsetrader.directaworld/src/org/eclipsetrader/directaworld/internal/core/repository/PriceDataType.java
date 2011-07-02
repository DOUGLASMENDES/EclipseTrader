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

package org.eclipsetrader.directaworld.internal.core.repository;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "prices")
@XmlType(name = "org.eclipsetrader.directaworld.PriceData")
public class PriceDataType {

    private Date time;
    private Double last;
    private Long lastSize;
    private Double bid;
    private Long bidSize;
    private Double ask;
    private Long askSize;
    private Long volume;
    private Double open;
    private Double high;
    private Double low;
    private Double close;

    public PriceDataType() {
    }

    @XmlAttribute(name = "time")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @XmlAttribute(name = "last")
    public Double getLast() {
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }

    @XmlAttribute(name = "last-size")
    public Long getLastSize() {
        return lastSize;
    }

    public void setLastSize(Long lastSize) {
        this.lastSize = lastSize;
    }

    @XmlAttribute(name = "bid")
    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    @XmlAttribute(name = "bid-size")
    public Long getBidSize() {
        return bidSize;
    }

    public void setBidSize(Long bidSize) {
        this.bidSize = bidSize;
    }

    @XmlAttribute(name = "ask")
    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }

    @XmlAttribute(name = "ask-size")
    public Long getAskSize() {
        return askSize;
    }

    public void setAskSize(Long askSize) {
        this.askSize = askSize;
    }

    @XmlAttribute(name = "volume")
    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    @XmlAttribute(name = "open")
    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    @XmlAttribute(name = "high")
    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    @XmlAttribute(name = "low")
    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    @XmlAttribute(name = "close")
    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }
}
