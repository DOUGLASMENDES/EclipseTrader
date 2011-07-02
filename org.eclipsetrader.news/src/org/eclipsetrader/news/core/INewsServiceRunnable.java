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

package org.eclipsetrader.news.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A runnable which executes a batch of operations within the news service.
 * This interface should be implemented by any class whose instances are
 * intended to be run by INewsService.runInService().
 *
 * @since 1.0
 */
public interface INewsServiceRunnable {

    /**
     * Runs the operation reporting progress to and accepting
     * cancellation requests from the given progress monitor.
     *
     * @param monitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not desired
     * @return the result of the operation
     */
    public IStatus run(IProgressMonitor monitor) throws Exception;
}
