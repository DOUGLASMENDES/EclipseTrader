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

package net.sourceforge.eclipsetrader.opentick.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.opentick.OpenTickPlugin;
import net.sourceforge.eclipsetrader.opentick.ui.dialogs.LoginDialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentick.OTBBO;
import com.opentick.OTBookCancel;
import com.opentick.OTBookChange;
import com.opentick.OTBookDelete;
import com.opentick.OTBookExecute;
import com.opentick.OTBookOrder;
import com.opentick.OTBookPurge;
import com.opentick.OTBookReplace;
import com.opentick.OTClient;
import com.opentick.OTConstants;
import com.opentick.OTDataEntity;
import com.opentick.OTEquityInit;
import com.opentick.OTError;
import com.opentick.OTException;
import com.opentick.OTMMQuote;
import com.opentick.OTMessage;
import com.opentick.OTOHLC;
import com.opentick.OTQuote;
import com.opentick.OTTodaysOHL;
import com.opentick.OTTrade;

public class Client
{
    private static Client instance;
    static Map statusText = new HashMap();
    int instanceCount = 0;
    Map tickStreams = new HashMap();
    Set pendingTickStreams = new HashSet();
    Map bookStreams = new HashMap();
    Set pendingBookStreams = new HashSet();
    Map securityBook = new HashMap();
    Thread thread;
    private Log log = LogFactory.getLog(getClass());
    private OTClient client = new OTClient() {

        public void onEquityInit(OTEquityInit msg)
        {
            log.trace(msg);

            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestId()));
            if (security != null)
            {
                if (msg.getPrevClosePrice() != 0)
                    security.setClose(new Double(msg.getPrevClosePrice()));
            }
            else
                log.warn("Unknown security for request id " + msg.getRequestId());
            tickStreams.remove(String.valueOf(msg.getRequestId()));
        }

        public void onError(OTError msg)
        {
            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestId()));
            if (security != null)
            {
                log.error(msg.getRequestId() + " / " + msg.getDescription() + " (ticks) - " + security);
                tickStreams.remove(String.valueOf(msg.getRequestId()));
            }
            else
            {
                security = (Security)bookStreams.get(String.valueOf(msg.getRequestId()));
                if (security != null)
                {
                    log.error(msg.getRequestId() + " / " + msg.getDescription() + " (book) - " + security);
                    bookStreams.remove(String.valueOf(msg.getRequestId()));
                    securityBook.remove(String.valueOf(msg.getRequestId()));
                }
                else
                    log.error(String.valueOf(msg.getRequestId()) + " / " + msg.getDescription());
            }
        }

        public void onHistBBO(OTBBO msg)
        {
            log.trace(msg);
        }

        public void onHistMMQuote(OTMMQuote msg)
        {
            log.trace(msg);
        }

        public void onHistOHLC(OTOHLC msg)
        {
            log.trace(msg);

            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestID()));
            if (security != null)
            {
                Bar bar = new Bar();
                bar.setDate(new Date(msg.getTimestamp() * 1000));
                bar.setOpen(msg.getOpenPrice());
                bar.setHigh(msg.getHighPrice());
                bar.setLow(msg.getLowPrice());
                bar.setClose(msg.getClosePrice());
                bar.setVolume(msg.getVolume());
                security.getHistory().add(bar);
            }
            else
                log.warn("Unknown security for request id " + msg.getRequestID());
        }

        public void onHistQuote(OTQuote msg)
        {
            log.trace(msg);
        }

        public void onHistTrade(OTTrade msg)
        {
            log.trace(msg);
        }

        public void onListExchanges(List list)
        {
            log.trace(list);
        }

        public void onListSymbols(List list)
        {
            log.trace(list);
        }

        public void onLogin()
        {
            log.info("Logged in to the server");

            try {
                for (Iterator iter = pendingTickStreams.iterator(); iter.hasNext(); )
                    Client.this.requestTickStream((Security)iter.next());
                pendingTickStreams.clear();
                
                for (Iterator iter = pendingBookStreams.iterator(); iter.hasNext(); )
                    Client.this.requestBookStream((Security)iter.next());
                pendingBookStreams.clear();
            } catch(Exception e) {
                log.error(e, e);
            }
        }

        public void onMessage(OTMessage msg)
        {
            log.info(msg);
        }

        public void onRealtimeBBO(OTBBO msg)
        {
            log.trace(msg);

            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestID()));
            if (security != null)
            {
                Quote quote = new Quote(security.getQuote());
                quote.setDate(new Date(msg.getTimestamp() * 1000L));
                if (msg.getSide() == 'B')
                {
                    quote.setBid(msg.getPrice());
                    quote.setBidSize(msg.getSize());
                }
                else if (msg.getSide() == 'A' || msg.getSide() == 'S')
                {
                    quote.setAsk(msg.getPrice());
                    quote.setAskSize(msg.getSize());
                }
                security.setQuote(quote);
            }
            else
                log.warn("Unknown security for request id " + msg.getRequestID());
        }

        public void onRealtimeBookCancel(OTBookCancel msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book != null)
            {
                book.remove(msg.getOrderRef(), msg.getSize());
                Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
            else
                log.warn("Unknown security for request id " + msg.getRequestID());
        }

        public void onRealtimeBookChange(OTBookChange msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book != null)
            {
                book.remove(msg.getOrderRef(), msg.getSize());
                Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookDelete(OTBookDelete msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book != null)
            {
                book.delete(msg.getOrderRef(), msg.getSide(), msg.getDeleteType());
                Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookExecute(OTBookExecute msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book != null)
            {
                book.remove(msg.getOrderRef(), msg.getSize());
                Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookOrder(OTBookOrder msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book != null)
            {
                book.add(msg.getTimestamp(), msg.getOrderRef(), msg.getPrice(), msg.getSize(), msg.getSide());
                Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookPurge(OTBookPurge msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book != null)
            {
                book.clear();
                Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookReplace(OTBookReplace msg)
        {
            log.trace(msg);
            
            Book book = (Book)securityBook.get(String.valueOf(msg.getRequestID()));
            if (book == null)
            {
                book = new Book();
                securityBook.put(String.valueOf(msg.getRequestID()), book);
            }
            book.replace(msg.getTimestamp(), msg.getOrderRef(), msg.getPrice(), msg.getSize(), msg.getSide());
            Security security = (Security)bookStreams.get(String.valueOf(msg.getRequestID()));
            security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
        }

        public void onRealtimeMMQuote(OTMMQuote msg)
        {
            log.trace(msg);
        }

        public void onRealtimeQuote(OTQuote msg)
        {
            log.trace(msg);

            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestID()));
            if (security != null)
            {
                Quote values = new Quote(security.getQuote());
                values.setDate(new Date(msg.getTimestamp() * 1000L));
                values.setBid(msg.getBidPrice());
                values.setBidSize(msg.getBidSize());
                values.setAsk(msg.getAskPrice());
                values.setAskSize(msg.getAskSize());
                security.setQuote(values);
            }
            else
            {
                log.warn("Unknown security for request id " + msg.getRequestID());
                try {
                    cancelTickStream(msg.getRequestID());
                } catch(Exception e) {
                    log.error(e, e);
                }
            }
        }

        public void onRealtimeTrade(OTTrade msg)
        {
            log.trace(msg);

            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestID()));
            if (security != null)
            {
                Quote quote = new Quote(security.getQuote());
                quote.setDate(new Date(msg.getTimestamp() * 1000L));
                quote.setLast(msg.getPrice());
                quote.setVolume(msg.getVolume());
                security.setQuote(quote);
                
                if (msg.isOpen())
                    security.setOpen(new Double(msg.getPrice()));
                if (msg.isHigh())
                    security.setHigh(new Double(msg.getPrice()));
                if (msg.isLow())
                    security.setLow(new Double(msg.getPrice()));
                if (msg.isClose())
                    security.setClose(new Double(msg.getPrice()));
            }
            else
            {
                log.warn("Unknown security for request id " + msg.getRequestID());
                try {
                    cancelTickStream(msg.getRequestID());
                } catch(Exception e) {
                    log.error(e, e);
                }
            }
        }

        public void onTodaysOHL(OTTodaysOHL msg)
        {
            log.trace(msg);

            Security security = (Security)tickStreams.get(String.valueOf(msg.getRequestID()));
            if (security != null)
            {
                if (msg.getOpenPrice() != 0)
                    security.setOpen(new Double(msg.getOpenPrice()));
                if (msg.getHighPrice() != 0)
                    security.setHigh(new Double(msg.getHighPrice()));
                if (msg.getLowPrice() != 0)
                    security.setLow(new Double(msg.getLowPrice()));
            }
            else
                log.warn("Unknown security for request id " + msg.getRequestID());
            
            tickStreams.remove(String.valueOf(msg.getRequestID()));
        }

        public void onRestoreConnection()
        {
            log.info("Connection restored");
            
            Object[] s = tickStreams.values().toArray();
            tickStreams.clear();

            try {
                for (int i = 0; i < s.length; i++)
                    Client.this.requestTickStream((Security)s[i]);
            } catch(Exception e) {
                log.error(e, e);
            }
            
            s = bookStreams.values().toArray();
            bookStreams.clear();

            try {
                for (int i = 0; i < s.length; i++)
                    Client.this.requestBookStream((Security)s[i]);
            } catch(Exception e) {
                log.error(e, e);
            }
        }

        public void onStatusChanged(int status)
        {
            log.info("Status " + (String)statusText.get(new Integer(status)));
        }
    };

    Client()
    {
    }
    
    public static synchronized Client getInstance()
    {
        if (instance == null)
        {
            instance = new Client();
            switch(OpenTickPlugin.getDefault().getPreferenceStore().getInt(OpenTickPlugin.PREFS_SERVER))
            {
                case 0:
                    instance.client.addHost("feed1.opentick.com", 10015); //delayed data
                    break;
                case 1:
                    instance.client.addHost("feed1.opentick.com", 10010); //real-time data
                    break;
                case 2:
                    instance.client.addHost("feed2.opentick.com", 10010); //real-time data
                    break;
            }
        }
        instance.instanceCount++;
        return instance;
    }
    
    public void dispose()
    {
        if (--instanceCount <= 0)
        {
            instance = null;
            try {
                if (client.isLoggedIn())
                {
                    client.logout();
                    
                    long timeout = System.currentTimeMillis() + 10 * 1000;
                    while(client.getStatus() != OTConstants.OT_STATUS_INACTIVE && System.currentTimeMillis() < timeout)
                    {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // Ignore exception, not important at this time
                        }
                    }
                }
            } catch(Exception e) {
                log.error(e, e);
            }
        }
    }
    
    public synchronized void login()
    {
        if (!client.isLoggedIn() && thread == null)
        {
            String userName = OpenTickPlugin.getDefault().getPreferenceStore().getString(OpenTickPlugin.PREFS_USERNAME);
            String password = OpenTickPlugin.getDefault().getPreferenceStore().getString(OpenTickPlugin.PREFS_PASSWORD);

            if (userName.length() == 0 || password.length() == 0)
            {
                LoginDialog dlg = new LoginDialog(userName, password);
                if (dlg.open() != LoginDialog.OK)
                    return;
                
                userName = dlg.getUserName();
                password = dlg.getPassword();
            }

            thread = new Thread("OpenTick Login") {
                public void run()
                {
                    try {
                        String userName = OpenTickPlugin.getDefault().getPreferenceStore().getString(OpenTickPlugin.PREFS_USERNAME);
                        String password = OpenTickPlugin.getDefault().getPreferenceStore().getString(OpenTickPlugin.PREFS_PASSWORD);
                        client.login(userName, password);
                    } catch(Exception e) {
                        log.error(e, e);
                    }
                    thread = null;
                }
            };
            thread.start();
        }
    }
    
    public void requestTickStream(Security security) throws OTException
    {
        if (client.getStatus() != OTConstants.OT_STATUS_LOGGED_IN)
            pendingTickStreams.add(security);
        else
        {
            String symbol = security.getQuoteFeed().getSymbol();
            if (symbol == null || symbol.length() == 0)
                symbol = security.getCode();
            String exchange = security.getQuoteFeed().getExchange();
            if (exchange == null || exchange.length() == 0)
                exchange = "Q";

            int id = client.requestEquityInit(new OTDataEntity(exchange, symbol));
            tickStreams.put(String.valueOf(id), security);
            log.debug(String.valueOf(id) + " / Request Equity Init " + security);

            id = client.requestTodaysOHL(new OTDataEntity(exchange, symbol));
            tickStreams.put(String.valueOf(id), security);
            log.debug(String.valueOf(id) + " / Request Today's OHL " + security);
            
            id = client.requestTickStream(new OTDataEntity(exchange, symbol), OTConstants.OT_TICK_TYPE_LEVEL1);
            tickStreams.put(String.valueOf(id), security);
            log.debug(String.valueOf(id) + " / Request Tick stream " + security);
        }
    }

    public void cancelTickStream(Security security) throws OTException
    {
        String[] keys = (String[])tickStreams.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++)
        {
            if (security.equals(tickStreams.get(keys[i])))
            {
                client.cancelTickStream(Integer.parseInt(keys[i]));
                tickStreams.remove(keys[i]);
                log.debug(String.valueOf(keys[i]) + " / Request cancel Tick stream " + security);
            }
        }
        pendingTickStreams.remove(security);
    }
    
    public void requestBookStream(Security security) throws OTException
    {
        if (client.getStatus() != OTConstants.OT_STATUS_LOGGED_IN)
            pendingBookStreams.add(security);
        else
        {
            String symbol = security.getLevel2Feed().getSymbol();
            if (symbol == null || symbol.length() == 0)
                symbol = security.getCode();
            String exchange = security.getLevel2Feed().getExchange();
            if (exchange == null || exchange.length() == 0)
                exchange = "Q";
            
            int id = client.requestBookStream(new OTDataEntity(exchange, symbol));
            bookStreams.put(String.valueOf(id), security);
            securityBook.put(String.valueOf(id), new Book());

            log.debug(String.valueOf(id) + " / Request Book stream " + security);
        }
    }

    public void cancelBookStream(Security security) throws OTException
    {
        for (Iterator iter = bookStreams.keySet().iterator(); iter.hasNext(); )
        {
            String id = (String)iter.next();
            if (security.equals(bookStreams.get(id)))
            {
                client.cancelBookStream(Integer.parseInt(id));
                bookStreams.remove(id);
                securityBook.remove(id);
                log.debug(String.valueOf(id) + " / Request cancel Book stream " + security);
                break;
            }
        }
        pendingBookStreams.remove(security);
    }
    
    public boolean isLoggedIn()
    {
        return client.isLoggedIn();
    }
    
    static {
        statusText.put(new Integer(OTConstants.OT_STATUS_INACTIVE), "Inactive");
        statusText.put(new Integer(OTConstants.OT_STATUS_CONNECTING), "Connecting");
        statusText.put(new Integer(OTConstants.OT_STATUS_CONNECTED), "Connected");
        statusText.put(new Integer(OTConstants.OT_STATUS_LOGGED_IN), "Logged In");
    }
}
