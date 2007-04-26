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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.ats.core.IMarketManager;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.History;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.ListenerList;

/**
 * Market manager implementation used to backtest a trading system.
 * 
 * <p>The default implementation simulates the bar events for a given period
 * by reading the currently available historical bars data.</p>
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class BacktestMarketManager implements IMarketManager, Runnable {
	Date start;

	Date end;

	BacktestExecutionManager executionManager;

	int initialBars = 0;

	List securities = new ArrayList();

	ListenerList barListeners = new ListenerList();

	Map securityBarListeners = new HashMap();

	Map securityBars = new HashMap();

	History[] history;

	int barCount = 0;

	public BacktestMarketManager(Date start, Date end, BacktestExecutionManager executionManager) {
		this.start = start;
		this.end = end;
		this.executionManager = executionManager;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#start()
	 */
	public void start() {
		securityBars.clear();

		history = new History[securities.size()];
		for (int i = 0; i < history.length; i++) {
			history[i] = ((Security) securities.get(i)).getHistory();
			securityBars.put(securities.get(i), new ArrayList());
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#stop()
	 */
	public void stop() {
		history = null;
		securityBars.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Calendar day = Calendar.getInstance();
		day.setTime(start);
		day.set(Calendar.HOUR, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);

		Calendar lastDay = Calendar.getInstance();
		lastDay.setTime(end);
		lastDay.set(Calendar.HOUR, 0);
		lastDay.set(Calendar.MINUTE, 0);
		lastDay.set(Calendar.SECOND, 0);
		lastDay.set(Calendar.MILLISECOND, 0);

		barCount = 0;
		while (!day.after(lastDay)) {
			boolean gotBar = false;

			// Notify the bar open events to the execution manager
			for (int i = 0; i < history.length; i++) {
				Bar bar = history[i].get(day.getTime());
				if (bar != null) {
					BarEvent e = new BarEvent(bar.getDate(), (Security) securities.get(i), bar.getOpen());
					executionManager.barOpen(e);
					gotBar = true;
				}
			}

			// Notify the bar open events to the event listeners
			for (int i = 0; i < history.length; i++) {
				Bar bar = history[i].get(day.getTime());
				if (bar != null) {
					BarEvent e = new BarEvent(bar.getDate(), (Security) securities.get(i), bar.getOpen());
					fireBarOpenEvent(e);
				}
			}

			// Notify the bar close events to the execution manager
			for (int i = 0; i < history.length; i++) {
				Bar bar = history[i].get(day.getTime());
				if (bar != null) {
					BarEvent e = new BarEvent(bar.getDate(), (Security) securities.get(i), bar, bar.getOpen());
					executionManager.barClose(e);

					// Adds the bar to the security history
					ArrayList list = (ArrayList) securityBars.get(securities.get(i));
					list.add(bar);
				}
			}

			// Notify the bar close events to the event listeners
			for (int i = 0; i < history.length; i++) {
				Bar bar = history[i].get(day.getTime());
				if (bar != null) {
					if (barCount >= initialBars) {
						BarEvent e = new BarEvent(bar.getDate(), (Security) securities.get(i), bar, bar.getOpen());
						fireBarCloseEvent(e);
					}
				}
			}

			if (gotBar)
				barCount++;

			day.add(Calendar.DATE, 1);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#getBars(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public Bar[] getBars(Security security) {
		ArrayList list = (ArrayList) securityBars.get(security);
		if (list != null)
			return (Bar[]) list.toArray(new Bar[0]);
		return new Bar[0];
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(IBarListener l) {
		barListeners.add(l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(Security security, IBarListener l) {
		ListenerList list = (ListenerList) securityBarListeners.get(security);
		if (list == null) {
			list = new ListenerList();
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

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeBarListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(Security security, IBarListener l) {
		ListenerList list = (ListenerList) securityBarListeners.get(security);
		if (list != null)
			list.remove(l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void addSecurity(Security security) {
		if (!securities.contains(security))
			securities.add(security);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#dispose()
	 */
	public void dispose() {
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
		securities.remove(security);
	}

	protected void fireBarOpenEvent(BarEvent e) {
		ListenerList list = (ListenerList) securityBarListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IBarListener) l[i]).barOpen(e);
		}

		Object[] l = barListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IBarListener) l[i]).barOpen(e);
	}

	protected void fireBarCloseEvent(BarEvent e) {
		ListenerList list = (ListenerList) securityBarListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IBarListener) l[i]).barClose(e);
		}

		Object[] l = barListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IBarListener) l[i]).barClose(e);
	}

	public int getBarCount() {
		return barCount;
	}
}
