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

package org.eclipsetrader.core.internal.trading;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketDay;
import org.eclipsetrader.core.markets.IMarketService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MarketAdapter extends XmlAdapter<String, IMarket> {

    private class FailsafeMarket implements IMarket {

        private String name;

        public FailsafeMarket(String name) {
            this.name = name;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.markets.IMarket#addMembers(org.eclipsetrader.core.instruments.ISecurity[])
         */
        @Override
        public void addMembers(ISecurity[] securities) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.markets.IMarket#getBackfillConnector()
         */
        @Override
        public IBackfillConnector getBackfillConnector() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.markets.IMarket#getIntradayBackfillConnector()
         */
        @Override
        public IBackfillConnector getIntradayBackfillConnector() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.markets.IMarket#getLiveFeedConnector()
         */
        @Override
        public IFeedConnector getLiveFeedConnector() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.markets.IMarket#getMembers()
         */
        @Override
        public ISecurity[] getMembers() {
            return null;
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
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.markets.IMarket#isOpen()
         */
        @Override
        public boolean isOpen() {
            return false;
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
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            return null;
        }
    }

    public MarketAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IMarket v) throws Exception {
        return v != null ? v.getName() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IMarket unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        IMarket market = null;

        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());

            IMarketService marketService = (IMarketService) context.getService(serviceReference);
            market = marketService.getMarket(v);

            context.ungetService(serviceReference);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error reading market service", e);
            CoreActivator.log(status);
        }

        if (market == null) {
            Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Failed to load market " + v, null);
            CoreActivator.log(status);
            return new FailsafeMarket(v);
        }

        return market;
    }
}
