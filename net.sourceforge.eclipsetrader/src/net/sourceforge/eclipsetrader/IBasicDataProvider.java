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
 * Interface for implementing a basic data provider.
 * <p>Basic data providers are capable of retriving the basic set of data regarding
 * a stock exchange item, such as the last trade price, the bid and ask prices/sizes,
 * the total volume of exchanges.</p>
 * 
 * @see net.sourceforge.eclipsetrader.IBookDataProvider
 * 
 * @author Marco Maccaferri - 16/08/2004
 */
public interface IBasicDataProvider
{
 
  /**
   * Return an array with the data items for which the streaming is required.<br>
   * 
   * @return The data array.
   */
  public IExtendedData[] getData();

  /**
   * Add a data update listener.<br>
   * 
   * @param listener Listener object.
   */
  public void addDataListener(IDataUpdateListener listener);
  
  /**
   * Add a data update listener specific for the portfolio item.<br>
   * 
   * @param data Portfolio item.
   * @param listener Listener object.
   */
  public void addDataListener(IBasicData data, IDataUpdateListener listener);
  
  /**
   * Remove a data update listener.<br>
   * 
   * @param listener Listener object.
   */
  public void removeDataListener(IDataUpdateListener listener);
  
  /**
   * Remove a data update listener specific for the portfolio item.<br>
   * 
   * @param data Portfolio item.
   * @param listener Listener object.
   */
  public void removeDataListener(IBasicData data, IDataUpdateListener listener);

  /**
   * Start the data streaming.<br>
   */
  public void startStreaming();
  
  /**
   * Test if the streaming was started successfuly.
   * <p></p>
   * @return true if the streaming was started, false otherwise.
   */
  public boolean isStreaming();

  /**
   * Stop the data streaming.<br>
   */
  public void stopStreaming();
  
  /**
   * Called when the plugin is disposed.<br>
   */
  public void dispose();
}
