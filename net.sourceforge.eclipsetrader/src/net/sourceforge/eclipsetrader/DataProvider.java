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

import java.util.HashMap;
import java.util.Vector;

import net.sourceforge.eclipsetrader.internal.StreamingControl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Default implementation of the IBasicDataProvider interface.
 * <p>Plugin developers may extend this class instead of implementing the interface and
 * override the startStreaming and stopStreaming methods only.</p>
 * <p></p>
 * 
 * @author Marco Maccaferri - 10/08/2004
 */
public class DataProvider extends Plugin implements IBasicDataProvider, IExecutableExtension
{
  private static DataProvider instance;
  private Vector _listeners = new Vector();
  private HashMap _dataListeners = new HashMap();
  private Thread thread;
  private boolean streaming = false;
  protected IExtendedData[] data;
  
  public static DataProvider getInstance() { return instance; }

  public DataProvider()
  {
    super();
    instance = this;
  }
  
  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
   */
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
  {
  }

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
  }
  
  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception
  {
    stopStreaming();
    super.stop(context);
  }

  /**
   * Add a data update listener.<br>
   * The default implementation will call the startStreaming() method after the first
   * listener is added.<br>
   * 
   * @param listener The listener to be removed.
   */
  public void addDataListener(IDataUpdateListener listener)
  {
    _listeners.add(listener);
//    if (_listeners.size() == 1 && _dataListeners.size() == 0)
//      startStreaming();
  }

  /**
   * Add a data update listener specific to a data item.<br>
   * The default implementation will call the startStreaming() method after the first
   * listener is added.<br>
   *
   * @param data The data item. 
   * @param listener The listener to be removed.
   */
  public void addDataListener(IBasicData data, IDataUpdateListener listener)
  {
    Vector _v = (Vector)_dataListeners.get(data.getSymbol());
    if (_v == null)
    {
      _v = new Vector();
      _dataListeners.put(data.getSymbol(), _v);
    }
    _v.add(listener);
//    if (_listeners.size() == 0 && _dataListeners.size() == 1)
//      startStreaming();
  }

  /**
   * Notify the listeners of a data updated event.<br>
   */
  public void fireDataUpdated()
  {
    for (int i = 0; i < _listeners.size(); i++)
    {
      IDataUpdateListener l = (IDataUpdateListener)_listeners.elementAt(i);
      l.dataUpdated(this);
    }
  }

  /**
   * Notify the data item's listeners of a data updated event.<br>
   */
  public void fireDataUpdated(IBasicData data)
  {
    Vector _v = (Vector)_dataListeners.get(data.getSymbol());
    if (_v != null)
    {
      for (int i = 0; i < _v.size(); i++)
      { 
        IDataUpdateListener l = (IDataUpdateListener)_v.elementAt(i);
        l.dataUpdated(this, data);
      }
    }
  }
  
  /**
   * Remove a data update listener specific to a data item.<br>
   * The default implementation will call the stopStreaming() method after the last
   * listener is removed.<br>
   * 
   * @param listener The listener to be removed.
   */
  public void removeDataListener(IDataUpdateListener listener)
  {
    _listeners.remove(listener);
//    if (_listeners.size() == 0 && _dataListeners.size() == 0)
//      stopStreaming();
  }

  /**
   * Remove a data update listener.<br>
   * The default implementation will call the stopStreaming() method after the last
   * listener is removed.<br>
   *
   * @param data The data item. 
   * @param listener The listener to be removed.
   */
  public void removeDataListener(IBasicData data, IDataUpdateListener listener)
  {
    Vector _v = (Vector)_dataListeners.get(data.getSymbol());
    if (_v != null)
    {
      _v.remove(listener);
      if (_v.size() == 0)
        _dataListeners.remove(data.getSymbol());
    }
//    if (_listeners.size() == 0 && _dataListeners.size() == 0)
//      stopStreaming();
  }

  /**
   * Called when the data provider is no longer needed.<br>
   * The default implementation stops the data streaming and removes all data
   * listeners.
   */
  public void dispose()
  {
    stopStreaming();
    _listeners.removeAllElements();
    _dataListeners.clear();
  }

  /**
   * Set the data items for which the streaming is required.<br>
   * 
   * @param data The data array.
   */
  public void setData(IExtendedData[] data)
  {
    this.data = data;
  }

  /**
   * Return an array with the data items for which the streaming is required.<br>
   * 
   * @return The data array.
   */
  public IExtendedData[] getData()
  {
    return data;
  }

  /**
   * Start the data streaming.<br>
   * The default implementation does nothing.
   */
  public void startStreaming()
  {
    System.out.println(this.getClass() + ": startStreaming");
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
    System.out.println(this.getClass() + ": stopStreaming");
  }
}
