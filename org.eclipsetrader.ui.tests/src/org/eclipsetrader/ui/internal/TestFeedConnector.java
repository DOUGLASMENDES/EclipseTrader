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

package org.eclipsetrader.ui.internal;

import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;

public class TestFeedConnector implements IFeedConnector {
	private String id;
	private String name;

	public TestFeedConnector(String id, String name) {
		this.id = id;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
	 */
	public void addConnectorListener(IConnectorListener listener) {
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
	 * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
	 */
	public void removeConnectorListener(IConnectorListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
	 */
	public IFeedSubscription subscribe(IFeedIdentifier identifier) {
		return null;
	}
}
