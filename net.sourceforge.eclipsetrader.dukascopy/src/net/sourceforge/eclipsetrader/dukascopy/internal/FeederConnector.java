/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Stephen Bate     - Dukascopy plugin
 *******************************************************************************/
package net.sourceforge.eclipsetrader.dukascopy.internal;

import java.util.HashMap;
import java.util.Iterator;

import ru.dukascopy.feeder.client.Connector;
import ru.dukascopy.feeder.client.DataListener;
import ru.dukascopy.feeder.client.ticker.TickerListener;
import ru.dukascopy.feeder.client.ticker.TickerWorker;

/**
 * @author Steve Bate (modifications)
 * @author Vladimir Pletnyov (original)
 * 
 * This is a modified version of the FeedConnector provided by Dukascopy.
 */
public class FeederConnector
{
  private HashMap quoteIdForSymbol = new HashMap();

  private HashMap symbolForQuoteId = new HashMap();

  private TickerWorker tw = new TickerWorker();

  private Connector connector = new Connector("ticker", tw, true);

  private DataListener listener;

  public FeederConnector()
  {
    tw.setListener(new TickerListener() {
      public void onNewTick(int id, double value, int volume)
      {
        if (null != listener)
        {
          listener.onNewTick(id, value, volume);
        }
      }

      public void onNewConnection(Connector conn)
      {
        synchronized (quoteIdForSymbol)
        {
          Iterator itr = quoteIdForSymbol.keySet().iterator();
          while (itr.hasNext())
          {
            try
            {
              String code = (String) itr.next();
              Integer id = (Integer) quoteIdForSymbol.get(code);
              conn.sendCommand("connect", code + ":" + id);
            } catch (Exception ex)
            {
            }
          }
        }
      }
    });
  }

  public void connect()
  {
    connector.addRoute("datafeed.dukascopy.com", 9999);
  }

  public void setDataListener(DataListener listener)
  {
    this.listener = listener;
  }

  public synchronized void subscribe(int id, String code)
  {
    try
    {
      quoteIdForSymbol.put(code, new Integer(id));
      symbolForQuoteId.put(new Integer(id), code);
      connector.sendCommand("connect", code + ":" + id);
    } catch (Exception ex)
    {
      // TODO log this error
    }
  }

  public String getSymbolForQuoteId(int quoteId)
  {
    return (String) symbolForQuoteId.get(new Integer(quoteId));
  }

  public synchronized void unsubscribe(String code)
  {
    try
    {
      quoteIdForSymbol.remove(code);
      connector.sendCommand("disconnect", code);
    } catch (Exception ex)
    {
    }
  }

  public void disconnect()
  {
    synchronized (quoteIdForSymbol)
    {
      quoteIdForSymbol.clear();
      symbolForQuoteId.clear();
    }
    connector.disconnect();
  }

  public boolean isSubscribed(String symbol)
  {
    return quoteIdForSymbol.containsKey(symbol);
  }
}