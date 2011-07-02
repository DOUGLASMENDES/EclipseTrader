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

/**
 * Interface to access the properties associated with a feed identifier.
 *
 * @since 1.0
 */
public interface IFeedProperties {

    /**
     * Returns the IDs of all properties set to this identifier.
     *
     * @return the property IDs array
     */
    public String[] getPropertyIDs();

    /**
     * Returns the value of the property with the given name.
     *
     * @param id - the property id
     * @return the value, or null if the property is not set
     */
    public String getProperty(String id);

    /**
     * Sets a property to a value.
     *
     * @param id - the property to set
     * @param value - the value
     */
    public void setProperty(String id, String value);
}
