/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

public class LogListener implements ILogListener
{

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
     */
    public void logging(IStatus status, String plugin)
    {
        Log log = LogFactory.getLog(plugin); 
        switch(status.getSeverity())
        {
            case IStatus.INFO:
                log.info(status.getMessage(), status.getException());
                break;
            case IStatus.WARNING:
                log.warn(status.getMessage(), status.getException());
                break;
            case IStatus.ERROR:
                log.error(status.getMessage(), status.getException());
                break;
            default:
                log.debug(status.getMessage(), status.getException());
                break;
        }
    }
}
