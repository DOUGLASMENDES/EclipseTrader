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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.opentick.OTBookPriceLevel;
import com.opentick.OTBookPurge;
import com.opentick.OTBookReplace;
import com.opentick.OTClient;
import com.opentick.OTConstants;
import com.opentick.OTDividend;
import com.opentick.OTEquityInit;
import com.opentick.OTError;
import com.opentick.OTMMQuote;
import com.opentick.OTMessage;
import com.opentick.OTOHLC;
import com.opentick.OTOptionInit;
import com.opentick.OTQuote;
import com.opentick.OTSplit;
import com.opentick.OTTodaysOHL;
import com.opentick.OTTrade;

public class Client extends OTClient
{
    private static Client instance;
    Map streamListeners = new HashMap();
    List listeners = new ArrayList();
    Thread thread;
    private Log log = LogFactory.getLog(getClass());
    static Map statusText = new HashMap();

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
                    instance.addHost("feed1.opentick.com", 10015); //delayed data
                    break;
                case 1:
                    instance.addHost("feed1.opentick.com", 10010); //real-time data
                    break;
                case 2:
                    instance.addHost("feed2.opentick.com", 10010); //real-time data
                    break;
            }
        }
        return instance;
    }
    
    public void login(long timeout)
    {
        if (!isLoggedIn() && thread == null)
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

            try {
                login(userName, password);
                long started = System.currentTimeMillis();
                while((System.currentTimeMillis() - started) < timeout && !isLoggedIn())
                    Thread.sleep(100);
            } catch(Exception e) {
                log.error(e, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onLogin()
     */
    public void onLogin()
    {
        IClientListener[] list = getListeners();
        for (int i = 0; i < list.length; i++)
            list[i].onLogin();
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRestoreConnection()
     */
    public void onRestoreConnection()
    {
        IClientListener[] list = getListeners();
        for (int i = 0; i < list.length; i++)
            list[i].onRestoreConnection();
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onStatusChanged(int)
     */
    public void onStatusChanged(int status)
    {
        log.info("Status " + (String)statusText.get(new Integer(status)));
        IClientListener[] list = getListeners();
        for (int i = 0; i < list.length; i++)
            list[i].onStatusChanged(status);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onListExchanges(java.util.List)
     */
    public void onListExchanges(List exchanges)
    {
        IClientListener[] list = getListeners();
        for (int i = 0; i < list.length; i++)
            list[i].onListExchanges(exchanges);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onListSymbols(java.util.List)
     */
    public void onListSymbols(List symbols)
    {
        IClientListener[] list = getListeners();
        for (int i = 0; i < list.length; i++)
            list[i].onListSymbols(symbols);
    }
    
    public synchronized void addListener(int id, IClientListener listener)
    {
        List list = (List)streamListeners.get(String.valueOf(id));
        if (list == null)
        {
            list = new ArrayList();
            streamListeners.put(String.valueOf(id), list);
        }
        list.add(listener);
    }
    
    public synchronized void addListener(IClientListener listener)
    {
        listeners.add(listener);
    }
    
    public synchronized void removeListener(int id, IClientListener listener)
    {
        List list = (List)streamListeners.get(String.valueOf(id));
        if (list != null)
            list.remove(listener);
    }
    
    public synchronized void removeListener(IClientListener listener)
    {
        listeners.remove(listener);
    }
    
    synchronized IClientListener[] getListeners(int id)
    {
        List list = new ArrayList(listeners);

        List stream = (List)streamListeners.get(String.valueOf(id));
        if (stream != null)
            list.addAll(stream);
        
        return (IClientListener[])list.toArray(new IClientListener[list.size()]); 
    }
    
    synchronized IClientListener[] getListeners()
    {
        return (IClientListener[])listeners.toArray(new IClientListener[listeners.size()]); 
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onDividend(com.opentick.OTDividend)
     */
    public void onDividend(OTDividend dividend)
    {
        IClientListener[] list = getListeners(dividend.getRequestId());
        for (int i = 0; i < list.length; i++)
            list[i].onDividend(dividend);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onEquityInit(com.opentick.OTEquityInit)
     */
    public void onEquityInit(OTEquityInit equity)
    {
        IClientListener[] list = getListeners(equity.getRequestId());
        for (int i = 0; i < list.length; i++)
            list[i].onEquityInit(equity);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onError(com.opentick.OTError)
     */
    public void onError(OTError error)
    {
        IClientListener[] list = getListeners(error.getRequestId());
        for (int i = 0; i < list.length; i++)
            list[i].onError(error);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBBO(com.opentick.OTBBO)
     */
    public void onHistBBO(OTBBO bbo)
    {
        IClientListener[] list = getListeners(bbo.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBBO(bbo);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookCancel(com.opentick.OTBookCancel)
     */
    public void onHistBookCancel(OTBookCancel bookCancel)
    {
        IClientListener[] list = getListeners(bookCancel.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookCancel(bookCancel);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookChange(com.opentick.OTBookChange)
     */
    public void onHistBookChange(OTBookChange bookChange)
    {
        IClientListener[] list = getListeners(bookChange.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookChange(bookChange);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookDelete(com.opentick.OTBookDelete)
     */
    public void onHistBookDelete(OTBookDelete bookDelete)
    {
        IClientListener[] list = getListeners(bookDelete.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookDelete(bookDelete);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookExecute(com.opentick.OTBookExecute)
     */
    public void onHistBookExecute(OTBookExecute bookExecute)
    {
        IClientListener[] list = getListeners(bookExecute.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookExecute(bookExecute);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookOrder(com.opentick.OTBookOrder)
     */
    public void onHistBookOrder(OTBookOrder bookOrder)
    {
        IClientListener[] list = getListeners(bookOrder.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookOrder(bookOrder);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookPriceLevel(com.opentick.OTBookPriceLevel)
     */
    public void onHistBookPriceLevel(OTBookPriceLevel bookPriceLevel)
    {
        IClientListener[] list = getListeners(bookPriceLevel.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookPriceLevel(bookPriceLevel);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookPurge(com.opentick.OTBookPurge)
     */
    public void onHistBookPurge(OTBookPurge bookPurge)
    {
        IClientListener[] list = getListeners(bookPurge.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookPurge(bookPurge);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistBookReplace(com.opentick.OTBookReplace)
     */
    public void onHistBookReplace(OTBookReplace bookReplace)
    {
        IClientListener[] list = getListeners(bookReplace.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistBookReplace(bookReplace);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistMMQuote(com.opentick.OTMMQuote)
     */
    public void onHistMMQuote(OTMMQuote quote)
    {
        IClientListener[] list = getListeners(quote.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistMMQuote(quote);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistOHLC(com.opentick.OTOHLC)
     */
    public void onHistOHLC(OTOHLC ohlc)
    {
        IClientListener[] list = getListeners(ohlc.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistOHLC(ohlc);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistQuote(com.opentick.OTQuote)
     */
    public void onHistQuote(OTQuote quote)
    {
        IClientListener[] list = getListeners(quote.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistQuote(quote);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onHistTrade(com.opentick.OTTrade)
     */
    public void onHistTrade(OTTrade trade)
    {
        IClientListener[] list = getListeners(trade.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onHistTrade(trade);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onMessage(com.opentick.OTMessage)
     */
    public void onMessage(OTMessage message)
    {
        IClientListener[] list = getListeners(message.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onMessage(message);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onOptionInit(com.opentick.OTOptionInit)
     */
    public void onOptionInit(OTOptionInit optionInit)
    {
        IClientListener[] list = getListeners(optionInit.getRequestId());
        for (int i = 0; i < list.length; i++)
            list[i].onOptionInit(optionInit);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBBO(com.opentick.OTBBO)
     */
    public void onRealtimeBBO(OTBBO bbo)
    {
        IClientListener[] list = getListeners(bbo.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBBO(bbo);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookCancel(com.opentick.OTBookCancel)
     */
    public void onRealtimeBookCancel(OTBookCancel cancel)
    {
        IClientListener[] list = getListeners(cancel.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookCancel(cancel);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookChange(com.opentick.OTBookChange)
     */
    public void onRealtimeBookChange(OTBookChange change)
    {
        IClientListener[] list = getListeners(change.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookChange(change);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookDelete(com.opentick.OTBookDelete)
     */
    public void onRealtimeBookDelete(OTBookDelete delete)
    {
        IClientListener[] list = getListeners(delete.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookDelete(delete);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookExecute(com.opentick.OTBookExecute)
     */
    public void onRealtimeBookExecute(OTBookExecute execute)
    {
        IClientListener[] list = getListeners(execute.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookExecute(execute);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookOrder(com.opentick.OTBookOrder)
     */
    public void onRealtimeBookOrder(OTBookOrder order)
    {
        IClientListener[] list = getListeners(order.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookOrder(order);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookPriceLevel(com.opentick.OTBookPriceLevel)
     */
    public void onRealtimeBookPriceLevel(OTBookPriceLevel level)
    {
        IClientListener[] list = getListeners(level.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookPriceLevel(level);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookPurge(com.opentick.OTBookPurge)
     */
    public void onRealtimeBookPurge(OTBookPurge purge)
    {
        IClientListener[] list = getListeners(purge.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookPurge(purge);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeBookReplace(com.opentick.OTBookReplace)
     */
    public void onRealtimeBookReplace(OTBookReplace replace)
    {
        IClientListener[] list = getListeners(replace.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeBookReplace(replace);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeMMQuote(com.opentick.OTMMQuote)
     */
    public void onRealtimeMMQuote(OTMMQuote quote)
    {
        IClientListener[] list = getListeners(quote.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeMMQuote(quote);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeQuote(com.opentick.OTQuote)
     */
    public void onRealtimeQuote(OTQuote quote)
    {
        IClientListener[] list = getListeners(quote.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeQuote(quote);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onRealtimeTrade(com.opentick.OTTrade)
     */
    public void onRealtimeTrade(OTTrade trade)
    {
        IClientListener[] list = getListeners(trade.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onRealtimeTrade(trade);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onSplit(com.opentick.OTSplit)
     */
    public void onSplit(OTSplit split)
    {
        IClientListener[] list = getListeners(split.getRequestId());
        for (int i = 0; i < list.length; i++)
            list[i].onSplit(split);
    }

    /* (non-Javadoc)
     * @see com.opentick.OTClient#onTodaysOHL(com.opentick.OTTodaysOHL)
     */
    public void onTodaysOHL(OTTodaysOHL todayOHL)
    {
        IClientListener[] list = getListeners(todayOHL.getRequestID());
        for (int i = 0; i < list.length; i++)
            list[i].onTodaysOHL(todayOHL);
    }
    
    static {
        statusText.put(new Integer(OTConstants.OT_STATUS_INACTIVE), "Inactive");
        statusText.put(new Integer(OTConstants.OT_STATUS_CONNECTING), "Connecting");
        statusText.put(new Integer(OTConstants.OT_STATUS_CONNECTED), "Connected");
        statusText.put(new Integer(OTConstants.OT_STATUS_LOGGED_IN), "Logged In");
    }
}
