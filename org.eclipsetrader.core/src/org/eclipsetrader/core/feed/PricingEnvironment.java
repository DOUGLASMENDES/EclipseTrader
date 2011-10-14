/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.feed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;

/**
 * Default implementation of the <code>IPricingEnvironment</code> interface.
 * <p>Clients sets pricing values using setters methods.</p>
 *
 * @since 1.0
 */
public class PricingEnvironment implements IPricingEnvironment {

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private boolean doNotify = true;

    class PricingStatus {

        ITrade trade;
        IQuote quote;
        ITodayOHL todayOHL;
        ILastClose lastClose;
        IBook book;
        List<PricingDelta> deltas = new ArrayList<PricingDelta>();
    }

    private Map<ISecurity, PricingStatus> map = new HashMap<ISecurity, PricingStatus>();

    private final ILock lock;

    public PricingEnvironment() {
        lock = Job.getJobManager().newLock();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#dispose()
     */
    @Override
    public void dispose() {
        listeners.clear();
        map.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#addPricingEnvironmentListener(org.eclipsetrader.core.feed.IPricingListener)
     */
    @Override
    public void addPricingListener(IPricingListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#removePricingEnvironmentListener(org.eclipsetrader.core.feed.IPricingListener)
     */
    @Override
    public void removePricingListener(IPricingListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public ITrade getTrade(ISecurity security) {
        return map.get(security) != null ? map.get(security).trade : null;
    }

    /**
     * Sets a new trade for a security.
     *
     * @param security the security.
     * @param trade the new trade value.
     */
    public void setTrade(ISecurity security, ITrade trade) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        Object oldValue = status.trade;
        if (oldValue == null && trade != null || oldValue != null && !oldValue.equals(trade)) {
            status.trade = trade;
            status.deltas.add(new PricingDelta(oldValue, trade));
            if (doNotify) {
                notifyListeners();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getQuote(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IQuote getQuote(ISecurity security) {
        return map.get(security) != null ? map.get(security).quote : null;
    }

    /**
     * Sets a new quote for a security.
     *
     * @param security the security.
     * @param quote the new quote value.
     */
    public void setQuote(ISecurity security, IQuote quote) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        Object oldValue = status.quote;
        if (oldValue == null && quote != null || oldValue != null && !oldValue.equals(quote)) {
            status.quote = quote;
            status.deltas.add(new PricingDelta(oldValue, quote));
            if (doNotify) {
                notifyListeners();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getTodayOHL(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public ITodayOHL getTodayOHL(ISecurity security) {
        return map.get(security) != null ? map.get(security).todayOHL : null;
    }

    /**
     * Sets a new today's OHL for a security.
     *
     * @param security the security.
     * @param todayOHL the new OHL value.
     */
    public void setTodayOHL(ISecurity security, ITodayOHL todayOHL) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        Object oldValue = status.todayOHL;
        if (oldValue == null && todayOHL != null || oldValue != null && !oldValue.equals(todayOHL)) {
            status.todayOHL = todayOHL;
            status.deltas.add(new PricingDelta(oldValue, todayOHL));
            if (doNotify) {
                notifyListeners();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getLastClose(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public ILastClose getLastClose(ISecurity security) {
        return map.get(security) != null ? map.get(security).lastClose : null;
    }

    /**
     * Sets a new last close price for a security.
     *
     * @param security the security.
     * @param lastClose the new last close value.
     */
    public void setLastClose(ISecurity security, ILastClose lastClose) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        Object oldValue = status.lastClose;
        if (oldValue == null && lastClose != null || oldValue != null && !oldValue.equals(lastClose)) {
            status.lastClose = lastClose;
            status.deltas.add(new PricingDelta(oldValue, lastClose));
            if (doNotify) {
                notifyListeners();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingEnvironment#getBook(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IBook getBook(ISecurity security) {
        return map.get(security) != null ? map.get(security).book : null;
    }

    /**
     * Sets the new level II book values for a security.
     *
     * @param security the security.
     * @param book the new book values.
     */
    public void setBook(ISecurity security, IBook book) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        Object oldValue = status.book;
        if (oldValue == null && book != null || oldValue != null && !oldValue.equals(book)) {
            status.book = book;
            status.deltas.add(new PricingDelta(oldValue, book));
            if (doNotify) {
                notifyListeners();
            }
        }
    }

    public void setBarOpen(ISecurity security, IBarOpen bar) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        status.deltas.add(new PricingDelta(null, bar));
        if (doNotify) {
            notifyListeners();
        }
    }

    public void setBar(ISecurity security, IBar bar) {
        PricingStatus status = map.get(security);
        if (status == null) {
            status = new PricingStatus();
            map.put(security, status);
        }
        status.deltas.add(new PricingDelta(null, bar));
        if (doNotify) {
            notifyListeners();
        }
    }

    /**
     * Updates a set of quotes in a single batch. Events are notified to the listeners
     * when the runnable returns.
     *
     * @param runnable the runnable to run.
     */
    public void runBatch(Runnable runnable) {
        try {
            lock.acquire();
            doNotify = false;
            try {
                runnable.run();
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment batch", e); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment batch", e); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
            }
            notifyListeners();
            doNotify = true;
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment batch", e); //$NON-NLS-1$
            CoreActivator.getDefault().getLog().log(status);
        } finally {
            lock.release();
        }
    }

    /**
     * Notify all listeners of changes occurred since the last notification.
     */
    protected void notifyListeners() {
        Object[] l = listeners.getListeners();
        for (ISecurity security : map.keySet()) {
            PricingStatus status = map.get(security);
            if (status == null || status.deltas.size() == 0) {
                continue;
            }
            final PricingEvent event = new PricingEvent(security, status.deltas.toArray(new PricingDelta[status.deltas.size()]));
            for (int i = 0; i < l.length; i++) {
                final IPricingListener listener = (IPricingListener) l[i];
                SafeRunner.run(new ISafeRunnable() {

                    @Override
                    public void run() throws Exception {
                        listener.pricingUpdate(event);
                    }

                    @Override
                    public void handleException(Throwable exception) {
                        Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running pricing environment listener", exception); //$NON-NLS-1$
                        CoreActivator.log(status);
                    }
                });
            }
            status.deltas.clear();
        }
    }

    PricingStatus getStatus(ISecurity security) {
        return map.get(security);
    }
}
