/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.monitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.IBasicData;

/**
 * Instances of this class represent monitor objects for the intraday chart data.
 */
public class IntradayChartMonitor
{
  private static IntradayChartMonitor instance = new IntradayChartMonitor();
  private Map objects = new HashMap();
  
  private IntradayChartMonitor()
  {
  }
  
  /**
   * Returns the singleton instance of this class.
   * 
   * @return - the singleton instance
   */
  public static IntradayChartMonitor getInstance()
  {
    return instance;
  }

  /**
   * Add a listener for updates to the given chart object.
   * 
   * @param obj - the chart object to monitor
   * @param listener - the listener that receive update notifications
   */
  public void addMonitor(IBasicData obj, IMonitorListener listener)
  {
    List list = (List)objects.get(obj);
    if (list == null)
    {
      list = new ArrayList();
      objects.put(obj, list);
    }
    if (!list.contains(listener))
      list.add(listener);
  }
  
  /**
   * Remove a listener for updates to the given chart object.
   * 
   * @param obj - the monitored chart object
   * @param listener - the listener to remove
   */
  public void removeMonitor(IBasicData obj, IMonitorListener listener)
  {
    List list = (List)objects.get(obj);
    if (list != null)
    {
      list.remove(listener);
      if (list.size() == 0)
        objects.remove(obj);
    }
  }

  /**
   * Notify all object listeners for an update to the given chart object.
   * 
   * @param obj - the updated chart object
   */
  public void notifyUpdate(IBasicData obj)
  {
    List list = (List)objects.get(obj);
    if (list != null)
    {
      for (Iterator iter = list.iterator(); iter.hasNext(); )
        ((IMonitorListener)iter.next()).monitoredDataUpdated(obj);
    }
  }
  
  public Map getObjects()
  {
    return objects;
  }
}
