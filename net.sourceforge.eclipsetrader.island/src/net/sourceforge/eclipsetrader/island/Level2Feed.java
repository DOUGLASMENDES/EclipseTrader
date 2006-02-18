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

package net.sourceforge.eclipsetrader.island;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ILevel2Feed;
import net.sourceforge.eclipsetrader.core.db.Security;

public class Level2Feed implements ILevel2Feed
{
    private Map map = new HashMap();

    public Level2Feed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#subscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void subscribeLevel2(Security security)
    {
        if (map.get(security) == null)
            map.put(security, new Level2FeedThread(security));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#unSubscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void unSubscribeLevel2(Security security)
    {
        try {
            Level2FeedThread thread = (Level2FeedThread)map.get(security);
            if (thread != null)
                thread.stop();
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
        map.remove(security);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#startLevel2()
     */
    public void startLevel2()
    {
        Object[] values = map.values().toArray();
        for (int i = 0; i < values.length; i++)
        {
            Level2FeedThread thread = (Level2FeedThread)values[i];
            thread.start();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#stopLevel2()
     */
    public void stopLevel2()
    {
        Object[] values = map.values().toArray();
        for (int i = 0; i < values.length; i++)
        {
            try {
                Level2FeedThread thread = (Level2FeedThread)values[i];
                thread.stop();
            } catch(Exception e) {
                CorePlugin.logException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#snapshotLevel2()
     */
    public void snapshotLevel2()
    {
    }
}
