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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * This is the main interface to an object repository.
 *
 * @since 1.0
 */
public interface IRepository extends IAdaptable {

    /**
     * Gets the repository schema.
     *
     * @return the schema.
     */
    public String getSchema();

    /**
     * Returns whether this repository supports modification.
     *
     * @return true if this repository allows modification of objects, and false otherwise
     */
    public boolean canWrite();

    /**
     * Returns whether this repository supports delete.
     *
     * @return true if this repository allows deletion of objects, and false otherwise
     */
    public boolean canDelete();

    /**
     * Returns an array containing information about all objects stored in this repository.
     *
     * <p>The array represents the state of the repository at the time it is created, but it is
     * never updated. Clients using the array must tolerate the fact that the actual repository
     * content may change after the array is generated.</p>
     *
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired
     * @return an array containing the stored objects
     */
    public IStore[] fetchObjects(IProgressMonitor monitor);

    /**
     * Returns an object store from this repository. The provided uri must have the
     * appropriate scheme and part for the repository on which this method is called.
     *
     * @param uri - the URI of the object to return
     * @return the object store, or null if no object is found
     */
    public IStore getObject(URI uri);

    /**
     * Creates a new object.
     *
     * @return the object store
     */
    public IStore createObject();

    /**
     * Runs the specified task in the repository.
     *
     * @param runnable - the task to run
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired
     */
    public IStatus runInRepository(IRepositoryRunnable runnable, IProgressMonitor monitor);

    /**
     * Runs the specified task in the repository.
     *
     * @param runnable - the task to run
     * @param rule the scheduling rule to use when running this operation, or null if there are no scheduling restrictions for this operation.
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired
     */
    public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor);
}
