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


/**
 * Interface for implementing a data storage.
 */
public interface IDataStore
{
  
  /**
   * Initializes the data store.
   */
  public void initialize();
  
  /**
   * Terminate the use of the data store.
   */
  public void terminate();
  
  /**
   * Returns the portfolio data.<br>
   * 
   * @return The data collection.
   */
  public ObservableCollection getStockwatchData();

  /**
   * Updates the portfolio data with the given data array, adding, removing
   * or updating data as needed.<br>
   * 
   * @param data The new data array.
   */
  public void update(IExtendedData[] data);

  public IExtendedData[] loadIndexData();
  public void storeIndexData(IExtendedData[] data);

  /**
   * Load the historical chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @return An array of IChartData items.
   */
  public IChartData[] getHistoricalData(IBasicData data);

  /**
   * Stores the historical chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @param chartData - The array of IChartData items to store. 
   */
  public void storeHistoryData(IBasicData data, IChartData[] chartData);

  /**
   * Load the intraday chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @return An array of IChartData items.
   */
  public IChartData[] getIntradayData(IBasicData data);

  /**
   * Stores the intraday chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @param chartData - The array of IChartData items to store. 
   */
  public void storeIntradayData(IBasicData data, IChartData[] chartData);
}
