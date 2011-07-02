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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipsetrader.repository.local.internal.stores.WatchListStore;

@XmlRootElement(name = "list")
public class WatchListCollection {

    private static WatchListCollection instance;

    @XmlAttribute(name = "next_id")
    private Integer nextId = new Integer(1);

    @XmlElementRef
    private List<WatchListStore> list;

    private Map<URI, WatchListStore> uriMap;

    public WatchListCollection() {
        instance = this;
        list = new ArrayList<WatchListStore>();
    }

    public static WatchListCollection getInstance() {
        return instance;
    }

    public WatchListStore get(URI uri) {
        synchronized (this) {
            if (uriMap == null) {
                uriMap = new HashMap<URI, WatchListStore>();
                for (WatchListStore securityStore : list) {
                    uriMap.put(securityStore.toURI(), securityStore);
                }
            }
        }
        return uriMap.get(uri);
    }

    public WatchListStore create() {
        WatchListStore securityStore = new WatchListStore(nextId);
        list.add(securityStore);
        if (uriMap != null) {
            uriMap.put(securityStore.toURI(), securityStore);
        }
        nextId = new Integer(nextId + 1);
        return securityStore;
    }

    public void delete(WatchListStore securityStore) {
        for (Iterator<WatchListStore> iter = list.iterator(); iter.hasNext();) {
            if (iter.next() == securityStore) {
                iter.remove();
                if (uriMap != null) {
                    uriMap.remove(securityStore.toURI());
                }
                break;
            }
        }
    }

    public WatchListStore[] getAll() {
        return list.toArray(new WatchListStore[list.size()]);
    }

    public List<WatchListStore> getList() {
        return list;
    }

    public WatchListStore[] toArray() {
        return list.toArray(new WatchListStore[list.size()]);
    }
}
