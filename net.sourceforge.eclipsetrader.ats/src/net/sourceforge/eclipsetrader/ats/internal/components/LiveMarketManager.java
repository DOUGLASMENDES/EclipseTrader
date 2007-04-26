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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.ats.core.IMarketManager;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.ats.core.events.MarketEvent;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines the set of securities that is seen and processed by a strategy.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class LiveMarketManager implements IMarketManager {
	/**
	 * The managed securities.
	 */
	List securities = new ArrayList();

	/**
	 * Bars period.
	 */
	int minutes = 1;

	/**
	 * Mapping between securities and bar data.
	 */
	Map map = new HashMap();

	Calendar barTime = Calendar.getInstance();

	List marketListeners = new ArrayList();

	Map securityMarketListeners = new HashMap();

	List barListeners = new ArrayList();

	Map securityBarListeners = new HashMap();

	Observer quoteObserver = new Observer() {
		public void update(Observable o, Object arg) {
			if (arg instanceof Security) {
				MarketEvent e = new MarketEvent();
				e.security = (Security) arg;
				fireMarketDataEvent(e);

				updateBarData((Security) arg);
			}
		}
	};

	Observer level2Observer = new Observer() {
		public void update(Observable o, Object arg) {
			if (arg instanceof Security) {
				MarketEvent e = new MarketEvent();
				e.security = (Security) arg;
				fireMarketDepthEvent(e);
			}
		}
	};

	private Log log = LogFactory.getLog(getClass());

	public LiveMarketManager() {
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public void addSecurity(Security security) {
		if (!securities.contains(security)) {
			security.getQuoteMonitor().addObserver(quoteObserver);
			security.getLevel2Monitor().addObserver(level2Observer);
			map.put(security, new MapData());
			securities.add(security);
			log.debug("Added security " + security);
		}
	}

	public void removeSecurity(Security security) {
		security.getQuoteMonitor().deleteObserver(quoteObserver);
		security.getLevel2Monitor().deleteObserver(level2Observer);
		map.remove(security);
		if (securities.contains(security)) {
			securities.remove(security);
			log.debug("Removed security " + security);
		}
	}

	public Security[] getSecurities() {
		return (Security[]) securities.toArray(new Security[securities.size()]);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#start()
	 */
	public void start() {
		Security[] s = getSecurities();
		for (int i = 0; i < s.length; i++) {
			String id = "";
			if (s[i].getHistoryFeed() != null)
				id = s[i].getHistoryFeed().getId();
			IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(id);
			feed.updateHistory(s[i], IHistoryFeed.INTERVAL_MINUTE);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#stop()
	 */
	public void stop() {
	}

	public void dispose() {
		Security[] list = getSecurities();
		for (int i = 0; i < list.length; i++) {
			list[i].getQuoteMonitor().deleteObserver(quoteObserver);
			list[i].getLevel2Monitor().deleteObserver(level2Observer);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#getBars(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public Bar[] getBars(Security security) {
		return security.getIntradayHistory().toArray();
	}

	void updateBarData(Security security) {
		Quote quote = security.getQuote();
		if (quote == null || quote.getDate() == null)
			return;

		barTime.setTime(quote.getDate());
		barTime.set(Calendar.SECOND, 0);
		barTime.set(Calendar.MILLISECOND, 0);

		int quoteTime = barTime.get(Calendar.HOUR_OF_DAY) * 60 + barTime.get(Calendar.MINUTE);
		if (quoteTime < security.getBeginTime() || quoteTime > security.getEndTime())
			return;

		MapData data = (MapData) map.get(security);
		if (data.bar != null && data.bar.getDate() != null) {
			if (barTime.after(data.nextBarTime) || barTime.equals(data.nextBarTime)) {
				BarEvent e = new BarEvent(data.bar.getDate(), security, data.bar, data.bar.getClose());
				fireBarCloseEvent(e);

				data.bar = null;
			}
		} else if (data.bar == null) {
			data.bar = new Bar();
			data.bar.setOpen(quote.getLast());
			data.bar.setHigh(quote.getLast());
			data.bar.setLow(quote.getLast());
			data.bar.setClose(quote.getLast());
			data.volume = quote.getVolume();
			barTime.add(Calendar.MINUTE, -(barTime.get(Calendar.MINUTE) % minutes));
			data.bar.setDate(barTime.getTime());
			data.nextBarTime.setTime(data.bar.getDate());
			data.nextBarTime.add(Calendar.MINUTE, minutes);

			BarEvent e = new BarEvent(data.bar.getDate(), security, data.bar.getOpen());
			fireBarOpenEvent(e);
		}

		if (data.bar != null) {
			if (quote.getLast() > data.bar.getHigh())
				data.bar.setHigh(quote.getLast());
			if (quote.getLast() < data.bar.getLow())
				data.bar.setLow(quote.getLast());
			data.bar.setClose(quote.getLast());
			data.bar.setVolume(quote.getVolume() - data.volume);
		}
	}

	public void addMarketListener(IMarketListener l) {
		marketListeners.add(l);
	}

	public void removeMarketListener(IMarketListener l) {
		marketListeners.remove(l);
	}

	public void addMarketListener(Security security, IMarketListener l) {
		List list = (List) securityMarketListeners.get(security);
		if (list == null) {
			list = new ArrayList();
			securityMarketListeners.put(security, list);
		}
		list.add(l);
	}

	public void removeMarketListener(Security security, IMarketListener l) {
		List list = (List) securityMarketListeners.get(security);
		if (list != null)
			list.remove(l);
	}

	protected void fireMarketDataEvent(MarketEvent e) {
		log.trace("Firing market data event for " + e.security);

		List list = (List) securityMarketListeners.get(e.security);
		if (list != null) {
			IMarketListener[] l = (IMarketListener[]) list.toArray(new IMarketListener[0]);
			for (int i = 0; i < l.length; i++)
				l[i].marketData(e);
		}

		IMarketListener[] l = (IMarketListener[]) marketListeners.toArray(new IMarketListener[0]);
		for (int i = 0; i < l.length; i++)
			l[i].marketData(e);
	}

	protected void fireMarketDepthEvent(MarketEvent e) {
		log.trace("Firing market data event for " + e.security);

		List list = (List) securityMarketListeners.get(e.security);
		if (list != null) {
			IMarketListener[] l = (IMarketListener[]) list.toArray(new IMarketListener[0]);
			for (int i = 0; i < l.length; i++)
				l[i].marketDepth(e);
		}

		IMarketListener[] l = (IMarketListener[]) marketListeners.toArray(new IMarketListener[0]);
		for (int i = 0; i < l.length; i++)
			l[i].marketDepth(e);
	}

	public void addBarListener(IBarListener l) {
		barListeners.add(l);
	}

	public void removeBarListener(IBarListener l) {
		barListeners.remove(l);
	}

	public void addBarListener(Security security, IBarListener l) {
		List list = (List) securityBarListeners.get(security);
		if (list == null) {
			list = new ArrayList();
			securityBarListeners.put(security, list);
		}
		list.add(l);
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

	private class MapData {
		Bar bar;

		long volume;

		Calendar nextBarTime;

		MapData() {
			nextBarTime = Calendar.getInstance();
			nextBarTime.set(Calendar.SECOND, 0);
			nextBarTime.set(Calendar.MILLISECOND, 0);
		}
	}
}
