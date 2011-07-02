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
 * Objects that implements this interface are factories capable of
 * creating data providers.
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public interface IDataProviderFactory {

    /**
     * Returns the unique id associated with this factory.
     *
     * @return the factory's id.
     */
    public String getId();

    /**
     * Returns the human-readable name of the this factory.
     *
     * @return the factory's name.
     */
    public String getName();

    /**
     * Returns the data provider.
     *
     * @return the data provider.
     */
    public IDataProvider createProvider();

    /**
     * Returns an array of possible class types returned as values
     * from the data providers created by this factory.
     *
     * @return the class types array.
     */
    public Class[] getType();
}
