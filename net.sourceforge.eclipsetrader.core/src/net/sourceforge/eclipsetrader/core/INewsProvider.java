/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core;

import net.sourceforge.eclipsetrader.core.db.Security;


/**
 * Interface for news data provider plugins.
 */
public interface INewsProvider
{
    
    /**
     * Starts the news feed.<br>
     * This method may be called more than once without stop being called first.
     * implementors should take care of this and avoid errors and duplicated threads.
     */
    public void start();
    
    /**
     * Stops the news feed.<br>
     * This method may be called more than once without start being called first.
     * implementors should take care of this and avoid errors and duplicated threads.
     */
    public void stop();

    /**
     * Take a single snapshot of the available news.<br>
     * This method may be called at any time with or without starting or stopping
     * the feed first.
     */
    public void snapshot();

    /**
     * Take a single snapshot of the available news for the given security.
     * This method may be called at any time with or without starting or stopping
     * the feed first.
     * 
     * @param security - the security
     */
    public void snapshot(Security security);
}
