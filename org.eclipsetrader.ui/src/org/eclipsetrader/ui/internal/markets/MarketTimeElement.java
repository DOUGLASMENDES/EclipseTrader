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

package org.eclipsetrader.ui.internal.markets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipsetrader.core.internal.markets.MarketTime;
import org.eclipsetrader.core.internal.markets.MarketTimeExclude;

public class MarketTimeElement implements Comparable<MarketTimeElement> {

    private Date openTime;
    private Date closeTime;
    private String description;

    private List<MarketTimeExcludeElement> exclude = new ArrayList<MarketTimeExcludeElement>();

    public static class MarketTimeExcludeElement implements Comparable<MarketTimeExcludeElement> {

        private Date fromDate;
        private Date toDate;

        public MarketTimeExcludeElement(Date fromDate, Date toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        public MarketTimeExcludeElement(MarketTimeExclude exclude) {
            this.fromDate = exclude.getFromDate();
            this.toDate = exclude.getToDate() != null ? exclude.getToDate() : exclude.getFromDate();
        }

        public Date getFromDate() {
            return fromDate;
        }

        public void setFromDate(Date fromDate) {
            this.fromDate = fromDate;
        }

        public Date getToDate() {
            return toDate;
        }

        public void setToDate(Date toDate) {
            this.toDate = toDate;
        }

        public MarketTimeExclude getMarketTimeExclude() {
            if (fromDate.equals(toDate)) {
                return new MarketTimeExclude(fromDate);
            }
            else {
                return new MarketTimeExclude(fromDate, toDate);
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(MarketTimeExcludeElement o) {
            return fromDate.compareTo(o.fromDate);
        }
    }

    public MarketTimeElement(Date openTime, Date closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public MarketTimeElement(MarketTime marketTime) {
        this.openTime = marketTime.getOpenTime();
        this.closeTime = marketTime.getCloseTime();
        this.description = marketTime.getDescription();

        for (MarketTimeExclude exclude : marketTime.getExclude()) {
            this.exclude.add(new MarketTimeExcludeElement(exclude));
        }
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MarketTimeExcludeElement> getExclude() {
        return exclude;
    }

    public MarketTime getMarketTime() {
        MarketTime marketTime = new MarketTime(openTime, closeTime, description);

        MarketTimeExclude[] exclude = new MarketTimeExclude[this.exclude.size()];
        for (int i = 0; i < exclude.length; i++) {
            exclude[i] = this.exclude.get(i).getMarketTimeExclude();
        }
        marketTime.setExclude(exclude);

        return marketTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MarketTimeElement o) {
        return openTime.compareTo(o.openTime);
    }
}
