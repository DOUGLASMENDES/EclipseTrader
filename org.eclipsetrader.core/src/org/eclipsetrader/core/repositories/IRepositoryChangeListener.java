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
 * A repository change listener is notified of changes to resources
 * stored in the repositories.
 * <p>
 * Clients may implement this interface.</p>
 *
 * @since 1.0
 */
public interface IRepositoryChangeListener {

    /**
     * Notifies this listener that some resource changes have happened.
     *
     * @param event the resource change event
     */
    public void repositoryResourceChanged(RepositoryChangeEvent event);
}
