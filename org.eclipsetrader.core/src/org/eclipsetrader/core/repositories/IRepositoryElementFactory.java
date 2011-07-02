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
 * A factory to recreate objects from a set of properties.
 *
 * @since 1.0
 */
public interface IRepositoryElementFactory {

    /**
     * Gets the unique id of this elements factory.
     *
     * @return the factory id.
     */
    public String getId();

    /**
     * Re-creates and returns an object from the given properties.
     *
     * @param properties the object properties
     * @return an object, or null if the element could not be created
     */
    public IStoreObject createElement(IStore store, IStoreProperties properties);
}
