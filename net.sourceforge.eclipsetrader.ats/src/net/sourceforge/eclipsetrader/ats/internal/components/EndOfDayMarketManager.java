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

package net.sourceforge.eclipsetrader.ats.internal.components;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.ats.core.IMarketManager;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.History;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.ListenerList;

public class EndOfDayMarketManager implements IMarketManager {
	/**
	 * The managed securities.
	 */
	Map securities = new HashMap();

	/**
	 * Generic bar listeners.
	 */
	ListenerList listeners = new ListenerList();

	/**
	 * The logger instance.
	 */
	private Log log = LogFactory.getLog(getClass());

	public EndOfDayMarketManager() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(IBarListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(Security security, IBarListener l) {
		MapData data = (MapData) securities.get(security);
		if (data != null)
			data.listeners.add(l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(IMarketListener l) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addMarketListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(Security security, IMarketListener l) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void addSecurity(Security security) {
		if (securities.get(security) == null) {
			securities.put(security, new MapData(security));
			log.debug("Added security " + security);
		}
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
		MapData data = (MapData) securities.get(security);
		if (data != null)
			return data.getHistory().toArray();
		return new Bar[0];
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(IBarListener l) {
		listeners.remove(l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeBarListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(Security security, IBarListener l) {
		MapData data = (MapData) securities.get(security);
		if (data != null)
			data.listeners.remove(l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(IMarketListener l) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeMarketListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(Security security, IMarketListener l) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void removeSecurity(Security security) {
		if (securities.get(security) != null) {
			securities.remove(security);
			log.debug("Removed security " + security);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#start()
	 */
	public void start() {
		MapData[] data = (MapData[]) securities.values().toArray(new MapData[securities.size()]);
		for (int i = 0; i < data.length; i++)
			data[i].start();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#stop()
	 */
	public void stop() {
		MapData[] data = (MapData[]) securities.values().toArray(new MapData[securities.size()]);
		for (int i = 0; i < data.length; i++)
			data[i].stop();
	}

	public Security[] getSecurities() {
		return (Security[]) securities.keySet().toArray(new Security[securities.size()]);
	}

	private class MapData implements Observer {
		Security security;

		History history;

		ListenerList listeners = new ListenerList();

		Date lastDate;

		MapData(Security security) {
			this.security = security;
		}

		void start() {
			history = security.getHistory();
			if (history.getLast() != null)
				lastDate = history.getLast().getDate();
			history.addObserver(this);
		}

		void stop() {
			history.deleteObserver(this);
			history = null;
		}

		public History getHistory() {
			return history;
		}

		/* (non-Javadoc)
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(Observable o, Object arg) {
			if (history.size() != 0) {
				int index = 0;
				if (lastDate != null) {
					index = history.indexOf(lastDate);
					if (index == -1)
						index = 0;
				}

				for (; index < history.size(); index++) {
					Bar bar = history.get(index);
					fireBarOpenEvent(new BarEvent(bar.getDate(), security, bar.getOpen()));
					fireBarCloseEvent(new BarEvent(bar.getDate(), security, bar, bar.getOpen()));
				}

				lastDate = history.getLast().getDate();
			} else
				lastDate = null;
		}

		protected void fireBarOpenEvent(BarEvent e) {
			log.trace("Notifying bar open at " + e.price + " event for " + e.security);

			Object[] l = listeners.getListeners();
			for (int i = 0; i < l.length; i++)
				((IBarListener) l[i]).barOpen(e);

			l = EndOfDayMarketManager.this.listeners.getListeners();
			for (int i = 0; i < l.length; i++)
				((IBarListener) l[i]).barOpen(e);
		}

		protected void fireBarCloseEvent(BarEvent e) {
			log.trace("Notifying bar close at " + e.price + " event for " + e.security);

			Object[] l = listeners.getListeners();
			for (int i = 0; i < l.length; i++)
				((IBarListener) l[i]).barClose(e);

			l = EndOfDayMarketManager.this.listeners.getListeners();
			for (int i = 0; i < l.length; i++)
				((IBarListener) l[i]).barClose(e);
		}
	}
}
