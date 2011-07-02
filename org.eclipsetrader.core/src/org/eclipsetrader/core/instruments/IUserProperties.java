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

/**
 * Defines the user-defined properties associated to a security.
 *
 * @since 1.0
 */
public interface IUserProperties {

    /**
     * Returns the value associated with the given property, if set.
     *
     * @param property - the property
     * @return the value, or null if no value is set
     */
    public String getValue(IUserProperty property);

    /**
     * Sets the value of a property.
     *
     * @param property - the property
     * @param value - the value to set
     */
    public void setValue(IUserProperty property, String value);

    /**
     * Returns all properties that have a value set.
     *
     * @return the properties
     */
    public IUserProperty[] getProperties();

    /**
     * Removes all property values from the receiver.
     */
    public void clearProperties();

    /**
     * Returns wether the receiver has a non null value for the given property.
     *
     * @param property - the property to test
     * @return true if a value is set, false otherwise
     */
    public boolean hasProperty(IUserProperty property);
}
