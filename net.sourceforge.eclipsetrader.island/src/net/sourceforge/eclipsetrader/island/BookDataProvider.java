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
package net.sourceforge.eclipsetrader.island;

import java.util.HashMap;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookDataProvider;
import net.sourceforge.eclipsetrader.IBookUpdateListener;
import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 */
public class BookDataProvider implements IBookDataProvider, IPropertyChangeListener
{
  private HashMap bookData = new HashMap();
  
  public BookDataProvider()
  {
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#dispose()
   */
  public void dispose()
  {
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    Iterator i = bookData.values().iterator();
    while(i.hasNext() == true)
      ((PriceBook)i.next()).runThread = false;
    bookData.clear();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#addBookListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IBookUpdateListener)
   */
  public void addBookListener(IBasicData data, IBookUpdateListener listener)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb == null)
    {
      pb = new PriceBook();
      pb.data = data;
      bookData.put(data.getSymbol(), pb);
    }
    pb.listeners.addElement(listener);
System.out.println(getClass().getName() + ": Add book listener for " + data.getSymbol());
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#getAskData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IBookData[] getAskData(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb == null)
      return pb.sell;
    return null;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#getBidData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IBookData[] getBidData(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb == null)
      return pb.buy;
    return null;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#removeBookListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IBookUpdateListener)
   */
  public void removeBookListener(IBasicData data, IBookUpdateListener listener)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb != null)
    {
System.out.println(getClass().getName() + ": Remove book listener for " + data.getSymbol());
      pb.listeners.removeElement(listener);
      if (pb.listeners.size() == 0)
      {
        stopBook(data);
        bookData.remove(data.getSymbol());
      }
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#startBook(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void startBook(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb != null && TraderPlugin.getDefault().getPreferenceStore().getBoolean("net.sourceforge.eclipsetrader.streaming") == true)
    {
      new Thread(pb).start();
System.out.println(getClass() + ": Start book for " + data.getSymbol());
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#stopBook(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void stopBook(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb != null)
    {
      pb.runThread = false;
System.out.println(getClass().getName() + ": Stop book for " + data.getSymbol());
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.streaming") == true)
    {
      System.out.println(property + "=" + TraderPlugin.getDefault().getPreferenceStore().getBoolean(property));
      if (TraderPlugin.getDefault().getPreferenceStore().getBoolean(property) == true)
      {
        Iterator i = bookData.values().iterator();
        while(i.hasNext() == true)
          new Thread((Runnable)i.next()).start();
      }
      else
      {
        Iterator i = bookData.values().iterator();
        while(i.hasNext() == true)
          ((PriceBook)i.next()).runThread = false;
      }
    }
  }
}
