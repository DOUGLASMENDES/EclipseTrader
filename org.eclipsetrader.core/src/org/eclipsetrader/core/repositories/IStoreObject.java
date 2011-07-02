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
 * Interface implemented by all classes that can be stored to
 * a repository, provides methods to get and set stores and properties.
 *
 * @since 1.0
 */
public interface IStoreObject {

    /**
     * Gets the store associated with this object, or null
     * if the object wasn't yet saved to a repository.
     *
     * @return the store, or null.
     */
    public IStore getStore();

    /**
     * Sets the store associated with this object.
     *
     * @param store the store to set.
     */
    public void setStore(IStore store);

    /**
     * Gets the properties set from which this object was built.
     *
     * @return the properties set.
     */
    public IStoreProperties getStoreProperties();

    /**
     * Updates this object with the given properties set.
     *
     * @param storeProperties the properties to set.
     */
    public void setStoreProperties(IStoreProperties storeProperties);
}
