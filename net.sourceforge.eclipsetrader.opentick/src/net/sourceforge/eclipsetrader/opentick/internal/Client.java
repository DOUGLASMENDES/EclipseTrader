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

        public void onEquityInit(OTEquityInit equity)
        {
            log.info(equity);
        }

        public void onError(OTError error)
        {
            log.info(error);
        }

        public void onHistBBO(OTBBO bbo)
        {
            log.info(bbo);
        }

        public void onHistMMQuote(OTMMQuote quote)
        {
            log.info(quote);
        }

        public void onHistOHLC(OTOHLC ohlc)
        {
            log.info(ohlc);
            Security security = (Security)tickStreams.get(String.valueOf(ohlc.getRequestID()));
            if (security != null)
            {
                Bar bar = new Bar();
                bar.setDate(new Date(ohlc.getTimestamp() * 1000));
                bar.setOpen(ohlc.getOpenPrice());
                bar.setHigh(ohlc.getHighPrice());
                bar.setLow(ohlc.getLowPrice());
                bar.setClose(ohlc.getClosePrice());
                bar.setVolume(ohlc.getVolume());
                security.getHistory().add(bar);
            }
        }

        public void onHistQuote(OTQuote quote)
        {
            log.info(quote);
        }

        public void onHistTrade(OTTrade trade)
        {
            log.info(trade);
        }

        public void onListExchanges(List exchanges)
        {
            log.debug(exchanges);
        }

        public void onListSymbols(List symbols)
        {
            log.debug(symbols);
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

        public void onMessage(OTMessage message)
        {
            log.info(message);
        }

        public void onRealtimeBBO(OTBBO bbo)
        {
            log.info(bbo);

            Security security = (Security)tickStreams.get(String.valueOf(bbo.getRequestID()));
            if (security != null)
            {
                Quote quote = new Quote(security.getQuote());
                quote.setDate(new Date(bbo.getTimestamp() * 1000L));
                if (bbo.getSide() == 'B')
                {
                    quote.setBid(bbo.getPrice());
                    quote.setBidSize(bbo.getSize());
                }
                else if (bbo.getSide() == 'A' || bbo.getSide() == 'S')
                {
                    quote.setAsk(bbo.getPrice());
                    quote.setAskSize(bbo.getSize());
                }
                security.setQuote(quote);
            }
        }

        public void onRealtimeBookCancel(OTBookCancel cancel)
        {
            log.debug(cancel);
            
            Book book = (Book)securityBook.get(String.valueOf(cancel.getRequestID()));
            if (book != null)
            {
                book.remove(cancel.getOrderRef(), cancel.getSize());
                Security security = (Security)bookStreams.get(String.valueOf(cancel.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookChange(OTBookChange change)
        {
            log.debug(change);
            
            Book book = (Book)securityBook.get(String.valueOf(change.getRequestID()));
            if (book != null)
            {
                book.remove(change.getOrderRef(), change.getSize());
                Security security = (Security)bookStreams.get(String.valueOf(change.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookDelete(OTBookDelete delete)
        {
            log.debug(delete);
            
            Book book = (Book)securityBook.get(String.valueOf(delete.getRequestID()));
            if (book != null)
            {
                book.delete(delete.getOrderRef(), delete.getSide(), delete.getDeleteType());
                Security security = (Security)bookStreams.get(String.valueOf(delete.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookExecute(OTBookExecute execute)
        {
            log.debug(execute);
            
            Book book = (Book)securityBook.get(String.valueOf(execute.getRequestID()));
            if (book != null)
            {
                book.remove(execute.getOrderRef(), execute.getSize());
                Security security = (Security)bookStreams.get(String.valueOf(execute.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookOrder(OTBookOrder order)
        {
            log.debug(order);
            
            Book book = (Book)securityBook.get(String.valueOf(order.getRequestID()));
            if (book != null)
            {
                book.add(order.getTimestamp(), order.getOrderRef(), order.getPrice(), order.getSize(), order.getSide());
                Security security = (Security)bookStreams.get(String.valueOf(order.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookPurge(OTBookPurge purge)
        {
            log.debug(purge);
            
            Book book = (Book)securityBook.get(String.valueOf(purge.getRequestID()));
            if (book != null)
            {
                book.clear();
                Security security = (Security)bookStreams.get(String.valueOf(purge.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onRealtimeBookReplace(OTBookReplace replace)
        {
            log.debug(replace);
            
            Book book = (Book)securityBook.get(String.valueOf(replace.getRequestID()));
            if (book == null)
            {
                book = new Book();
                securityBook.put(String.valueOf(replace.getRequestID()), book);
            }
            book.replace(replace.getTimestamp(), replace.getOrderRef(), replace.getPrice(), replace.getSize(), replace.getSide());
            Security security = (Security)bookStreams.get(String.valueOf(replace.getRequestID()));
            security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
        }

        public void onRealtimeMMQuote(OTMMQuote quote)
        {
            log.info(quote);
        }

        public void onRealtimeQuote(OTQuote quote)
        {
            log.debug(quote);

            Security security = (Security)tickStreams.get(String.valueOf(quote.getRequestID()));
            if (security != null)
            {
                Quote values = new Quote(security.getQuote());
                values.setDate(new Date(quote.getTimestamp() * 1000L));
                values.setBid(quote.getBidPrice());
                values.setBidSize(quote.getBidSize());
                values.setAsk(quote.getAskPrice());
                values.setAskSize(quote.getAskSize());
                security.setQuote(values);
            }
        }

        public void onRealtimeTrade(OTTrade trade)
        {
            log.debug(trade);

            Security security = (Security)tickStreams.get(String.valueOf(trade.getRequestID()));
            if (security != null)
            {
                Quote quote = new Quote(security.getQuote());
                quote.setDate(new Date(trade.getTimestamp() * 1000L));
                quote.setLast(trade.getPrice());
                quote.setVolume(trade.getVolume());
                security.setQuote(quote);
                
                if (trade.isOpen())
                    security.setOpen(new Double(trade.getPrice()));
                if (trade.isHigh())
                    security.setHigh(new Double(trade.getPrice()));
                if (trade.isLow())
                    security.setLow(new Double(trade.getPrice()));
                if (trade.isClose())
                    security.setClose(new Double(trade.getPrice()));
            }
        }

        public void onRestoreConnection()
        {
            log.info("Connection restored");
        }

        public void onStatusChanged(int status)
        {
            log.info("Status " + (String)statusText.get(new Integer(status)));
        }

        public void onTodaysOHL(OTTodaysOHL todayOHL)
        {
            log.info(todayOHL);
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
                    while(client.getStatus() != OTConstants.OT_STATUS_INACTIVE)
                        Thread.yield();
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
            
            int id = client.requestTickStream(new OTDataEntity(exchange, symbol), OTConstants.OT_TICK_TYPE_LEVEL1);
            tickStreams.put(String.valueOf(id), security);

            log.debug(String.valueOf(id) + " / Request tick stream " + symbol + "." + exchange);
        }
    }

    public void cancelTickStream(Security security) throws OTException
    {
        for (Iterator iter = tickStreams.keySet().iterator(); iter.hasNext(); )
        {
            String id = (String)iter.next();
            if (security.equals(tickStreams.get(id)))
            {
                client.cancelTickStream(Integer.parseInt(id));
                tickStreams.remove(id);
                log.debug(String.valueOf(id) + " / Request cancel tick stream");
                break;
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

            log.debug(String.valueOf(id) + " / Request book stream " + symbol + "." + exchange);
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
                log.debug(String.valueOf(id) + " / Request cancel book stream");
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
