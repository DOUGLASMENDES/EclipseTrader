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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.IFeedConnector;

@XmlRootElement(name = "connector")
public class MarketConnector {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(FeedConnectorAdapter.class)
    private IFeedConnector connector;

    public MarketConnector() {
    }

    public MarketConnector(IFeedConnector connector) {
        this.connector = connector;
    }

    @XmlTransient
    public IFeedConnector getConnector() {
        return connector;
    }
}
