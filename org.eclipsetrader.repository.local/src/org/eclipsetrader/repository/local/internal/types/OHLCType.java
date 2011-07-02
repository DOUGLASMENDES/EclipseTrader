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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;

@XmlRootElement(name = "bar")
public class OHLCType {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    @XmlAttribute(name = "open")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double open;

    @XmlAttribute(name = "high")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double high;

    @XmlAttribute(name = "low")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double low;

    @XmlAttribute(name = "close")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double close;

    @XmlAttribute(name = "volume")
    private Long volume;

    public OHLCType() {
    }

    public OHLCType(IOHLC ohlc) {
        this.date = ohlc.getDate();
        this.open = ohlc.getOpen();
        this.high = ohlc.getHigh();
        this.low = ohlc.getLow();
        this.close = ohlc.getClose();
        this.volume = ohlc.getVolume();
    }

    public IOHLC getOHLC() {
        return new OHLC(date, open, high, low, close, volume);
    }
}
