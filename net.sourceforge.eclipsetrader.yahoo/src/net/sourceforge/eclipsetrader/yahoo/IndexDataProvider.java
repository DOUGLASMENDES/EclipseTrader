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
import java.util.Vector;

import net.sourceforge.eclipsetrader.DataCollector;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.IIndexUpdateListener;
import net.sourceforge.eclipsetrader.IRealtimeChartListener;
import net.sourceforge.eclipsetrader.IRealtimeChartProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.yahoo.internal.Streamer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 */
public class IndexDataProvider extends ChartDataProvider implements IIndexDataProvider, IRealtimeChartProvider, IPropertyChangeListener
{
  private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private SimpleDateFormat df_us = new SimpleDateFormat("MM/dd/yyyy h:mma");
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
  private Vector listeners = new Vector();
  private String[] symbols = new String[0];
  private IExtendedData[] data = new IExtendedData[0];
  private Streamer streamer = Streamer.getInstance();
  private DataCollector dataCollector = DataCollector.getInstance();
  private ChartDataProvider chartDataProvider = new ChartDataProvider();
  
  public IndexDataProvider()
  {
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    
    // Sets the default parameters for retrieving the indices data
    symbolField = 0;
    useMapping = false;
    defaultExtension = "";
  }
  
  public void dispose()
  {
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    listeners.clear();
    streamer.removeListener(this);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#addUpdateListener(net.sourceforge.eclipsetrader.ui.views.indices.IIndexUpdateListener)
   */
  public void addUpdateListener(IIndexUpdateListener listener)
  {
    if (listeners.contains(listener) == false)
      listeners.add(listener);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#removeUpdateListener(net.sourceforge.eclipsetrader.ui.views.indices.IIndexUpdateListener)
   */
  public void removeUpdateListener(IIndexUpdateListener listener)
  {
    listeners.remove(listener);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#setSymbols(java.lang.String[])
   */
  public void setSymbols(String[] symbols)
  {
    if (TraderPlugin.isStreaming() == true)
      streamer.removeListener(this);
    
    for (int i = 0; i < this.symbols.length; i++)
      streamer.removeSymbol(this.symbols[i]);
    
    this.symbols = symbols;
    
    data = new IExtendedData[symbols.length];
    for (int i = 0; i < symbols.length; i++)
    {
      data[i] = TraderPlugin.createExtendedData();
      setDescription(symbols[i], data[i]);
      streamer.addSymbol(symbols[i]);
    }

    if (TraderPlugin.isStreaming() == true)
      streamer.addListener(this);
  }
  
  private void setDescription(String symbol, IExtendedData data)
  {
    data.setSymbol(symbol);
    data.setTicker(symbol);
    data.setDescription(symbol);
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        if(members[i].getAttribute("id").equalsIgnoreCase("yahoo.indexProvider") == false)
          continue;

        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true)
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              if (symbol.equalsIgnoreCase(items[iii].getAttribute("symbol")) == true)
              {
                data.setDescription(items[iii].getAttribute("label"));
                return;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true)
          {
            if (symbol.equalsIgnoreCase(children[ii].getAttribute("symbol")) == true)
            {
              data.setDescription(children[ii].getAttribute("label"));
              return;
            }
          }
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#getIndexData()
   */
  public IExtendedData[] getIndexData()
  {
    return data;
  }

  public void update() 
  {
    for (int i = 0; i < data.length; i++) 
    {
      IExtendedData ed = streamer.getData(data[i].getSymbol());
      if (ed != null)
      {
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
        dataCollector.dataUpdated(data[i]);
      }
    }
    
    // Notify all listeners
    for (int i = 0; i < listeners.size(); i++)
      ((IIndexUpdateListener)listeners.get(i)).indexUpdate(this);
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
    dataCollector.backfill(data);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#getHistoryData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getHistoryData(IBasicData data)
  {
    return dataCollector.getHistoryData(data);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#removeRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    dataCollector.removeRealtimeChartListener(data, listener);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#setHistoryData(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setHistoryData(IBasicData data, IChartData[] chartData)
  {
    dataCollector.setHistoryData(data, chartData);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.streaming") == true)
    {
      if (TraderPlugin.isStreaming() == true)
      {
        streamer.addListener(this);
        streamer.start();
      }
      else
      {
        streamer.stop();
        streamer.removeListener(this);
      }
    }
  }
}
