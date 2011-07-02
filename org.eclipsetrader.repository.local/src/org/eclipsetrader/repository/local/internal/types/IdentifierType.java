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

package org.eclipsetrader.repository.local.internal.types;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;

@XmlRootElement(name = "identifier")
public class IdentifierType implements Comparable<IdentifierType> {

    @XmlAttribute(name = "symbol")
    private String symbol;

    @XmlElementWrapper(name = "properties")
    @XmlElementRef
    private List<PropertyType> properties;

    private IFeedIdentifier identifier;

    public IdentifierType() {
    }

    public IdentifierType(String symbol) {
        this.symbol = symbol;
    }

    public IdentifierType(IFeedIdentifier identifier) {
        this.identifier = identifier;
        this.symbol = identifier.getSymbol();

        IFeedProperties feedProperties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (feedProperties != null) {
            properties = new ArrayList<PropertyType>();
            for (String name : feedProperties.getPropertyIDs()) {
                properties.add(new PropertyType(name, feedProperties.getProperty(name)));
            }
        }
    }

    @XmlTransient
    public IFeedIdentifier getIdentifier() {
        if (identifier == null) {
            FeedProperties feedProperties = null;
            if (properties != null) {
                feedProperties = new FeedProperties();
                for (PropertyType type : properties) {
                    feedProperties.setProperty(type.getName(), type.getValue());
                }
            }
            identifier = new FeedIdentifier(symbol, feedProperties);
        }
        return identifier;
    }

    @XmlTransient
    public String getSymbol() {
        return symbol;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IdentifierType o) {
        return getSymbol().compareTo(o.getSymbol());
    }
}
