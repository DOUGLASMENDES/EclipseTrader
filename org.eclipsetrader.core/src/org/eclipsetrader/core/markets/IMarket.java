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

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.instruments.ISecurity;

/**
 * @since 1.0
 */
public interface IMarket extends IAdaptable {

    /**
     * Name property.
     */
    public static final String PROP_NAME = "name";

    /**
     * Time schedule property.
     */
    public static final String PROP_SCHEDULE = "schedule";

    /**
     * Weekdays property.
     */
    public static final String PROP_WEEKDAYS = "weekdays";

    /**
     * Holidays property.
     */
    public static final String PROP_HOLIDAYS = "holidays";

    /**
     * Timezone property.
     */
    public static final String PROP_TIMEZONE = "timezone";

    /**
     * Members property.
     */
    public static final String PROP_MEMBERS = "members";

    /**
     * Live feed connector property.
     */
    public static final String PROP_LIVE_FEED_CONNECTOR = "live-feed-connector";

    /**
     * Returns this market name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns wether the market is open at the time this method is invoked.
     *
     * @return true if the market is open, false otherwise.
     */
    public boolean isOpen();

    /**
     * Returns wether the market is open at the given date and time.
     *
     * @param time - the date and time to check.
     * @return true if the market is open, false otherwise.
     */
    public boolean isOpen(Date time);

    /**
     * Returns the market's open and close times for today.
     *
     * @return today's market open and close times
     */
    public IMarketDay getToday();

    /**
     * Returns the market's open and close times for the next day.
     *
     * @return next day's market open and close times
     */
    public IMarketDay getNextDay();

    /**
     * Adds a set of securities to the list of securities that are members of this market.
     *
     * @param securities the securities to add.
     */
    public void addMembers(ISecurity[] securities);

    /**
     * Removes a set of securities from the list of securities that are members of this market.
     *
     * @param securities the securities to remove.
     */
    public void removeMembers(ISecurity[] securities);

    /**
     * Returns a possibly empty array of securities that are members of this market.
     *
     * @return the array of securities.
     */
    public ISecurity[] getMembers();

    /**
     * Check if a security is a member of this market.
     *
     * @param security the security to check.
     * @return true if it is a member.
     */
    public boolean hasMember(ISecurity security);

    /**
     * Gets the connector used to receive live data.
     *
     * @return the feed connector.
     */
    public IFeedConnector getLiveFeedConnector();

    public IBackfillConnector getBackfillConnector();

    public IBackfillConnector getIntradayBackfillConnector();
}
