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

package org.eclipsetrader.core.internal.ats.repository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.internal.ats.TradingSystemProperties;
import org.eclipsetrader.core.repositories.IStoreObject;

@XmlRootElement(name = "list")
public class SettingsCollection {

    @XmlElementRef
    private List<Settings> systems = new ArrayList<Settings>();

    public SettingsCollection() {
    }

    public void add(Settings settings) {
        systems.add(settings);
    }

    public TradingSystemProperties getSettingsFor(IAdaptable strategy) {
        IStoreObject storeObject = (IStoreObject) strategy.getAdapter(IStoreObject.class);
        if (storeObject == null) {
            return null;
        }

        URI uri = storeObject.getStore().toURI();
        for (Settings settings : systems) {
            if (settings.getUri().equals(uri)) {
                return settings.getProperties();
            }
        }

        return null;
    }

    public void setSettingsFor(IAdaptable strategy, TradingSystemProperties properties) {
        IStoreObject storeObject = (IStoreObject) strategy.getAdapter(IStoreObject.class);
        if (storeObject == null) {
            return;
        }

        URI uri = storeObject.getStore().toURI();
        for (Iterator<Settings> iter = systems.iterator(); iter.hasNext();) {
            Settings settings = iter.next();
            if (settings.getUri().equals(uri)) {
                iter.remove();
                break;
            }
        }

        systems.add(new Settings(uri, properties));
    }
}
