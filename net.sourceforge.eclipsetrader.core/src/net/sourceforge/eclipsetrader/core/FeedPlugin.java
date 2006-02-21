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
 * Base abstract class for all feed plugins.
 */
public abstract class FeedPlugin
{
    public static final int INTERVAL_MINUTE1 = 0;
    public static final int INTERVAL_MINUTE2 = 2;
    public static final int INTERVAL_MINUTE5 = 3;
    public static final int INTERVAL_MINUTE10 = 4;
    public static final int INTERVAL_MINUTE15 = 5;
    public static final int INTERVAL_MINUTE30 = 6;
    public static final int INTERVAL_MINUTE60 = 7;
    public static final int INTERVAL_DAILY = 8;
    public static final int INTERVAL_WEEKLY = 9;
    public static final int INTERVAL_MONTHLY = 10;

    public FeedPlugin()
    {
    }

    public void updateHistory(Security security, int interval)
    {
    }

    public void subscribe(Security security)
    {
    }

    public void unSubscribe(Security security)
    {
    }
    
    public void subscribeLevelII(Security security)
    {
    }

    public void unSubscribeLevelII(Security security)
    {
    }
}
