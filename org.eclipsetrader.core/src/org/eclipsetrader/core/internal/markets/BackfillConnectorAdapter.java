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

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.internal.CoreActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BackfillConnectorAdapter extends XmlAdapter<String, IBackfillConnector> {

    public class FailsafeBackfillConnector implements IBackfillConnector {

        private String id;

        public FailsafeBackfillConnector(String id) {
            this.id = id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillDividends(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
         */
        @Override
        public IDividend[] backfillDividends(IFeedIdentifier identifier, Date from, Date to) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillHistory(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
         */
        @Override
        public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date from, Date to, TimeSpan timeSpan) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillSplits(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
         */
        @Override
        public ISplit[] backfillSplits(IFeedIdentifier identifier, Date from, Date to) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IBackfillConnector#canBackfill(org.eclipsetrader.core.feed.IFeedIdentifier, org.eclipsetrader.core.feed.TimeSpan)
         */
        @Override
        public boolean canBackfill(IFeedIdentifier identifier, TimeSpan timeSpan) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IBackfillConnector#getId()
         */
        @Override
        public String getId() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IBackfillConnector#getName()
         */
        @Override
        public String getName() {
            return null;
        }
    }

    public BackfillConnectorAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IBackfillConnector v) throws Exception {
        return v != null ? v.getId() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IBackfillConnector unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        IBackfillConnector connector = null;
        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());

            IFeedService feedService = (IFeedService) context.getService(serviceReference);
            connector = feedService.getBackfillConnector(v);

            context.ungetService(serviceReference);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error reading feed service", e);
            CoreActivator.log(status);
        }

        return connector;
    }
}
