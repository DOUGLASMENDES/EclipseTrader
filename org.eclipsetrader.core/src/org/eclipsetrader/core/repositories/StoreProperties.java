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

package org.eclipsetrader.core.repositories;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the IStoreProperties interface.
 *
 * @since 1.0
 */
public class StoreProperties implements IStoreProperties {

    private Map<String, Object> properties = new HashMap<String, Object>();

    public StoreProperties() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreProperties#getPropertyNames()
     */
    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreProperties#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreProperties#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String name, Object value) {
        if (value == null) {
            properties.remove(name);
        }
        else {
            properties.put(name, value);
        }
    }

    protected Map<String, Object> getProperties() {
        return properties;
    }
}
