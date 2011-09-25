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

import org.eclipsetrader.repository.local.internal.stores.ScriptStore;

@XmlRootElement(name = "list")
public class ScriptsCollection {

    private static ScriptsCollection instance;

    @XmlAttribute(name = "next_id")
    private Integer nextId = new Integer(1);

    @XmlElementRef
    private List<ScriptStore> list;

    private Map<URI, ScriptStore> uriMap;

    public ScriptsCollection() {
        instance = this;
        list = new ArrayList<ScriptStore>();
    }

    public static ScriptsCollection getInstance() {
        return instance;
    }

    public ScriptStore get(URI uri) {
        synchronized (this) {
            if (uriMap == null) {
                uriMap = new HashMap<URI, ScriptStore>();
                for (ScriptStore store : list) {
                    uriMap.put(store.toURI(), store);
                }
            }
        }
        return uriMap.get(uri);
    }

    public ScriptStore create() {
        ScriptStore securityStore = new ScriptStore(nextId);
        list.add(securityStore);
        if (uriMap != null) {
            uriMap.put(securityStore.toURI(), securityStore);
        }
        nextId = new Integer(nextId + 1);
        return securityStore;
    }

    public void delete(ScriptStore tradeStore) {
        for (Iterator<ScriptStore> iter = list.iterator(); iter.hasNext();) {
            if (iter.next() == tradeStore) {
                iter.remove();
                if (uriMap != null) {
                    uriMap.remove(tradeStore.toURI());
                }
                break;
            }
        }
    }

    public ScriptStore[] getAll() {
        return list.toArray(new ScriptStore[list.size()]);
    }

    public List<ScriptStore> getList() {
        return list;
    }

    public ScriptStore[] toArray() {
        return list.toArray(new ScriptStore[list.size()]);
    }
}
