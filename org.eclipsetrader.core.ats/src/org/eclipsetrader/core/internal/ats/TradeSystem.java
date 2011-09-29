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

package org.eclipsetrader.core.internal.ats;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.ats.ITradeStrategy;
import org.eclipsetrader.core.ats.ITradeSystem;
import org.eclipsetrader.core.ats.ITradeSystemParameter;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.ats.repository.InstrumentType;
import org.eclipsetrader.core.internal.ats.repository.TradeStrategyAdapter;

@XmlRootElement(name = "system")
public class TradeSystem implements ITradeSystem {

    @XmlAttribute(name = "active")
    private boolean active;

    @XmlAttribute(name = "strategy")
    @XmlJavaTypeAdapter(TradeStrategyAdapter.class)
    private ITradeStrategy tradeStrategy;

    @XmlElementRef
    private InstrumentType instrumentType;

    @XmlElementWrapper(name = "parameters")
    @XmlElementRef
    @XmlJavaTypeAdapter(ParameterAdapter.class)
    private List<ITradeSystemParameter> parameters;

    public static class ParameterAdapter extends XmlAdapter<TradeSystemParameter, ITradeSystemParameter> {

        public ParameterAdapter() {
        }

        /* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
         */
        @Override
        public TradeSystemParameter marshal(ITradeSystemParameter v) throws Exception {
            return v != null ? new TradeSystemParameter(v.getName(), v.getValue()) : null;
        }

        /* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
         */
        @Override
        public ITradeSystemParameter unmarshal(TradeSystemParameter v) throws Exception {
            return v;
        }
    }

    public TradeSystem() {
    }

    public TradeSystem(boolean active, ITradeStrategy tradeStrategy, ISecurity security, TimeSpan timeSpan) {
        this.active = active;
        this.tradeStrategy = tradeStrategy;
        this.instrumentType = new InstrumentType(security, timeSpan);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystem#isActive()
     */
    @Override
    @XmlTransient
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystem#getTradeStrategy()
     */
    @Override
    @XmlTransient
    public ITradeStrategy getTradeStrategy() {
        return tradeStrategy;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystem#getInstrument()
     */
    @Override
    @XmlTransient
    public ISecurity getInstrument() {
        return instrumentType.getInstrument();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystem#getTimeSpan()
     */
    @Override
    @XmlTransient
    public TimeSpan getTimeSpan() {
        return instrumentType.getTimeSpan();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystem#getParameters()
     */
    @Override
    @XmlTransient
    public ITradeSystemParameter[] getParameters() {
        return parameters != null ? parameters.toArray(new ITradeSystemParameter[parameters.size()]) : new ITradeSystemParameter[0];
    }
}
