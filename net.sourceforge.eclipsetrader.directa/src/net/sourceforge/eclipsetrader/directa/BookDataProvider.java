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

import java.util.HashMap;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookDataProvider;
import net.sourceforge.eclipsetrader.IBookUpdateListener;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.directa.internal.PriceBook;
import net.sourceforge.eclipsetrader.directa.internal.Streamer;


/**
 * Instances of this class are providing Level II / Book price data. 
 */
public class BookDataProvider implements IBookDataProvider, IPropertyChangeListener
{
  private static BookDataProvider bookDataProvider;
  public HashMap bookData = new HashMap();
  
  /**
   * Constructs an instance of this class.
   */
  public BookDataProvider()
  {
    if (bookDataProvider != null)
      bookDataProvider.dispose();
    bookDataProvider = this;
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }
  
  /**
   * Gets the singleton instance of this class.
   */
  public static BookDataProvider getDefault()
  {
    if (bookDataProvider == null)
      bookDataProvider = new BookDataProvider();
    return bookDataProvider;
  }
  
  public void dispose()
  {
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    bookData.clear();
    bookDataProvider = null;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#getBidData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IBookData[] getBidData(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getTicker());
    if (pb == null)
      return null;
    return pb.bid;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#getAskData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IBookData[] getAskData(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getTicker());
    if (pb == null)
      return null;
    return pb.ask;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#startBook(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void startBook(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getTicker());
    if (pb == null)
      bookData.put(data.getTicker(), new PriceBook());

    Streamer streamer = Streamer.getInstance();
    if (streamer.isLoggedIn() == true)
      streamer.readInitialBookValues(data);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#stopBook(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void stopBook(IBasicData data)
  {
    bookData.remove(data.getTicker());
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#addBookListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IBookUpdateListener)
   */
  public void addBookListener(IBasicData data, IBookUpdateListener listener)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getTicker());
    if (pb == null)
    {
      pb = new PriceBook();
      bookData.put(data.getTicker(), pb);
    }
    pb.listeners.addElement(listener);
    System.out.println("Added book listener on " + data.getTicker());
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#removeBookListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IBookUpdateListener)
   */
  public void removeBookListener(IBasicData data, IBookUpdateListener listener)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getTicker());
    if (pb != null)
      pb.listeners.removeElement(listener);
    System.out.println("Removed book listener on " + data.getTicker());
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
        if (! (TraderPlugin.getDataProvider() instanceof PushDataProvider))
        {
          Streamer streamer = Streamer.getInstance();
          if (streamer.isLoggedIn() == false)
          {
            if (DirectaPlugin.connectServer() == false)
              return;
          }
          streamer.readInitialData();
          if (streamer.connect() == true)
            new Thread(streamer).start();
        }
      }
    }
  }
}
