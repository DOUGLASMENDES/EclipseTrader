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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides a list of user-created repositories.
 *
 * @since 1.0
 */
public interface IRepositoryProvider {

    /**
     * Gets the user-defined repositories.
     *
     * @param monitor - a progress monitor, or null if progress reporting is not desired.
     * @return the array of repositories.
     */
    public IRepository[] getRepositories(IProgressMonitor monitor);
}
