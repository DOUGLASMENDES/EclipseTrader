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

import java.util.Date;

/**
 * Interface that represents the prices of a stock exchange item.<br>
 */
public interface IPriceData
{

  /**
   * Method to return the lastPrice field.<br>
   *
   * @return Returns the lastPrice.
   */
  public abstract double getLastPrice();

  /**
   * Method to set the lastPrice field.<br>
   * 
   * @param lastPrice The lastPrice to set.
   */
  public abstract void setLastPrice(double lastPrice);

  /**
   * Method to return the openPrice field.<br>
   *
   * @return Returns the openPrice.
   */
  public abstract double getOpenPrice();

  /**
   * Method to set the openPrice field.<br>
   * 
   * @param openPrice The openPrice to set.
   */
  public abstract void setOpenPrice(double openPrice);

  /**
   * Method to return the highPrice field.<br>
   *
   * @return Returns the highPrice.
   */
  public abstract double getHighPrice();

  /**
   * Method to set the highPrice field.<br>
   * 
   * @param highPrice The highPrice to set.
   */
  public abstract void setHighPrice(double highPrice);

  /**
   * Method to return the lowPrice field.<br>
   *
   * @return Returns the lowPrice.
   */
  public abstract double getLowPrice();

  /**
   * Method to set the lowPrice field.<br>
   * 
   * @param lowPrice The lowPrice to set.
   */
  public abstract void setLowPrice(double lowPrice);

  /**
   * Method to return the lastPriceVariance field.<br>
   *
   * @return Returns the lastPriceVariance.
   */
  public abstract double getLastPriceVariance();

  /**
   * Method to return the date field.<br>
   *
   * @return Returns the date.
   */
  public abstract Date getDate();

  /**
   * Method to set the date field.<br>
   * 
   * @param date The date to set.
   */
  public abstract void setDate(Date date);
}
