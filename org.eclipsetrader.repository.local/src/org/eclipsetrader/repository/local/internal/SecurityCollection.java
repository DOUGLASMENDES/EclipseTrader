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

import org.eclipsetrader.repository.local.internal.stores.SecurityStore;

@XmlRootElement(name = "list")
public class SecurityCollection {

    private static SecurityCollection instance;

    @XmlAttribute(name = "next_id")
    private Integer nextId = new Integer(1);

    @XmlElementRef
    private List<SecurityStore> list;

    private Map<URI, SecurityStore> uriMap;

    public SecurityCollection() {
        instance = this;
        list = new ArrayList<SecurityStore>();
    }

    public static SecurityCollection getInstance() {
        return instance;
    }

    public SecurityStore get(URI uri) {
        synchronized (this) {
            if (uriMap == null) {
                uriMap = new HashMap<URI, SecurityStore>();
                for (SecurityStore securityStore : list) {
                    uriMap.put(securityStore.toURI(), securityStore);
                }
            }
        }
        return uriMap.get(uri);
    }

    public SecurityStore create() {
        SecurityStore securityStore = new SecurityStore(nextId);
        list.add(securityStore);
        if (uriMap != null) {
            uriMap.put(securityStore.toURI(), securityStore);
        }
        nextId = new Integer(nextId + 1);
        return securityStore;
    }

    public void delete(SecurityStore securityStore) {
        for (Iterator<SecurityStore> iter = list.iterator(); iter.hasNext();) {
            if (iter.next() == securityStore) {
                iter.remove();
                if (uriMap != null) {
                    uriMap.remove(securityStore.toURI());
                }
                break;
            }
        }
    }

    public SecurityStore[] getAll() {
        return list.toArray(new SecurityStore[list.size()]);
    }

    public List<SecurityStore> getList() {
        return list;
    }

    public SecurityStore[] toArray() {
        return list.toArray(new SecurityStore[list.size()]);
    }
}
