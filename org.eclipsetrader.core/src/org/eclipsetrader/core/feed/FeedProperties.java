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

package org.eclipsetrader.core.feed;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the pproperties associated with a feed identifier.
 *
 * @see org.eclipsetrader.core.feed.IFeedProperties
 * @since 1.0
 */
public class FeedProperties implements IFeedProperties {

    private Map<String, String> properties = new HashMap<String, String>();
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public FeedProperties() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedProperties#getProperty(java.lang.String)
     */
    @Override
    public String getProperty(String id) {
        return properties.get(id);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedProperties#getPropertyIDs()
     */
    @Override
    public String[] getPropertyIDs() {
        return properties.keySet().toArray(new String[properties.keySet().size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedProperties#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void setProperty(String id, String value) {
        Object oldValue = properties.get(id);
        if (value == null) {
            properties.remove(id);
        }
        else {
            properties.put(id, value);
        }
        propertyChangeSupport.firePropertyChange(id, oldValue, properties.get(id));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (String k : properties.keySet()) {
            if (s.length() != 0) {
                s.append(",");
            }
            s.append(k);
            s.append("=");
            s.append(properties.get(k));
        }
        return s.toString();
    }
}
