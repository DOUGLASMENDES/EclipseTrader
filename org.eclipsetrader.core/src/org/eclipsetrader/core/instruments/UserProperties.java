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

package org.eclipsetrader.core.instruments;

import java.util.HashMap;
import java.util.Map;

public class UserProperties implements IUserProperties {

    private Map<IUserProperty, String> properties = new HashMap<IUserProperty, String>();

    public UserProperties() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.IUserProperties#clearProperties()
     */
    @Override
    public void clearProperties() {
        properties.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.IUserProperties#getProperties()
     */
    @Override
    public IUserProperty[] getProperties() {
        return properties.keySet().toArray(new IUserProperty[properties.keySet().size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.IUserProperties#getValue(org.eclipsetrader.core.model.IUserProperty)
     */
    @Override
    public String getValue(IUserProperty property) {
        return properties.get(property);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.IUserProperties#hasProperty(org.eclipsetrader.core.model.IUserProperty)
     */
    @Override
    public boolean hasProperty(IUserProperty property) {
        return properties.containsKey(property);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.IUserProperties#setValue(org.eclipsetrader.core.model.IUserProperty, java.lang.String)
     */
    @Override
    public void setValue(IUserProperty property, String value) {
        if (value != null) {
            properties.put(property, value);
        }
        else {
            properties.remove(property);
        }
    }
}
