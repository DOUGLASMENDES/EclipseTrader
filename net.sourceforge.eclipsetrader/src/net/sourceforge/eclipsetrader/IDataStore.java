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
   * Updates the portfolio data with the given data array, adding, removing
   * or updating data as needed.<br>
   * 
   * @param data The new data array.
   */
  public void update(IExtendedData[] data);

  public IExtendedData[] loadIndexData();
  public void storeIndexData(IExtendedData[] data);
}
