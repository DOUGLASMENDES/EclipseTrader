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

package org.eclipsetrader.core.trading;

import java.util.Date;

import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;

public class AlertEvent {

    private Date time;
    private ISecurity instrument;
    private ITrade trade;
    private IQuote quote;
    private IAlert[] alerts;

    public AlertEvent(ISecurity instrument, ITrade trade, IQuote quote, IAlert[] alerts) {
        this.instrument = instrument;
        this.trade = trade;
        this.quote = quote;
        this.alerts = alerts;
    }

    public Date getTime() {
        return time;
    }

    public ISecurity getInstrument() {
        return instrument;
    }

    public IAlert[] getTriggeredAlerts() {
        return alerts;
    }

    public ITrade getTrade() {
        return trade;
    }

    public IQuote getQuote() {
        return quote;
    }
}
