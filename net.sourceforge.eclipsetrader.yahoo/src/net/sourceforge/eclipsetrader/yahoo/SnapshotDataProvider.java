/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.yahoo;

import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.RealtimeChartDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.yahoo.internal.Streamer;
import net.sourceforge.eclipsetrader.yahoo.internal.SymbolMapper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @author Marco Maccaferri
 */
public class SnapshotDataProvider extends RealtimeChartDataProvider
{
  private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private SimpleDateFormat df_us = new SimpleDateFormat("MM/dd/yyyy h:mma");
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
  private Streamer streamer = Streamer.getInstance();
  
  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
   */
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataStreamer#startStreaming()
   */
  public void startStreaming()
  {
    data = TraderPlugin.getData();
    for (int i = 0; i < data.length; i++)
      streamer.addSymbol(SymbolMapper.getYahooSymbol(data[i].getTicker()));

    streamer.addListener(this);
    streamer.start();
    super.startStreaming();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataStreamer#stopStreaming()
   */
  public void stopStreaming()
  {
    streamer.stop();
    streamer.removeListener(this);

    data = TraderPlugin.getData();
    for (int i = 0; i < data.length; i++)
      streamer.removeSymbol(SymbolMapper.getYahooSymbol(data[i].getTicker()));

    super.stopStreaming();
  }
  
  public void update()
  {
    data = TraderPlugin.getData();
    for (int i = 0; i < data.length; i++) 
    {
      IExtendedData ed = streamer.getData(SymbolMapper.getYahooSymbol(data[i].getTicker()));
      data[i].setLastPrice(ed.getLastPrice());
      data[i].setBidPrice(ed.getBidPrice());
      data[i].setBidSize(ed.getBidSize());
      data[i].setAskPrice(ed.getAskPrice());
      data[i].setAskSize(ed.getAskSize());
      data[i].setOpenPrice(ed.getOpenPrice());
      data[i].setHighPrice(ed.getHighPrice());
      data[i].setLowPrice(ed.getLowPrice());
      data[i].setClosePrice(ed.getClosePrice());
      data[i].setVolume(ed.getVolume());
      data[i].setDate(ed.getDate());
    }
    
    // Signal the update to all listeners.
    fireDataUpdated();
  }
}
