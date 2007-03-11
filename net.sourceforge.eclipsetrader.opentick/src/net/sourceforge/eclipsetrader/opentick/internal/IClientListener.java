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

public interface IClientListener
{

    public void onRestoreConnection();
    
    public void onStatusChanged(int status);
    
    public void onLogin();
    
    public void onError(OTError error);
    
    public void onMessage(OTMessage message);
    
    public void onListExchanges(List exchanges);
    
    public void onListSymbols(List symbols);
    
    public void onRealtimeMMQuote(OTMMQuote quote);
    
    public void onRealtimeQuote(OTQuote quote);
    
    public void onRealtimeTrade(OTTrade trade);
    
    public void onRealtimeBBO(OTBBO bbo);
    
    public void onEquityInit(OTEquityInit equity);
    
    public void onTodaysOHL(OTTodaysOHL todayOHL);
    
    public void onHistQuote(OTQuote quote);
    
    public void onHistMMQuote(OTMMQuote quote);
    
    public void onHistTrade(OTTrade trade);
    
    public void onHistBBO(OTBBO bbo);
    
    public void onHistOHLC(OTOHLC ohlc);
    
    public void onRealtimeBookOrder(OTBookOrder order);
    
    public void onRealtimeBookExecute(OTBookExecute execute);
    
    public void onRealtimeBookCancel(OTBookCancel cancel);
    
    public void onRealtimeBookDelete(OTBookDelete delete);
    
    public void onRealtimeBookChange(OTBookChange change);
    
    public void onRealtimeBookReplace(OTBookReplace replace);
    
    public void onRealtimeBookPurge(OTBookPurge purge);
    
    public void onRealtimeBookPriceLevel(OTBookPriceLevel level);
    
    public void onSplit(OTSplit split);
    
    public void onDividend(OTDividend dividend);
    
    public void onHistBookCancel(OTBookCancel bookCancel);
    
    public void onHistBookChange(OTBookChange bookChange);
    
    public void onHistBookDelete(OTBookDelete bookDelete);
    
    public void onHistBookExecute(OTBookExecute bookExecute);
    
    public void onHistBookOrder(OTBookOrder bookOrder);
    
    public void onHistBookPriceLevel(OTBookPriceLevel bookPriceLevel);
    
    public void onHistBookPurge(OTBookPurge bookPurge);
    
    public void onHistBookReplace(OTBookReplace bookReplace);
    
    public void onOptionInit(OTOptionInit optionInit);
}
