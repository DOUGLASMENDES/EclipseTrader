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
package net.sourceforge.eclipsetrader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import net.sourceforge.eclipsetrader.internal.StreamingControl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * Default implementation of the IBasicDataProvider interface.
 * <p>Plugin developers may extend this class instead of implementing the interface and
 * override the startStreaming and stopStreaming methods only.</p>
 * <p></p>
 */
public class DataProvider implements IBasicDataProvider, IExecutableExtension, ICollectionObserver
{
  private List _listeners = new ArrayList();
  private HashMap _dataListeners = new HashMap();
  private boolean streaming = false;
  private IExtendedData[] data;
  private Map symbolMap = new HashMap();
  private Map tickerMap = new HashMap();
  
  public DataProvider()
  {
    TraderPlugin.getDataStore().getStockwatchData().addObserver(this);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
   */
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
  {
  }

  /**
   * Add a data update listener.<br>
   * 
   * @param listener The listener to be removed.
   */
  public void addDataListener(IDataUpdateListener listener)
  {
    _listeners.add(listener);
  }

  /**
   * Add a data update listener specific to a data item.<br>
   *
   * @param data The data item. 
   * @param listener The listener to be removed.
   */
  public void addDataListener(IBasicData data, IDataUpdateListener listener)
  {
    List _v = (ArrayList)_dataListeners.get(data.getSymbol());
    if (_v == null)
    {
      _v = new ArrayList();
      _dataListeners.put(data.getSymbol(), _v);
    }
    _v.add(listener);
  }

  /**
   * Notify the listeners of a data updated event.<br>
   */
  public void fireDataUpdated()
  {
    for (int i = 0; i < _listeners.size(); i++)
    {
      IDataUpdateListener l = (IDataUpdateListener)_listeners.get(i);
      l.dataUpdated(this);
    }
    for (int i = 0; i < getData().length; i++)
    {
      if (getData()[i] instanceof Observable)
        ((Observable)getData()[i]).notifyObservers();
    }
  }

  /**
   * Notify the data item's listeners of a data updated event.<br>
   */
  public void fireDataUpdated(IBasicData data)
  {
    // Notify the specific listeners
    List _v = (ArrayList)_dataListeners.get(data.getSymbol());
    if (_v != null)
    {
      for (int i = 0; i < _v.size(); i++)
      { 
        IDataUpdateListener l = (IDataUpdateListener)_v.get(i);
        l.dataUpdated(this, data);
      }
    }
    if (data instanceof Observable)
      ((Observable)data).notifyObservers();
    
    // Notify generic listeners using the data-specific update notification method.
    // Avoids updating the same listener twice.
    for (int i = 0; i < _listeners.size(); i++)
    {
      IDataUpdateListener l = (IDataUpdateListener)_listeners.get(i);
      if (_v == null || _v.contains(l) == false)
        l.dataUpdated(this, data);
    }
  }
  
  /**
   * Remove a data update listener specific to a data item.<br>
   * 
   * @param listener The listener to be removed.
   */
  public void removeDataListener(IDataUpdateListener listener)
  {
    _listeners.remove(listener);
  }

  /**
   * Remove a data update listener.<br>
   *
   * @param data The data item. 
   * @param listener The listener to be removed.
   */
  public void removeDataListener(IBasicData data, IDataUpdateListener listener)
  {
    List _v = (ArrayList)_dataListeners.get(data.getSymbol());
    if (_v != null)
    {
      _v.remove(listener);
      if (_v.size() == 0)
        _dataListeners.remove(data.getSymbol());
    }
  }

  /**
   * Called when the data provider is no longer needed.<br>
   * The default implementation stops the data streaming and removes all data
   * listeners.
   */
  public void dispose()
  {
    stopStreaming();
    _listeners.clear();
    _dataListeners.clear();
    TraderPlugin.getDataStore().getStockwatchData().removeObserver(this);
  }

  /**
   * Return an array with the data items for which the streaming is required.<br>
   * 
   * @return The data array.
   */
  public IExtendedData[] getData()
  {
    if (data == null)
    {
      List list = TraderPlugin.getDataStore().getStockwatchData();
      data = new IExtendedData[list.size()];
      list.toArray(data);
    }
    return data;
  }
  
  public IExtendedData getSymbol(String symbol)
  {
    if (symbolMap.isEmpty() == true)
    {
      for (Iterator iter = TraderPlugin.getDataStore().getStockwatchData().iterator(); iter.hasNext(); )
      {
        IExtendedData data = (IExtendedData)iter.next();
        symbolMap.put(data.getSymbol(), data);
      }
    }
    
    return (IExtendedData)symbolMap.get(symbol);
  }
  
  public IExtendedData getTicker(String ticker)
  {
    if (tickerMap.isEmpty() == true)
    {
      for (Iterator iter = TraderPlugin.getDataStore().getStockwatchData().iterator(); iter.hasNext(); )
      {
        IExtendedData data = (IExtendedData)iter.next();
        tickerMap.put(data.getTicker(), data);
      }
    }
    
    return (IExtendedData)tickerMap.get(ticker);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ICollectionObserver#itemAdded(java.lang.Object)
   */
  public void itemAdded(Object obj)
  {
    data = null;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ICollectionObserver#itemRemoved(java.lang.Object)
   */
  public void itemRemoved(Object obj)
  {
    data = null;
  }

  /**
   * Start the data streaming.<br>
   * The default implementation does nothing.
   */
  public void startStreaming()
  {
    StreamingControl.actionStart.setEnabled(false);
    StreamingControl.actionStop.setEnabled(true);
    streaming = true;
    TraderPlugin.getDefault().getPreferenceStore().setValue("net.sourceforge.eclipsetrader.streaming", true);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBasicDataProvider#isStreaming()
   */
  public boolean isStreaming()
  {
    return streaming;
  }

  /**
   * Stop the data streaming.<br>
   * The default implementation does nothing.
   */
  public void stopStreaming()
  {
    TraderPlugin.getDefault().getPreferenceStore().setValue("net.sourceforge.eclipsetrader.streaming", false);
    streaming = false;
    StreamingControl.actionStart.setEnabled(true);
    StreamingControl.actionStop.setEnabled(false);
  }
}
