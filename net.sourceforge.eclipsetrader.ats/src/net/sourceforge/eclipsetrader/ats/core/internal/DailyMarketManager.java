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

package net.sourceforge.eclipsetrader.ats.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.ats.core.IMarketManager;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DailyMarketManager implements IMarketManager {
	List securities = new ArrayList();

	List barListeners = new ArrayList();

	Map securityBarListeners = new HashMap();

	private Log log = LogFactory.getLog(getClass());

	public DailyMarketManager() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void addSecurity(Security security) {
		if (!securities.contains(security)) {
			securities.add(security);
			log.debug("Added security " + security);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void removeSecurity(Security security) {
		if (securities.contains(security)) {
			securities.remove(security);
			log.debug("Removed security " + security);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#start()
	 */
	public void start() {
		Security[] s = (Security[]) securities.toArray(new Security[securities.size()]);
		for (int i = 0; i < s.length; i++) {
			String id = "";
			if (s[i].getHistoryFeed() != null)
				id = s[i].getHistoryFeed().getId();
			IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(id);
			feed.updateHistory(s[i], IHistoryFeed.INTERVAL_DAILY);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#stop()
	 */
	public void stop() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#getBars(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public Bar[] getBars(Security security) {
		return security.getHistory().toArray();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(IBarListener l) {
		barListeners.add(l);
	}

	public void addBarListener(Security security, IBarListener l) {
		List list = (List) securityBarListeners.get(security);
		if (list == null) {
			list = new ArrayList();
			securityBarListeners.put(security, list);
		}
		list.add(l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(IBarListener l) {
		barListeners.remove(l);
	}

	public void removeBarListener(Security security, IBarListener l) {
		List list = (List) securityBarListeners.get(security);
		if (list != null)
			list.remove(l);
	}

	protected void fireBarOpenEvent(BarEvent e) {
		log.trace("Notifying bar open at " + e.price + " event for " + e.security);

		List list = (List) securityBarListeners.get(e.security);
		if (list != null) {
			IBarListener[] l = (IBarListener[]) list.toArray(new IBarListener[0]);
			for (int i = 0; i < l.length; i++)
				l[i].barOpen(e);
		}

		IBarListener[] l = (IBarListener[]) barListeners.toArray(new IBarListener[0]);
		for (int i = 0; i < l.length; i++)
			l[i].barOpen(e);
	}

	protected void fireBarCloseEvent(BarEvent e) {
		log.trace("Notifying bar close at " + e.price + " event for " + e.security);

		List list = (List) securityBarListeners.get(e.security);
		if (list != null) {
			IBarListener[] l = (IBarListener[]) list.toArray(new IBarListener[0]);
			for (int i = 0; i < l.length; i++)
				l[i].barClose(e);
		}

		IBarListener[] l = (IBarListener[]) barListeners.toArray(new IBarListener[0]);
		for (int i = 0; i < l.length; i++)
			l[i].barClose(e);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addMarketListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(Security security, IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeMarketListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(Security security, IMarketListener l) {
	}
}
