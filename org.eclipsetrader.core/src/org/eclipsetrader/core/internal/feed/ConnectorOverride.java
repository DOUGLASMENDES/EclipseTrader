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

package org.eclipsetrader.core.internal.feed;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.markets.BackfillConnectorAdapter;
import org.eclipsetrader.core.internal.markets.FeedConnectorAdapter;
import org.eclipsetrader.core.internal.markets.SecurityAdapter;

@XmlRootElement(name = "override")
public class ConnectorOverride implements IConnectorOverride {

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity security;

    @XmlElement(name = "feedConnector")
    @XmlJavaTypeAdapter(FeedConnectorAdapter.class)
    private IFeedConnector feedConnector;

    @XmlElement(name = "backfillConnector")
    @XmlJavaTypeAdapter(BackfillConnectorAdapter.class)
    private IBackfillConnector backfillConnector;

    @XmlElement(name = "intradayBackfillConnector")
    @XmlJavaTypeAdapter(BackfillConnectorAdapter.class)
    private IBackfillConnector intradayBackfillConnector;

    public ConnectorOverride() {
    }

    public ConnectorOverride(ISecurity security) {
        this.security = security;
    }

    @XmlTransient
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IConnectorOverride#getLiveFeedConnector()
     */
    @Override
    @XmlTransient
    public IFeedConnector getLiveFeedConnector() {
        return feedConnector;
    }

    public void setLiveFeedConnector(IFeedConnector feedConnector) {
        this.feedConnector = feedConnector;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IConnectorOverride#getBackfillConnector()
     */
    @Override
    @XmlTransient
    public IBackfillConnector getBackfillConnector() {
        return backfillConnector;
    }

    public void setBackfillConnector(IBackfillConnector backfillConnector) {
        this.backfillConnector = backfillConnector;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IConnectorOverride#getIntradayBackfillConnector()
     */
    @Override
    @XmlTransient
    public IBackfillConnector getIntradayBackfillConnector() {
        return intradayBackfillConnector;
    }

    public void setIntradayBackfillConnector(IBackfillConnector intradayBackfillConnector) {
        this.intradayBackfillConnector = intradayBackfillConnector;
    }
}
