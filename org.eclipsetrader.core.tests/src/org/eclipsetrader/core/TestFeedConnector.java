/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;

public class TestFeedConnector implements IFeedConnector {
	private String id;
	private String name;
	private Set<IFeedSubscription> subscriptions = new HashSet<IFeedSubscription>();

	public TestFeedConnector(String id, String name) {
	    this.id = id;
	    this.name = name;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
	 */
	public void connect() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
	 */
	public void disconnect() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
	 */
	public IFeedSubscription subscribe(final IFeedIdentifier identifier) {
		IFeedSubscription s = new IFeedSubscription() {

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#dispose()
             */
            public void dispose() {
            	subscriptions.remove(this);
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#addSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
             */
            public void addSubscriptionListener(ISubscriptionListener listener) {
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getIdentifier()
             */
            public IFeedIdentifier getIdentifier() {
	            return identifier;
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getSymbol()
             */
            public String getSymbol() {
	            return identifier.getSymbol();
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
             */
            public ILastClose getLastClose() {
	            return null;
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
             */
            public IQuote getQuote() {
	            return null;
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
             */
            public ITodayOHL getTodayOHL() {
	            return null;
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
             */
            public ITrade getTrade() {
	            return null;
            }

			/* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#removeSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
             */
            public void removeSubscriptionListener(ISubscriptionListener listener) {
            }
		};
		subscriptions.add(s);
		return s;
	}

	public Set<IFeedSubscription> getSubscriptions() {
    	return subscriptions;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    public void addConnectorListener(IConnectorListener listener) {
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    public void removeConnectorListener(IConnectorListener listener) {
    }
}
