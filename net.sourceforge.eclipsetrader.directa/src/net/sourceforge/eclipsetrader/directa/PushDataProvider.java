/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.directa;

import java.util.List;

import net.sourceforge.eclipsetrader.DataCollector;
import net.sourceforge.eclipsetrader.DataProvider;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.IRealtimeChartListener;
import net.sourceforge.eclipsetrader.IRealtimeChartProvider;
import net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver;
import net.sourceforge.eclipsetrader.directa.internal.Streamer;


/**
 */
public class PushDataProvider extends DataProvider implements IStreamerEventReceiver, IRealtimeChartProvider
{
  private DataCollector dataCollector = DataCollector.getInstance();


  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBasicDataProvider#startStreaming()
   */
  public void startStreaming()
  {
    Streamer streamer = Streamer.getInstance();
    if (streamer.isLoggedIn() == false)
    {
      if (DirectaPlugin.connectServer() == false)
        return;
    }

    streamer.addEventReceiver(this);
    streamer.readInitialData();
    if (streamer.connect() == true)
    {
      new Thread(streamer).start();
      super.startStreaming();
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBasicDataProvider#stopStreaming()
   */
  public void stopStreaming()
  {
    Streamer streamer = Streamer.getInstance();
    streamer.removeEventReceiver(this);
    streamer.disconnect();
    super.stopStreaming();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBasicDataProvider#setData(net.sourceforge.eclipsetrader.IExtendedData[])
   */
  public void setData(IExtendedData[] data)
  {
    super.setData(data);
    if (this.isStreaming() == true)
    {
      Streamer streamer = Streamer.getInstance();
      streamer.disconnect();
      streamer.readInitialData();
      if (streamer.connect() == true)
      {
        new Thread(streamer).start();
        super.startStreaming();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#dataUpdated()
   */
  public void dataUpdated()
  {
    fireDataUpdated();
    dataCollector.dataUpdated(this);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#dataUpdated(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void dataUpdated(IBasicData data)
  {
    fireDataUpdated(data);
    if (data instanceof IExtendedData)
      dataCollector.dataUpdated((IExtendedData)data);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#orderStatusChanged()
   */
  public void orderStatusChanged()
  {
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#getHistoryData(net.sourceforge.eclipsetrader.IChartData)
   */
  public IChartData[] getHistoryData(IBasicData data)
  {
    return dataCollector.getHistoryData(data);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#setHistoryData(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setHistoryData(IBasicData data, IChartData[] chartData)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#addRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void addRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    dataCollector.addRealtimeChartListener(data, listener);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#backfill(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void backfill(IBasicData data)
  {
    Streamer streamer = Streamer.getInstance();
    if (streamer.isLoggedIn() == false)
    {
      if (DirectaPlugin.connectServer() == false)
        return;
    }
    int period = DirectaPlugin.getDefault().getPreferenceStore().getInt("rtcharts.update");
    List values = streamer.backfill(data, period);
    dataCollector.setData(data.getSymbol(), values);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#removeRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    dataCollector.removeRealtimeChartListener(data, listener);
  }
}
