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
 * Interface for implementing a portfolio data storage.<br>
 * 
 * @author Marco Maccaferri - 11/08/2004
 */
public interface IDataStore
{
  
  /**
   * Read the portfolio data from a resource.<br>
   */
  public void load();
  
  /**
   * Writes the portfolio data to a resource.<br>
   */
  public void store();

  /**
   * Returns the portfolio data.<br>
   * 
   * @return The data array.
   */
  public IExtendedData[] getData();

  /**
   * Add an item to the portfolio data.
   * 
   * @param data The new data item.
   */
  public void add(IExtendedData data);

  /**
   * Remove an item from the portfolio data.
   * 
   * @param data The data item to remove.
   */
  public void remove(IExtendedData data);

  /**
   * Update the item at the give location.
   * 
   * @param index The position of the data item to update.
   * @param data The data that replaces the existing item.
   */
  public void update(int index, IExtendedData data);

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
  public IChartData[] loadHistoryData(IBasicData data);

  /**
   * Stores the historical chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @param chartData - The array of IChartData items to store. 
   */
  public void storeHistoryData(IBasicData data, IChartData[] chartData);
  
  public void addHistoryDataListener(IBasicData data, IChartDataListener listener);
  public void removeHistoryDataListener(IBasicData data, IChartDataListener listener);
}
