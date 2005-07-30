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
package net.sourceforge.eclipsetrader.dukascopy;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.RealtimeChartDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.dukascopy.internal.FeederConnector;
import ru.dukascopy.feeder.client.DataListener;

/**
 * @author Steve Bate
 */
public class SnapshotDataProvider extends RealtimeChartDataProvider
{
  private FeederConnector feed;

  private int quoteId = 1;

  public synchronized void startStreaming()
  {
    DukascopyPlugin.getDefault().log(Messages.getString("SnapshotDataProvider.streamingStarted")); //$NON-NLS-1$
    super.startStreaming();
    feed = new FeederConnector();
    feed.setDataListener(new DataListener() {
      public void onNewTick(int id, double price, int volume)
      {
        IExtendedData d = null;
        IExtendedData[] data = getData();
        String symbol = feed.getSymbolForQuoteId(id);
        for (int i = 0; i < data.length; i++)
        {
          if (data[i].getSymbol().equals(symbol))
          {
            d = data[i];
            break;
          }
        }
        if (d != null)
        {
//          System.out.println("ON NEWTICK " + symbol + " " + price + " " + volume);
          d.setLastPrice(price);
          d.setVolume(volume);
          fireDataUpdated(d);
        }
      }
    });
    feed.connect();

    for (Iterator iter = TraderPlugin.getDataStore().getStockwatchData().iterator(); iter.hasNext(); )
    {
      IExtendedData data = (IExtendedData)iter.next();
      String symbol = data.getSymbol();
      if (!feed.isSubscribed(symbol))
      {
        feed.subscribe(quoteId++, symbol);
        DukascopyPlugin.getDefault().log(Messages.getString("SnapshotDataProvider.subscribed") + " " + quoteId + " " + symbol); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
  }

  public void stopStreaming()
  {
    DukascopyPlugin.getDefault().log(Messages.getString("SnapshotDataProvider.streamingStopped")); //$NON-NLS-1$
    super.stopStreaming();
    feed.disconnect();
  }
}