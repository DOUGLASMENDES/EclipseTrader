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

package org.eclipsetrader.core.feed;

public interface IFeedConnector {

    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;
    public static final int STATUS_LOGGED_IN = 3;

    public String getId();

    public String getName();

    public IFeedSubscription subscribe(IFeedIdentifier identifier);

    public void connect();

    public void disconnect();

    public void addConnectorListener(IConnectorListener listener);

    public void removeConnectorListener(IConnectorListener listener);
}
