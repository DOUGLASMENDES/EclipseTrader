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

package org.eclipsetrader.core.markets;

/**
 * Instances of this class are sent as a result of a market
 * status change.
 *
 * @since 1.0
 */
public class MarketStatusEvent {

    private IMarket market;

    /**
     * Constructs the status event.
     *
     * @param market the market that generated the status change.
     */
    public MarketStatusEvent(IMarket market) {
        this.market = market;
    }

    /**
     * Returns the market that generated the event.
     *
     * @return the market.
     */
    public IMarket getMarket() {
        return market;
    }
}
