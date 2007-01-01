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

package net.sourceforge.eclipsetrader.opentick;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.eclipsetrader.core.ILevel2Feed;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.opentick.internal.Client;

import org.apache.commons.logging.LogFactory;

public class Level2Feed implements ILevel2Feed
{
    boolean running = false;
    Set map = new HashSet();
    Client client;

    public Level2Feed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#subscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void subscribeLevel2(Security security)
    {
        if (!map.contains(security))
        {
            map.add(security);

            try {
                if (client != null && running)
                    client.requestBookStream(security);
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#unSubscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void unSubscribeLevel2(Security security)
    {
        if (map.contains(security))
        {
            map.remove(security);

            try {
                if (client != null && running)
                    client.cancelBookStream(security);
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#startLevel2()
     */
    public void startLevel2()
    {
        if (!running)
        {
            client = Client.getInstance();
            try {
                client.login();
                for (Iterator iter = map.iterator(); iter.hasNext(); )
                    client.requestBookStream((Security)iter.next());
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
            
            running = true;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#stopLevel2()
     */
    public void stopLevel2()
    {
        if (running)
        {
            try {
                for (Iterator iter = map.iterator(); iter.hasNext(); )
                    client.cancelBookStream((Security)iter.next());
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
            client.dispose();
            
            running = false;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#snapshotLevel2()
     */
    public void snapshotLevel2()
    {
    }
}
