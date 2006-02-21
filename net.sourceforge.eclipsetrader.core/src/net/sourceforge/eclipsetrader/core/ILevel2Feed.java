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
 * Interface for Level II data feed plugins.
 */
public interface ILevel2Feed
{
    
    /**
     * Add the given security to the list of securities feeded by this plugin.
     * 
     * @param security the security to add
     */
    public void subscribeLevel2(Security security);

    /**
     * Remove the given security from the list of securities feeded by this plugin.
     * 
     * @param security the security to remove
     */
    public void unSubscribeLevel2(Security security);

    /**
     * Starts the level2 data feed.<br>
     * This method may be called more than once without stop being called first.
     * implementors should take care of this and avoid errors and duplicated threads.
     */
    public void startLevel2();
    
    /**
     * Stops the level2 data feed.<br>
     * This method may be called more than once without start being called first.
     * implementors should take care of this and avoid errors and duplicated threads.
     */
    public void stopLevel2();
    
    /**
     * Take a single snapshot from the feed.
     */
    public void snapshotLevel2();
}
