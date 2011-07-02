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

package org.eclipsetrader.core.internal.markets;

import java.util.Calendar;
import java.util.Date;

import org.eclipsetrader.core.markets.IMarketDay;

public class MarketDay implements IMarketDay {

    private Date openTime;
    private Date closeTime;
    private String message;

    public MarketDay(Date openTime, Date closeTime, String message) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.message = message;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketDay#getOpenTime()
     */
    @Override
    public Date getOpenTime() {
        return openTime;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketDay#getCloseTime()
     */
    @Override
    public Date getCloseTime() {
        return closeTime;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketDay#getMessage()
     */
    @Override
    public String getMessage() {
        return message;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketDay#isOpen()
     */
    @Override
    public boolean isOpen() {
        return isOpen(Calendar.getInstance().getTime());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketDay#isOpen(java.util.Date)
     */
    @Override
    public boolean isOpen(Date time) {
        if (openTime == null || closeTime == null) {
            return false;
        }
        return (time.equals(openTime) || time.after(openTime)) && time.before(closeTime);
    }
}
