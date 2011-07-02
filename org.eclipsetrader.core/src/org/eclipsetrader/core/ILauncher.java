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

package org.eclipsetrader.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface implemented by service launchers.
 *
 * @since 1.0
 */
public interface ILauncher {

    public String getId();

    public String getName();

    public void launch(IProgressMonitor monitor);

    public void terminate(IProgressMonitor monitor);
}
