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

package net.sourceforge.eclipsetrader.core.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

public class LogListener implements ILogListener
{

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
     */
    public void logging(IStatus status, String plugin)
    {
        Logger logger = Logger.getLogger(plugin); 
        switch(status.getSeverity())
        {
            case IStatus.INFO:
                logger.info(status.getMessage(), status.getException());
                break;
            case IStatus.WARNING:
                logger.warn(status.getMessage(), status.getException());
                break;
            case IStatus.ERROR:
                logger.error(status.getMessage(), status.getException());
                break;
            default:
                logger.debug(status.getMessage(), status.getException());
                break;
        }
    }
}
