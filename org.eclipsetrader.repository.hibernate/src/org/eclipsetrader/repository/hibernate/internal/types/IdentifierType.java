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

package org.eclipsetrader.repository.hibernate.internal.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "identifiers")
public class IdentifierType {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Version
    @Column(name = "version")
    @SuppressWarnings("unused")
    private Integer version;

    @Column(name = "symbol", unique = true)
    private String symbol;

    @OneToMany(mappedBy = "identifier", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    List<IdentifierPropertyType> properties = new ArrayList<IdentifierPropertyType>();

    @Transient
    private IFeedIdentifier identifier;

    public IdentifierType() {
    }

    public IdentifierType(IFeedIdentifier identifier) {
        this.identifier = identifier;
        this.symbol = identifier.getSymbol();

        IFeedProperties feedProperties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (feedProperties != null) {
            for (String name : feedProperties.getPropertyIDs()) {
                properties.add(new IdentifierPropertyType(this, name, feedProperties.getProperty(name)));
            }
        }
    }

    public IFeedIdentifier getIdentifier() {
        if (identifier == null) {
            FeedProperties feedProperties = null;
            if (properties != null && properties.size() != 0) {
                feedProperties = new FeedProperties();
                for (IdentifierPropertyType type : properties) {
                    feedProperties.setProperty(type.getName(), type.getValue());
                }
            }
            identifier = new FeedIdentifier(symbol, feedProperties);
        }
        return identifier;
    }

    public String getSymbol() {
        return symbol;
    }

    public void updateProperties(IFeedProperties feedProperties) {
        if (feedProperties == null) {
            properties.clear();
            return;
        }

        Map<String, IdentifierPropertyType> map = new HashMap<String, IdentifierPropertyType>();

        for (String name : feedProperties.getPropertyIDs()) {
            for (IdentifierPropertyType property : properties) {
                if (name.equals(property.getName())) {
                    property.setValue(feedProperties.getProperty(name));
                    map.put(name, property);
                    break;
                }
            }
        }
        for (String name : feedProperties.getPropertyIDs()) {
            if (!map.containsKey(name)) {
                map.put(name, new IdentifierPropertyType(this, name, feedProperties.getProperty(name)));
            }
        }

        properties.clear();
        for (IdentifierPropertyType property : map.values()) {
            properties.add(property);
        }
    }
}
