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

package org.eclipsetrader.core;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketDay;

public class TestMarket implements IMarket {

    private String name;
    private Set<ISecurity> members = new HashSet<ISecurity>();
    private IFeedConnector liveFeedConnector;
    private IBackfillConnector backfillConnector;

    public TestMarket(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#addMembers(org.eclipsetrader.core.instruments.ISecurity[])
     */
    @Override
    public void addMembers(ISecurity[] securities) {
        members.addAll(Arrays.asList(securities));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getLiveFeedConnector()
     */
    @Override
    public IFeedConnector getLiveFeedConnector() {
        return liveFeedConnector;
    }

    public void setLiveFeedConnector(IFeedConnector liveFeedConnector) {
        this.liveFeedConnector = liveFeedConnector;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getBackfillConnector()
     */
    @Override
    public IBackfillConnector getBackfillConnector() {
        return backfillConnector;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getIntradayBackfillConnector()
     */
    @Override
    public IBackfillConnector getIntradayBackfillConnector() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getMembers()
     */
    @Override
    public ISecurity[] getMembers() {
        return members.toArray(new ISecurity[members.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getNextDay()
     */
    @Override
    public IMarketDay getNextDay() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getToday()
     */
    @Override
    public IMarketDay getToday() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#hasMember(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean hasMember(ISecurity security) {
        return members.contains(security);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#isOpen()
     */
    @Override
    public boolean isOpen() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#isOpen(java.util.Date)
     */
    @Override
    public boolean isOpen(Date time) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#removeMembers(org.eclipsetrader.core.instruments.ISecurity[])
     */
    @Override
    public void removeMembers(ISecurity[] securities) {
        members.removeAll(Arrays.asList(securities));
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }
}
