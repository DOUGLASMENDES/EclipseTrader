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

import org.eclipsetrader.core.feed.IBackfillConnector;

@XmlRootElement(name = "backfill")
public class MarketBackfillConnector {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(BackfillConnectorAdapter.class)
    private IBackfillConnector connector;

    public MarketBackfillConnector() {
    }

    public MarketBackfillConnector(IBackfillConnector connector) {
        this.connector = connector;
    }

    @XmlTransient
    public IBackfillConnector getConnector() {
        return connector;
    }
}
