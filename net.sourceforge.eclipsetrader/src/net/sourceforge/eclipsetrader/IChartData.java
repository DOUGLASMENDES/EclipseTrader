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
 * Placeholder for chart data.
 * <p></p>
 * 
 * @author Marco Maccaferri - 14/08/2004
 */
public interface IChartData
{
  /**
   * Method to return the askPrice field.<br>
   *
   * @return Returns the askPrice.
   */
  public abstract double getAskPrice();

  /**
   * Method to set the askPrice field.<br>
   * 
   * @param askPrice The askPrice to set.
   */
  public abstract void setAskPrice(double askPrice);

  /**
   * Method to return the bidPrice field.<br>
   *
   * @return Returns the bidPrice.
   */
  public abstract double getBidPrice();

  /**
   * Method to set the bidPrice field.<br>
   * 
   * @param bidPrice The bidPrice to set.
   */
  public abstract void setBidPrice(double bidPrice);

  /**
   * Method to return the closePrice field.<br>
   *
   * @return Returns the closePrice.
   */
  public abstract double getClosePrice();

  /**
   * Method to set the closePrice field.<br>
   * 
   * @param closePrice The closePrice to set.
   */
  public abstract void setClosePrice(double closePrice);

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

  /**
   * Method to return the maxPrice field.<br>
   *
   * @return Returns the maxPrice.
   */
  public abstract double getMaxPrice();

  /**
   * Method to set the maxPrice field.<br>
   * 
   * @param maxPrice The maxPrice to set.
   */
  public abstract void setMaxPrice(double maxPrice);

  /**
   * Method to return the minPrice field.<br>
   *
   * @return Returns the minPrice.
   */
  public abstract double getMinPrice();

  /**
   * Method to set the minPrice field.<br>
   * 
   * @param minPrice The minPrice to set.
   */
  public abstract void setMinPrice(double minPrice);

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
   * Method to return the price field.<br>
   *
   * @return Returns the price.
   */
  public abstract double getPrice();

  /**
   * Method to set the price field.<br>
   * 
   * @param price The price to set.
   */
  public abstract void setPrice(double price);

  /**
   * Method to return the volume field.<br>
   *
   * @return Returns the volume.
   */
  public abstract int getVolume();

  /**
   * Method to set the volume field.<br>
   * 
   * @param volume The volume to set.
   */
  public abstract void setVolume(int volume);
}