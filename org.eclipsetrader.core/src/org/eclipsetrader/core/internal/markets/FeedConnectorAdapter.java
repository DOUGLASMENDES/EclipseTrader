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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.internal.CoreActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class FeedConnectorAdapter extends XmlAdapter<String, IFeedConnector> {

    public class FailsafeFeedConnector implements IFeedConnector {

        private String id;

        public FailsafeFeedConnector(String id) {
            this.id = id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
         */
        @Override
        public void connect() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
         */
        @Override
        public void disconnect() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
         */
        @Override
        public String getId() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
         */
        @Override
        public String getName() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
         */
        @Override
        public IFeedSubscription subscribe(IFeedIdentifier identifier) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
         */
        @Override
        public void addConnectorListener(IConnectorListener listener) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
         */
        @Override
        public void removeConnectorListener(IConnectorListener listener) {
        }
    }

    public FeedConnectorAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IFeedConnector v) throws Exception {
        return v != null ? v.getId() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IFeedConnector unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        IFeedConnector connector = null;
        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference<IFeedService> serviceReference = context.getServiceReference(IFeedService.class);

            IFeedService feedService = context.getService(serviceReference);
            connector = feedService.getConnector(v);
            if (connector == null) {
                connector = new FailsafeFeedConnector(v);
            }

            context.ungetService(serviceReference);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error reading feed service", e);
            CoreActivator.log(status);
        }

        if (connector == null) {
            connector = new FailsafeFeedConnector(v);
        }

        return connector;
    }
}
