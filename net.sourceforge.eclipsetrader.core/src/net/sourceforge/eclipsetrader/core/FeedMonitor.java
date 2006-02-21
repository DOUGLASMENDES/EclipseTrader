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

public class FeedMonitor
{
    private static Map feedMap = new HashMap();
    private static Map securityMap = new HashMap();
    private static Map securityInstances = new HashMap();

    private FeedMonitor()
    {
    }

    public static void monitor(Security security)
    {
        String id = (String)securityMap.get(security);
        if (id != null && (security.getQuoteFeed() == null || !id.equals(security.getQuoteFeed().getId())))
        {
            IFeed feed = (IFeed)feedMap.get(id);
            if (feed != null)
                feed.unSubscribe(security);

            securityInstances.remove(security);
            securityMap.remove(security);
        }
        
        if (security.getQuoteFeed() == null)
            return;

        IFeed feed = (IFeed)feedMap.get(security.getQuoteFeed().getId());
        if (feed == null)
        {
            feed = CorePlugin.createQuoteFeedPlugin(security.getQuoteFeed().getId());
            if (feed == null)
                return;
            feedMap.put(security.getQuoteFeed().getId(), feed);
        }
        
        Integer count = (Integer)securityInstances.get(security);
        if (count == null)
        {
            count = new Integer(0);
            feed.subscribe(security);
            if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING))
                feed.start();
        }
        securityInstances.put(security, new Integer(count.intValue() + 1));
        
        securityMap.put(security, security.getQuoteFeed().getId());
    }

    public static void cancelMonitor(Security security)
    {
        if (security.getQuoteFeed() == null)
            return;
        IFeed feed = (IFeed)feedMap.get(security.getQuoteFeed().getId());
        if (feed == null)
        {
            feed = CorePlugin.createQuoteFeedPlugin(security.getQuoteFeed().getId());
            if (feed == null)
                return;
            feedMap.put(security.getQuoteFeed().getId(), feed);
        }

        Integer count = (Integer)securityInstances.get(security);
        if (count != null)
        {
            if ((count.intValue() - 1) == 0)
            {
                feed.unSubscribe(security);
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
            IFeed feed = (IFeed)feedMap.get((String)iter.next());
            feed.start();
        }
    }
    
    public static synchronized void stop()
    {
        for (Iterator iter = feedMap.keySet().iterator(); iter.hasNext(); )
        {
            IFeed feed = (IFeed)feedMap.get((String)iter.next());
            feed.stop();
        }
    }
    
    public static synchronized void snapshot()
    {
        for (Iterator iter = feedMap.keySet().iterator(); iter.hasNext(); )
        {
            IFeed feed = (IFeed)feedMap.get((String)iter.next());
            feed.snapshot();
        }
    }
    
    public static IFeed getFeed(String id)
    {
        return (IFeed)feedMap.get(id);
    }
}
