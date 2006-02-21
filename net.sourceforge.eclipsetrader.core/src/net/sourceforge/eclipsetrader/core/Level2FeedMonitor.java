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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.db.Security;

public class Level2FeedMonitor
{
    private static Map feedMap = new HashMap();
    private static Map securityMap = new HashMap();
    private static Map securityInstances = new HashMap();

    private Level2FeedMonitor()
    {
    }

    public static void monitor(Security security)
    {
        String id = (String)securityMap.get(security);
        if (id != null && (security.getLevel2Feed() == null || !id.equals(security.getLevel2Feed().getId())))
        {
            ILevel2Feed feed = (ILevel2Feed)feedMap.get(id);
            if (feed != null)
                feed.unSubscribeLevel2(security);

            securityInstances.remove(security);
            securityMap.remove(security);
        }
        
        if (security.getLevel2Feed() == null)
            return;

        ILevel2Feed feed = (ILevel2Feed)feedMap.get(security.getLevel2Feed().getId());
        if (feed == null)
        {
            feed = CorePlugin.createLevel2FeedPlugin(security.getLevel2Feed().getId());
            if (feed == null)
                return;
            feedMap.put(security.getLevel2Feed().getId(), feed);
        }
        
        Integer count = (Integer)securityInstances.get(security);
        if (count == null)
        {
            count = new Integer(0);
            feed.subscribeLevel2(security);
            if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING))
                feed.startLevel2();
        }
        securityInstances.put(security, new Integer(count.intValue() + 1));
        
        securityMap.put(security, security.getLevel2Feed().getId());
    }

    public static void cancelMonitor(Security security)
    {
        if (security.getLevel2Feed() == null)
            return;
        ILevel2Feed feed = (ILevel2Feed)feedMap.get(security.getLevel2Feed().getId());
        if (feed == null)
        {
            feed = CorePlugin.createLevel2FeedPlugin(security.getLevel2Feed().getId());
            if (feed == null)
                return;
            feedMap.put(security.getLevel2Feed().getId(), feed);
        }

        Integer count = (Integer)securityInstances.get(security);
        if (count != null)
        {
            if ((count.intValue() - 1) == 0)
            {
                feed.unSubscribeLevel2(security);
                securityInstances.remove(security);
                securityMap.remove(security);
            }
            else
                securityInstances.put(security, new Integer(count.intValue() - 1));
        }
    }
    
    public static synchronized void start()
    {
        for (Iterator iter = feedMap.keySet().iterator(); iter.hasNext(); )
        {
            ILevel2Feed feed = (ILevel2Feed)feedMap.get((String)iter.next());
            feed.startLevel2();
        }
    }
    
    public static synchronized void stop()
    {
        for (Iterator iter = feedMap.keySet().iterator(); iter.hasNext(); )
        {
            ILevel2Feed feed = (ILevel2Feed)feedMap.get((String)iter.next());
            feed.stopLevel2();
        }
    }
    
    public static synchronized void snapshot()
    {
        for (Iterator iter = feedMap.keySet().iterator(); iter.hasNext(); )
        {
            ILevel2Feed feed = (ILevel2Feed)feedMap.get((String)iter.next());
            feed.snapshotLevel2();
        }
    }
    
    public static ILevel2Feed getFeed(String id)
    {
        return (ILevel2Feed)feedMap.get(id);
    }
}
