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

package org.eclipsetrader.core.views;

/**
 * Interface implemented by objects that allows clients to set
 * application defined properties. The values are not persisted.
 *
 * @since 1.0
 */
public interface ISessionData {

    /**
     * Returns the application defined property of the receiver with
     * the specified name, or null if it has not been set.
     *
     * @param key the name of the property.
     * @return the value of the property or null if it has not been set.
     */
    public Object getData(Object key);

    /**
     * Sets the application defined property of the receiver with the
     * specified name to the given value.
     *
     * @param key the name of the property.
     * @param value the new value for the property.
     */
    public void setData(Object key, Object value);
}
