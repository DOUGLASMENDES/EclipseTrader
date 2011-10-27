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

package org.eclipsetrader.core.internal.ats;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

public class BarFactory {

    final Map<ISecurity, Set<Data>> map = new HashMap<ISecurity, Set<Data>>();
    private final Timer timer;

    private final ListenerList listeners = new ListenerList();

    class Data {

        final ISecurity security;
        final TimeSpan timeSpan;

        Date dateOpen;

        Double open;
        Double high;
        Double low;
        Double close;
        Long volume;

        Date dateClose;

        public Data(ISecurity security, TimeSpan timeSpan) {
            this.security = security;
            this.timeSpan = timeSpan;
        }
    }

    private class BarCloseTimerTask extends TimerTask {

        private final Data data;
        private final Date date;

        public BarCloseTimerTask(Data data) {
            this.data = data;
            this.date = data.dateOpen;
        }

        @Override
        public void run() {
            if (data.dateOpen == date) {
                fireBarCloseEvent(data);
            }
        }
    }

    public BarFactory() {
        this.timer = new Timer(true);
    }

    public void add(ISecurity security, TimeSpan timeSpan) {
        Set<Data> set = map.get(security);
        if (set == null) {
            set = new HashSet<Data>();
            map.put(security, set);
        }
        set.add(new Data(security, timeSpan));
    }

    public void addBarFactoryListener(IBarFactoryListener listener) {
        listeners.add(listener);
    }

    public void removeBarFactoryListener(IBarFactoryListener listener) {
        listeners.remove(listener);
    }

    public void dispose() {
        timer.cancel();
        listeners.clear();
    }

    public void pricingUpdate(PricingEvent event) {
        Set<Data> set = map.get(event.getSecurity());
        if (set == null) {
            return;
        }
        for (PricingDelta delta : event.getDelta()) {
            if (delta.getNewValue() instanceof ITrade) {
                for (Data data : set) {
                    processTrade(data, (ITrade) delta.getNewValue());
                }
            }
        }
    }

    private void processTrade(Data data, ITrade trade) {
        if (trade.getTime() == null) {
            return;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(trade.getTime());
        c.set(Calendar.MILLISECOND, 0);

        if (data.dateOpen != null && data.dateClose != null) {
            Date time = c.getTime();
            if (!time.before(data.dateOpen) && time.before(data.dateClose)) {
                if (data.high == null || trade.getPrice() > data.high) {
                    data.high = trade.getPrice();
                }
                if (data.low == null || trade.getPrice() < data.low) {
                    data.low = trade.getPrice();
                }
                data.close = trade.getPrice();
                if (trade.getSize() != null) {
                    data.volume = data.volume != null ? data.volume + trade.getSize() : trade.getSize();
                }
            }

            if (!time.before(data.dateClose)) {
                fireBarCloseEvent(data);
            }
        }

        if (data.dateOpen == null) {
            data.dateOpen = c.getTime();

            data.open = trade.getPrice();
            data.high = trade.getPrice();
            data.low = trade.getPrice();
            data.close = trade.getPrice();
            data.volume = trade.getSize();

            c.add(Calendar.MINUTE, data.timeSpan.getLength());
            data.dateClose = c.getTime();

            fireBarOpenEvent(data);

            timer.schedule(new BarCloseTimerTask(data), data.dateClose);
        }
    }

    private void fireBarOpenEvent(Data data) {
        BarFactoryEvent event = new BarFactoryEvent(data.security, data.timeSpan, data.dateOpen, data.open);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((IBarFactoryListener) l[i]).barOpen(event);
        }
    }

    private void fireBarCloseEvent(Data data) {
        BarFactoryEvent event = new BarFactoryEvent(data.security, data.timeSpan, data.dateOpen, data.open, data.high, data.low, data.close, data.volume);
        data.dateOpen = null;

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((IBarFactoryListener) l[i]).barClose(event);
        }
    }
}
