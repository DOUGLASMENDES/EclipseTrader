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
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.ICollectionObserver;

/**
 */
public class IndicatorsCollection
{
  private List objects = new ArrayList();
  private List observers = new ArrayList();

  public boolean add(IChartPlotter obj)
  {
    if (objects.indexOf(obj) != -1)
      return false;
    
    boolean value = objects.add(obj);
    if (value)
    {
      for (Iterator iter = observers.iterator(); iter.hasNext(); )
        ((ICollectionObserver)iter.next()).itemAdded(obj);
    }
    
    return value;
  }

  public boolean add(IndicatorPlugin obj)
  {
    if (objects.indexOf(obj) != -1)
      return false;
    
    boolean value = objects.add(obj);
    if (value)
    {
      for (Iterator iter = observers.iterator(); iter.hasNext(); )
        ((ICollectionObserver)iter.next()).itemAdded(obj);
    }
    
    return value;
  }

  public boolean remove(IChartPlotter arg0)
  {
    boolean value = objects.remove(arg0);
    if (value && arg0 != null)
    {
      for (Iterator iter = observers.iterator(); iter.hasNext(); )
        ((ICollectionObserver)iter.next()).itemRemoved(arg0);
    }
    return value;
  }

  public boolean remove(IndicatorPlugin arg0)
  {
    boolean value = objects.remove(arg0);
    if (value && arg0 != null)
    {
      for (Iterator iter = observers.iterator(); iter.hasNext(); )
        ((ICollectionObserver)iter.next()).itemRemoved(arg0);
    }
    return value;
  }
  
  public Object get(int index)
  {
    return objects.get(index);
  }
  
  public int size()
  {
    return objects.size();
  }
  
  public Iterator iterator()
  {
    return objects.iterator();
  }
  
  public int indexOf(Object obj)
  {
    return objects.indexOf(obj);
  }
  
  public void clear()
  {
    objects.clear();
  }
  
  public void addObserver(ICollectionObserver observer)
  {
    if (!observers.contains(observer)) 
      observers.add(observer);
  }
  
  public void removeObserver(ICollectionObserver observer)
  {
    observers.remove(observer);
  }
}
