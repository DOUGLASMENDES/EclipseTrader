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

package org.eclipsetrader.opentick.internal.core.repository;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;

@XmlRootElement(name = "list")
@XmlType(name = "org.eclipsetrader.opentick.IdentifiersList")
public class IdentifiersList {
	private static final String SYMBOL_PROPERTY = "org.eclipsetrader.opentick.symbol";
	private static final String EXCHANGE_PROPERTY = "org.eclipsetrader.opentick.exchange";
	private static IdentifiersList instance;

    @XmlElementRef
	private List<IdentifierType> identifiers;

	public IdentifiersList() {
		instance = this;
		identifiers = new ArrayList<IdentifierType>();
	}

	public static IdentifiersList getInstance() {
    	return instance;
    }

	@XmlTransient
	public List<IdentifierType> getIdentifiers() {
    	return identifiers;
    }

	public void setIdentifiers(List<IdentifierType> identifiers) {
    	this.identifiers = identifiers;
    }

	public IdentifierType getIdentifierFor(IFeedIdentifier identifier) {
		String symbol = identifier.getSymbol();
		String exchange = "@";

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty(SYMBOL_PROPERTY) != null)
				symbol = properties.getProperty(SYMBOL_PROPERTY);
			if (properties.getProperty(EXCHANGE_PROPERTY) != null)
				exchange = properties.getProperty(EXCHANGE_PROPERTY);
		}

		for (IdentifierType type : identifiers) {
			if (type.getSymbol().equals(symbol) && type.getExchange().equals(exchange)) {
				type.setIdentifier(identifier);
				return type;
			}
		}

		IdentifierType type = new IdentifierType(symbol, exchange);
		type.setIdentifier(identifier);
		identifiers.add(type);
		return type;
	}
}
