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

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An object store is responsible for storage and retrieval of a single object
 * in some object repository.
 *
 * @since 1.0
 */
public interface IStore {

    /**
     * Fetches and returns the properties of this object from the underlying repository.
     *
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired
     * @return a structure containing the properties of this object.
     */
    public IStoreProperties fetchProperties(IProgressMonitor monitor);

    /**
     * Writes the properties of this object to the underlying repository.
     *
     * @param properties - the properties to set
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired
     */
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor);

    /**
     * Delete the object represented by this store.
     *
     * @param monitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not desired
     * @throws CoreException if this method fails.
     */
    public void delete(IProgressMonitor monitor) throws CoreException;

    /**
     * Returns an array containing information about all objects that are childs of this store.
     *
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired
     * @return an array containing the child objects objects
     */
    public IStore[] fetchChilds(IProgressMonitor monitor);

    /**
     * Creates a new child object.
     *
     * @return the object store
     */
    public IStore createChild();

    /**
     * Returns the repository this object store belongs to.
     *
     * @return the object repository
     */
    public IRepository getRepository();

    /**
     * Returns an URI instance corresponding to this object.
     *
     * @return a URI corresponding to this object
     */
    public URI toURI();
}
