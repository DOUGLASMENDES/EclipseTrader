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

package org.eclipsetrader.repository.local.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.repository.local.internal.types.IdentifierType;

@XmlRootElement(name = "list")
public class IdentifiersCollection {

    private static IdentifiersCollection instance;

    @XmlElementRef
    private TreeSet<IdentifierType> list;

    private Map<String, IFeedIdentifier> identifierMap;

    public IdentifiersCollection() {
        instance = this;
        list = new TreeSet<IdentifierType>();
    }

    public static IdentifiersCollection getInstance() {
        if (instance == null) {
            instance = new IdentifiersCollection();
        }
        return instance;
    }

    public IFeedIdentifier getFeedIdentifierFromSymbol(String symbol) {
        if (identifierMap == null) {
            identifierMap = new HashMap<String, IFeedIdentifier>();
            for (IdentifierType type : list) {
                identifierMap.put(type.getSymbol(), type.getIdentifier());
            }
        }

        IFeedIdentifier feedIdentifier = identifierMap.get(symbol);
        if (feedIdentifier == null) {
            IdentifierType identifierType = new IdentifierType(symbol);
            feedIdentifier = identifierType.getIdentifier();
            list.add(identifierType);
            identifierMap.put(symbol, feedIdentifier);
        }

        return feedIdentifier;
    }

    public void putFeedIdentifier(IFeedIdentifier feedIdentifier) {
        for (Iterator<IdentifierType> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getSymbol().equals(feedIdentifier.getSymbol())) {
                iter.remove();
            }
        }
        IdentifierType identifierType = new IdentifierType(feedIdentifier);
        list.add(identifierType);
        if (identifierMap != null) {
            identifierMap.put(feedIdentifier.getSymbol(), feedIdentifier);
        }
    }

    public TreeSet<IdentifierType> getList() {
        return list;
    }
}
