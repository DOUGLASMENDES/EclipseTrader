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

public class QuoteEvent {

    private IFeedConnector connector;
    private IFeedIdentifier identifier;
    private QuoteDelta[] delta;

    public QuoteEvent(IFeedConnector connector, IFeedIdentifier identifier, QuoteDelta[] delta) {
        this.connector = connector;
        this.identifier = identifier;
        this.delta = delta;
    }

    /**
     * Gets the connector that issued the event.
     *
     * @return the feed connector.
     */
    public IFeedConnector getConnector() {
        return connector;
    }

    /**
     * Returns the identifier associated with the event.
     *
     * @return the feed identifier.
     */
    public IFeedIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * Returns the quote objects that are changed since the last event.
     *
     * @return the quote objects.
     */
    public QuoteDelta[] getDelta() {
        return delta;
    }
}
