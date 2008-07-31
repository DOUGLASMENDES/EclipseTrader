/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.internal.ats;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactory;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.trading.Activator;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BarFactory implements IBarFactory {
	private ISecurity security;
	private TimeSpan timeSpan;
	private IPricingEnvironment pricingEnvironment;

	private ListenerList listeners = new ListenerList();

	private Date date;

	private Double open;
	private Double high;
	private Double low;
	private Double close;

	private Date dateClose;

	private IPricingListener pricingListener = new IPricingListener() {
        public void pricingUpdate(PricingEvent event) {
        	for (PricingDelta delta : event.getDelta()) {
        		if (delta.getSecurity() == security && delta.getNewValue() instanceof ITrade)
        			processTrade((ITrade) delta.getNewValue());
        	}
        }
	};

	private class BarCloseTimerTask extends TimerTask {
		private Date date;

		public BarCloseTimerTask(Date date) {
	        this.date = date;
        }

		/* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
        	if (BarFactory.this.date == date)
				fireBarCloseEvent();
        }
	}

	private Timer timer;

	public BarFactory(ISecurity security, TimeSpan timeSpan, IPricingEnvironment pricingEnvironment) {
        this.security = security;
        this.timeSpan = timeSpan;
        this.pricingEnvironment = pricingEnvironment;

        this.pricingEnvironment.addPricingListener(pricingListener);
        this.timer = new Timer(true);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IBarFactory#getTimeSpan()
     */
    public TimeSpan getTimeSpan() {
	    return timeSpan;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IBarFactory#getBars(org.eclipsetrader.core.feed.TimeSpan)
     */
    public IOHLC[] getBars(TimeSpan backfillTimeSpan) {
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.MINUTE, 0);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);

    	Date from = c.getTime();

    	if (backfillTimeSpan.getUnits() == Units.Months)
    		c.add(Calendar.MONTH, - backfillTimeSpan.getLength());
    	else if (backfillTimeSpan.getUnits() == Units.Days)
    		c.add(Calendar.DATE, - backfillTimeSpan.getLength());
    	else if (backfillTimeSpan.getUnits() == Units.Minutes)
    		c.add(Calendar.MINUTE, - backfillTimeSpan.getLength());

    	Date to = c.getTime();

	    return getBars(from, to);
    }

    protected IOHLC[] getBars(Date from, Date to) {
    	IOHLC[] bars = null;

    	BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);

			IHistory history = repositoryService.getHistoryFor(security);
			if (history != null) {
				history = history.getSubset(from, to, timeSpan);
				if (history != null)
					bars = history.getOHLC();
			}

			context.ungetService(serviceReference);
		}

		return bars;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IBarFactory#addBarFactoryListener(org.eclipsetrader.core.ats.IBarFactoryListener)
     */
    public void addBarFactoryListener(IBarFactoryListener listener) {
		listeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IBarFactory#removeBarFactoryListener(org.eclipsetrader.core.ats.IBarFactoryListener)
     */
    public void removeBarFactoryListener(IBarFactoryListener listener) {
		listeners.remove(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IBarFactory#dispose()
     */
    public void dispose() {
    	timer.cancel();
        pricingEnvironment.removePricingListener(pricingListener);
        listeners.clear();
    }

	protected void processTrade(ITrade trade) {
		Calendar c = Calendar.getInstance();
		c.setTime(trade.getTime());
		c.set(Calendar.MILLISECOND, 0);

		if (timeSpan.getUnits() == TimeSpan.Units.Days) {
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
		}

		if (date != null && dateClose != null) {
			Date time = c.getTime();
			if (!time.before(date) && time.before(dateClose)) {
				if (high == null || trade.getPrice() > high)
					high = trade.getPrice();
				if (low == null || trade.getPrice() < low)
					low = trade.getPrice();
				close = trade.getPrice();
			}

			if (!time.before(dateClose))
				fireBarCloseEvent();
		}

		if (date == null) {
			if (timeSpan.getUnits() == TimeSpan.Units.Minutes) {
				int round = c.get(Calendar.MINUTE) % timeSpan.getLength();
				c.add(Calendar.MINUTE, -round);
				date = c.getTime();
				c.add(Calendar.MINUTE, timeSpan.getLength());
				dateClose = c.getTime();
			}
			else if (timeSpan.getUnits() == TimeSpan.Units.Days) {
				int round = c.get(Calendar.DAY_OF_MONTH) % timeSpan.getLength();
				c.add(Calendar.DAY_OF_MONTH, -round);
				date = c.getTime();
				c.add(Calendar.DAY_OF_MONTH, timeSpan.getLength());
				dateClose = c.getTime();
			}
			open = trade.getPrice();
			high = trade.getPrice();
			low = trade.getPrice();
			close = trade.getPrice();

			fireBarOpenEvent();

			timer.schedule(new BarCloseTimerTask(date), dateClose);
		}
	}

	protected void fireBarOpenEvent() {
		BarFactoryEvent event = new BarFactoryEvent(security, timeSpan, date, open);
		event.factory = this;

		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IBarFactoryListener) l[i]).barOpen(event);
	}

	protected void fireBarCloseEvent() {
		BarFactoryEvent event = new BarFactoryEvent(security, timeSpan, date, open, high, low, close);
		event.factory = this;
		date = null;

		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IBarFactoryListener) l[i]).barClose(event);
	}
}
