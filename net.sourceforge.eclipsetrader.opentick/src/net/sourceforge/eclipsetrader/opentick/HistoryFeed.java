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

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Dividend;
import net.sourceforge.eclipsetrader.core.db.History;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Split;
import net.sourceforge.eclipsetrader.opentick.internal.Client;
import net.sourceforge.eclipsetrader.opentick.internal.ClientAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentick.OTConstants;
import com.opentick.OTDataEntity;
import com.opentick.OTDividend;
import com.opentick.OTError;
import com.opentick.OTMessage;
import com.opentick.OTOHLC;
import com.opentick.OTSplit;

public class HistoryFeed implements IHistoryFeed
{
    Client client = Client.getInstance();
    private Log log = LogFactory.getLog(getClass());

    public HistoryFeed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IHistoryFeed#updateHistory(net.sourceforge.eclipsetrader.core.db.Security, int)
     */
    public void updateHistory(Security security, int interval)
    {
        try {
            client.login(15 * 1000);
        } catch(Exception e) {
            log.error(e, e);
        }

        if (interval == IHistoryFeed.INTERVAL_DAILY)
        {
            log.info("Updating historical data for " + security);
            try {
                requestHistoryStream(security, 60 * 1000);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
        if (interval == IHistoryFeed.INTERVAL_MINUTE)
        {
            String symbol = security.getHistoryFeed().getSymbol();
            if (symbol == null || symbol.length() == 0)
                symbol = security.getCode();
            String exchange = security.getHistoryFeed().getExchange();
            if (exchange == null || exchange.length() == 0)
                exchange = "Q";

            History history = security.getIntradayHistory();
            history.clear();
            
            BackfillClientAdapter adapter = new BackfillClientAdapter(security);
            client.addListener(adapter);

            Calendar from = Calendar.getInstance();
            from.set(Calendar.HOUR, 0);
            from.set(Calendar.MINUTE, 0);
            from.set(Calendar.SECOND, 0);
            from.set(Calendar.MILLISECOND, 0);
            from.add(Calendar.DATE, -5);
            int startTime = (int)(from.getTimeInMillis() / 1000);
            
            Calendar to = Calendar.getInstance();
            to.set(Calendar.MILLISECOND, 0);
            int endTime = (int)(to.getTimeInMillis() / 1000);
            
            log.info("Updating intraday data for " + security);
            adapter.started = System.currentTimeMillis();
            try {
                adapter.historyStream = client.requestHistData(new OTDataEntity(exchange, symbol), startTime, endTime, OTConstants.OT_HIST_OHLC_MINUTELY, 1);
                while((System.currentTimeMillis() - adapter.started) < 60 * 1000 && !adapter.isCompleted())
                    Thread.sleep(100);
            } catch (Exception e) {
                log.error(e, e);
            }
            
            client.removeListener(adapter);
            
            Collections.sort(security.getDividends(), new Comparator() {
                public int compare(Object o1, Object o2)
                {
                    return ((Dividend)o1).getDate().compareTo(((Dividend)o2).getDate());
                }
            });
            Collections.sort(security.getSplits(), new Comparator() {
                public int compare(Object o1, Object o2)
                {
                    return ((Split)o1).getDate().compareTo(((Split)o2).getDate());
                }
            });
            
            CorePlugin.getRepository().save(history);
            CorePlugin.getRepository().save(security);
        }
    }
    
    public void requestHistoryStream(final Security security, long timeout)
    {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();

        to.set(Calendar.HOUR, 23);
        to.set(Calendar.MINUTE, 59);
        to.set(Calendar.SECOND, 59);
        to.set(Calendar.MILLISECOND, 0);
        
        HistoryClientAdapter adapter = new HistoryClientAdapter(security);
        client.addListener(adapter);

        String symbol = security.getHistoryFeed().getSymbol();
        if (symbol == null || symbol.length() == 0)
            symbol = security.getCode();
        String exchange = security.getHistoryFeed().getExchange();
        if (exchange == null || exchange.length() == 0)
            exchange = "Q";
        
        History history = security.getHistory();
        if (history.size() == 0)
        {
            from = Calendar.getInstance();
            from.add(Calendar.YEAR, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
        }
        else
        {
            Bar last = history.getLast();
            from.setTime(last.getDate());
            from.add(Calendar.DATE, 1);
        }

        from.set(Calendar.HOUR, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);
        
        try {
            adapter.historyStream = client.requestHistData(new OTDataEntity(exchange, symbol), (int)(from.getTimeInMillis() / 1000), (int)(to.getTimeInMillis() / 1000), OTConstants.OT_HIST_OHLC_DAILY, 1);
            log.debug(String.valueOf(adapter.historyStream) + " / Request History " + security + " [" + CorePlugin.getDateFormat().format(from.getTime()) + "-" + CorePlugin.getDateFormat().format(to.getTime()) + "]");
        } catch(Exception e) {
            log.warn(e);
        }
        
        List splits = security.getSplits();
        if (splits.size() == 0)
        {
            from = Calendar.getInstance();
            from.add(Calendar.YEAR, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
        }
        else
        {
            Split last = (Split)splits.get(splits.size() - 1);
            from.setTime(last.getDate());
            from.add(Calendar.DATE, 1);
        }

        from.set(Calendar.HOUR, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);
        
        try {
            adapter.splitsStream = client.requestSplits(new OTDataEntity(exchange, symbol), (int)(from.getTimeInMillis() / 1000), (int)(to.getTimeInMillis() / 1000));
            log.debug(String.valueOf(adapter.splitsStream) + " / Request Splits " + security + " [" + CorePlugin.getDateFormat().format(from.getTime()) + "-" + CorePlugin.getDateFormat().format(to.getTime()) + "]");
        } catch(Exception e) {
            log.warn(e);
        }
        
        List dividends = security.getDividends();
        if (dividends.size() == 0)
        {
            from = Calendar.getInstance();
            from.add(Calendar.YEAR, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
        }
        else
        {
            Dividend last = (Dividend)dividends.get(dividends.size() - 1);
            from.setTime(last.getDate());
            from.add(Calendar.DATE, 1);
        }

        from.set(Calendar.HOUR, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);
        
        try {
            adapter.dividendsStream = client.requestDividends(new OTDataEntity(exchange, symbol), (int)(from.getTimeInMillis() / 1000), (int)(to.getTimeInMillis() / 1000));
            log.debug(String.valueOf(adapter.dividendsStream) + " / Request Dividends " + security + " [" + CorePlugin.getDateFormat().format(from.getTime()) + "-" + CorePlugin.getDateFormat().format(to.getTime()) + "]");
        } catch(Exception e) {
            log.warn(e);
        }

        adapter.started = System.currentTimeMillis();
        try {

            while((System.currentTimeMillis() - adapter.started) < timeout && !adapter.isCompleted())
                Thread.sleep(100);
        } catch(Exception e) {
            log.warn(e);
        }
        
        client.removeListener(adapter);
        
        Collections.sort(security.getDividends(), new Comparator() {
            public int compare(Object o1, Object o2)
            {
                return ((Dividend)o1).getDate().compareTo(((Dividend)o2).getDate());
            }
        });
        Collections.sort(security.getSplits(), new Comparator() {
            public int compare(Object o1, Object o2)
            {
                return ((Split)o1).getDate().compareTo(((Split)o2).getDate());
            }
        });
        
        CorePlugin.getRepository().save(history);
        CorePlugin.getRepository().save(security);
    }

    /**
     * Client listener implementation specialized for daily data.
     */
    private class HistoryClientAdapter extends ClientAdapter
    {
        Security security;
        History history;
        List dividends;
        List splits;
        int historyStream;
        int dividendsStream;
        int splitsStream;
        long started;
        Set completedStreams = new HashSet();
        
        public HistoryClientAdapter(Security security)
        {
            this.security = security;
            history = security.getHistory();
            dividends = security.getDividends();
            splits = security.getSplits();
        }
        
        public boolean isCompleted()
        {
            if (historyStream != 0 && !completedStreams.contains(String.valueOf(historyStream)))
                return false;
            if (dividendsStream != 0 && !completedStreams.contains(String.valueOf(dividendsStream)))
                return false;
            if (splitsStream != 0 && !completedStreams.contains(String.valueOf(splitsStream)))
                return false;
            return true;
        }

        public void onHistOHLC(OTOHLC msg)
        {
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis((long)msg.getTimestamp() * 1000);
            date.set(Calendar.HOUR, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            Bar current = new Bar();
            current.setDate(date.getTime());
            current.setOpen(msg.getOpenPrice());
            current.setHigh(msg.getHighPrice());
            current.setLow(msg.getLowPrice());
            current.setClose(msg.getClosePrice());
            current.setVolume(msg.getVolume());

            Bar last = history.getLast();
            if (last == null || !last.getDate().equals(current.getDate()))
                history.add(current);

            started = System.currentTimeMillis();
        }

        public void onDividend(OTDividend msg)
        {
            if (msg.getPrice() != 0)
            {
                Calendar date = Calendar.getInstance();
                Dividend current = new Dividend();

                date.setTimeInMillis((long)msg.getPaymentDate() * 1000);
                date.set(Calendar.HOUR, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                date.add(Calendar.DATE, 1);
                while(date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                    date.add(Calendar.DATE, 1);
                current.setDate(date.getTime());
                current.setValue(msg.getPrice());
                
                if (dividends.size() != 0)
                {
                    Dividend last = (Dividend)dividends.get(dividends.size() - 1);
                    if (last.getDate().equals(current.getDate()))
                        dividends.set(dividends.size() - 1, current);
                    else
                        dividends.add(current);
                }
                else
                    dividends.add(current);
            }

            started = System.currentTimeMillis();
        }

        public void onSplit(OTSplit msg)
        {
            Calendar date = Calendar.getInstance();
            Split current = new Split();

            date.setTimeInMillis((long)msg.getPaymentDate() * 1000);
            date.set(Calendar.HOUR, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            date.add(Calendar.DATE, 1);
            while(date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                date.add(Calendar.DATE, 1);
            current.setDate(date.getTime());
            current.setFromQuantity(msg.getToFactor());
            current.setToQuantity(msg.getForFactor());
            
            if (splits.size() != 0)
            {
                Split last = (Split)splits.get(splits.size() - 1);
                if (last.getDate().equals(current.getDate()))
                    splits.set(splits.size() - 1, current);
                else
                    splits.add(current);
            }
            else
                splits.add(current);

            started = System.currentTimeMillis();
        }
        
        public void onError(OTError msg)
        {
            completedStreams.add(String.valueOf(msg.getRequestId()));
            log.debug(String.valueOf(msg.getRequestId()) + " / Error: " + msg.getDescription());
        }
        
        public void onMessage(OTMessage msg)
        {
            completedStreams.add(String.valueOf(msg.getRequestID()));
            log.debug(String.valueOf(msg.getRequestID()) + " / " + msg.getDescription());
        }
    }
    
    /**
     * Client listener implementation specialized for backfilling intraday data.
     */
    private class BackfillClientAdapter extends ClientAdapter
    {
        Security security;
        History history;
        int historyStream;
        long started;
        Set completedStreams = new HashSet();
        
        public BackfillClientAdapter(Security security)
        {
            this.security = security;
            history = security.getIntradayHistory();
        }
        
        public boolean isCompleted()
        {
            if (historyStream != 0 && !completedStreams.contains(String.valueOf(historyStream)))
                return false;
            return true;
        }

        public void onHistOHLC(OTOHLC msg)
        {
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis((long)msg.getTimestamp() * 1000);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            Bar current = new Bar();
            current.setDate(date.getTime());
            current.setOpen(msg.getOpenPrice());
            current.setHigh(msg.getHighPrice());
            current.setLow(msg.getLowPrice());
            current.setClose(msg.getClosePrice());
            current.setVolume(msg.getVolume());

            Bar last = history.getLast();
            if (last == null || !last.getDate().equals(current.getDate()))
                history.add(current);

            started = System.currentTimeMillis();
        }
        
        public void onError(OTError msg)
        {
            completedStreams.add(String.valueOf(msg.getRequestId()));
            log.debug(String.valueOf(msg.getRequestId()) + " / Error: " + msg.getDescription());
        }
        
        public void onMessage(OTMessage msg)
        {
            completedStreams.add(String.valueOf(msg.getRequestID()));
            log.debug(String.valueOf(msg.getRequestID()) + " / " + msg.getDescription());
        }
    }
}
