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
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.IChartData;

/**
 */
public class BarData
{
  public static final int OPEN = 0;
  public static final int HIGH = 1;
  public static final int LOW = 2;
  public static final int CLOSE = 3;
  public static final int VOLUME = 4;
  private List objects = new ArrayList();
  private double high = -99999999;
  private double low = 99999999;

  public boolean add(IChartData obj)
  {
    if (objects.indexOf(obj) != -1)
      return false;

    if (obj.getMaxPrice() > high)
      high = obj.getMaxPrice();
    if (obj.getMinPrice() < low)
      low = obj.getMinPrice();
    
    return objects.add(obj);
  }
  
  public void addAll(IChartData[] data)
  {
    for(int i = 0; i < data.length; i++)
      add(data[i]);
  }

  public boolean remove(IChartData arg0)
  {
    return objects.remove(arg0);
  }
  
  public IChartData get(int index)
  {
    return (IChartData)objects.get(index);
  }
  
  public int size()
  {
    return objects.size();
  }
  
  public Iterator iterator()
  {
    return objects.iterator();
  }
  
  public int indexOf(IChartPlotter obj)
  {
    return objects.indexOf(obj);
  }
  
  public void clear()
  {
    objects.clear();
    high = -99999999;
    low = 99999999;
  }

  public PlotLine getInput(int field)
  {
    PlotLine plotLine = new PlotLine();
    
    for (int i = 0; i < size(); i++)
    {
      switch(field)
      {
        case OPEN:
          plotLine.add(get(i).getOpenPrice());
          break;
        case HIGH:
          plotLine.add(get(i).getMaxPrice());
          break;
        case LOW:
          plotLine.add(get(i).getMinPrice());
          break;
        case CLOSE:
          plotLine.add(get(i).getClosePrice());
          break;
        case VOLUME:
          plotLine.add(get(i).getVolume());
          break;
      }
    }
    
    return plotLine;
  }
  
  public double getClose(int index)
  {
    return get(index).getClosePrice();
  }
  
  public double getOpen(int index)
  {
    return get(index).getOpenPrice();
  }
  
  public double getHigh(int index)
  {
    return get(index).getMaxPrice();
  }
  
  public double getLow(int index)
  {
    return get(index).getMinPrice();
  }
  
  public Date getDate(int x)
  {
    return get(x).getDate();
  }
  
  public int getX(Date date)
  {
    for (int i = 0; i < size(); i++)
    {
      if (get(i).getDate().equals(date) || get(i).getDate().before(date))
        return i;
    }
    
    return -1;
  }
  
  public double getHigh()
  {
    return this.high;
  }
  
  public double getLow()
  {
    return this.low;
  }
}
