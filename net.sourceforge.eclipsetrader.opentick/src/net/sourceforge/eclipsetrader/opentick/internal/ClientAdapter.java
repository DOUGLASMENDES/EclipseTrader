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

import java.util.List;

import com.opentick.OTBBO;
import com.opentick.OTBookCancel;
import com.opentick.OTBookChange;
import com.opentick.OTBookDelete;
import com.opentick.OTBookExecute;
import com.opentick.OTBookOrder;
import com.opentick.OTBookPriceLevel;
import com.opentick.OTBookPurge;
import com.opentick.OTBookReplace;
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

public class ClientAdapter implements IClientListener
{

    public ClientAdapter()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onDividend(com.opentick.OTDividend)
     */
    public void onDividend(OTDividend dividend)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onEquityInit(com.opentick.OTEquityInit)
     */
    public void onEquityInit(OTEquityInit equity)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onError(com.opentick.OTError)
     */
    public void onError(OTError error)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBBO(com.opentick.OTBBO)
     */
    public void onHistBBO(OTBBO bbo)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookCancel(com.opentick.OTBookCancel)
     */
    public void onHistBookCancel(OTBookCancel bookCancel)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookChange(com.opentick.OTBookChange)
     */
    public void onHistBookChange(OTBookChange bookChange)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookDelete(com.opentick.OTBookDelete)
     */
    public void onHistBookDelete(OTBookDelete bookDelete)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookExecute(com.opentick.OTBookExecute)
     */
    public void onHistBookExecute(OTBookExecute bookExecute)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookOrder(com.opentick.OTBookOrder)
     */
    public void onHistBookOrder(OTBookOrder bookOrder)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookPriceLevel(com.opentick.OTBookPriceLevel)
     */
    public void onHistBookPriceLevel(OTBookPriceLevel bookPriceLevel)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookPurge(com.opentick.OTBookPurge)
     */
    public void onHistBookPurge(OTBookPurge bookPurge)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistBookReplace(com.opentick.OTBookReplace)
     */
    public void onHistBookReplace(OTBookReplace bookReplace)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistMMQuote(com.opentick.OTMMQuote)
     */
    public void onHistMMQuote(OTMMQuote quote)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistOHLC(com.opentick.OTOHLC)
     */
    public void onHistOHLC(OTOHLC ohlc)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistQuote(com.opentick.OTQuote)
     */
    public void onHistQuote(OTQuote quote)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onHistTrade(com.opentick.OTTrade)
     */
    public void onHistTrade(OTTrade trade)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onListExchanges(java.util.List)
     */
    public void onListExchanges(List exchanges)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onListSymbols(java.util.List)
     */
    public void onListSymbols(List symbols)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onLogin()
     */
    public void onLogin()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onMessage(com.opentick.OTMessage)
     */
    public void onMessage(OTMessage message)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onOptionInit(com.opentick.OTOptionInit)
     */
    public void onOptionInit(OTOptionInit optionInit)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBBO(com.opentick.OTBBO)
     */
    public void onRealtimeBBO(OTBBO bbo)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookCancel(com.opentick.OTBookCancel)
     */
    public void onRealtimeBookCancel(OTBookCancel cancel)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookChange(com.opentick.OTBookChange)
     */
    public void onRealtimeBookChange(OTBookChange change)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookDelete(com.opentick.OTBookDelete)
     */
    public void onRealtimeBookDelete(OTBookDelete delete)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookExecute(com.opentick.OTBookExecute)
     */
    public void onRealtimeBookExecute(OTBookExecute execute)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookOrder(com.opentick.OTBookOrder)
     */
    public void onRealtimeBookOrder(OTBookOrder order)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookPriceLevel(com.opentick.OTBookPriceLevel)
     */
    public void onRealtimeBookPriceLevel(OTBookPriceLevel level)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookPurge(com.opentick.OTBookPurge)
     */
    public void onRealtimeBookPurge(OTBookPurge purge)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeBookReplace(com.opentick.OTBookReplace)
     */
    public void onRealtimeBookReplace(OTBookReplace replace)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeMMQuote(com.opentick.OTMMQuote)
     */
    public void onRealtimeMMQuote(OTMMQuote quote)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeQuote(com.opentick.OTQuote)
     */
    public void onRealtimeQuote(OTQuote quote)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRealtimeTrade(com.opentick.OTTrade)
     */
    public void onRealtimeTrade(OTTrade trade)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onRestoreConnection()
     */
    public void onRestoreConnection()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onSplit(com.opentick.OTSplit)
     */
    public void onSplit(OTSplit split)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onStatusChanged(int)
     */
    public void onStatusChanged(int status)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.opentick.internal.IClientListener#onTodaysOHL(com.opentick.OTTodaysOHL)
     */
    public void onTodaysOHL(OTTodaysOHL todayOHL)
    {
    }
}
