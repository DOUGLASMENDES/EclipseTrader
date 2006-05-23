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

package net.sourceforge.eclipsetrader.trading;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.db.Security;

/**
 * Base abstract class for alert plugins.
 */
public abstract class AlertPlugin
{
    private Security security;
    private Date lastSeen;

    public AlertPlugin()
    {
    }
    
    public String getDescription()
    {
        return "";
    }

    public void init(Security security, Map params)
    {
        this.security = security;
        init(params);
    }
    
    public void init(Map params)
    {
    }

    public abstract boolean apply();
    
    public boolean isSeenToday()
    {
        if (getLastSeen() != null)
        {
            Calendar today = Calendar.getInstance();
            Calendar last = Calendar.getInstance();
            last.setTime(getLastSeen());
            if (today.get(Calendar.DAY_OF_MONTH) == last.get(Calendar.DAY_OF_MONTH) &&
                today.get(Calendar.MONTH) == last.get(Calendar.MONTH) &&
                today.get(Calendar.YEAR) == last.get(Calendar.YEAR))
                return true;
        }
        
        return false;
    }

    public Security getSecurity()
    {
        return security;
    }

    public Date getLastSeen()
    {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen)
    {
        this.lastSeen = lastSeen;
    }
}
