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

package org.eclipsetrader.core.internal.markets;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.trading.IBroker;

@XmlRootElement(name = "broker")
public class MarketBroker {
    @XmlAttribute(name = "id")
	@XmlJavaTypeAdapter(BrokerAdapter.class)
	private IBroker connector;

	public MarketBroker() {
	}

	public MarketBroker(IBroker connector) {
	    this.connector = connector;
    }

	@XmlTransient
	public IBroker getConnector() {
    	return connector;
    }
}
