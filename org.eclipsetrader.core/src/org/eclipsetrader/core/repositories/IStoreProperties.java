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

/**
 * A set of properties associated with a store object.
 *
 * @since 1.0
 */
public interface IStoreProperties {

    /**
     * Returns the names of all properties.
     *
     * @return the property names array
     */
    public String[] getPropertyNames();

    /**
     * Returns the value of the property with the given name.
     *
     * @param name - the property name
     * @return the value, or null if the property is not set
     */
    public Object getProperty(String name);

    /**
     * Sets a property to a value.
     *
     * @param name - the property to set
     * @param value - the value
     */
    public void setProperty(String name, Object value);
}
