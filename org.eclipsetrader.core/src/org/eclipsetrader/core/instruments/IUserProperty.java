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
 * Defines a user-defined property object.
 *
 * @since 1.0
 */
public interface IUserProperty {

    /**
     * Returns the name of the property.
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns wether this property is required.
     * <p>Property editor widgets should prevent the users from saving a
     * security without all required properties set.</p>
     *
     * @return true if the property is required
     */
    public boolean isRequired();

    /**
     * Returns the default value.
     *
     * @return the value
     */
    public String getDefaultValue();
}
