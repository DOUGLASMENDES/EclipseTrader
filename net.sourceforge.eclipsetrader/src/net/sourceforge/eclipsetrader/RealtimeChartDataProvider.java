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
package net.sourceforge.eclipsetrader;


/**
 * Default implementation of the IRealtimeChartProvider interface.
 * <p>Plugin developers may extend this class instead of implementing the interface and
 * override the startStreaming and stopStreaming methods only.</p>
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class RealtimeChartDataProvider extends DataProvider implements IRealtimeChartProvider
{
  private DataCollector dataCollector = new DataCollector(this);

  /**
   * Notify the listeners of a data update event, and collects the data for
   * realtime chart generation.
   * <p></p>
   */
  public void fireDataUpdated()
  {
    super.fireDataUpdated();
    dataCollector.dataUpdated(this);
  }
  
  /**
   * Notify the listeners of a data update event, and collects the data for
   * realtime chart generation.
   * <p></p>
   */
  public void fireDataUpdated(IBasicData data)
  {
    super.fireDataUpdated(data);
    dataCollector.dataUpdated(this, data);
  }
  
  public DataCollector getDataCollector()
  {
    return dataCollector;
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
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#getHistoryData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getHistoryData(IBasicData data)
  {
    return dataCollector.getData(data.getSymbol());
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#removeRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    dataCollector.removeRealtimeChartListener(data, listener);
  }
}
