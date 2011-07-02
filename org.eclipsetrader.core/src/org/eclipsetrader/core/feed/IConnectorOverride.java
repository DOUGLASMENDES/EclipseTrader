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

/**
 * Interface used to override the default connectors associated to a security.
 *
 * <p>For any given method a return value of <code>null</code> means that no override is
 * set and the default connector should be used.<p>
 *
 * @since 1.0
 */
public interface IConnectorOverride {

    /**
     * Gets the connector used to receive live data.
     *
     * @return the feed connector.
     */
    public IFeedConnector getLiveFeedConnector();

    public IBackfillConnector getBackfillConnector();

    public IBackfillConnector getIntradayBackfillConnector();
}
