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
package net.sourceforge.eclipsetrader.internal;

import java.util.Date;

import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IExtendedData;


/**
 * Default implementation of the IChartData interface.<br>
 * 
 * @author Marco Maccaferri - 14/08/2004
 */
public class ChartData implements IChartData 
{
  private Date date = new Date();
  private double price = 0;
  private double bidPrice = 0;
  private double askPrice = 0;
  private double openPrice = 0;
  private double maxPrice = 0;
  private double minPrice = 0;
  private double closePrice = 0;
  private int volume = 0;

  public ChartData()
  {
  }

  public ChartData(IExtendedData data)
  {
    price = data.getLastPrice();
    askPrice = data.getAskPrice();
    bidPrice = data.getBidPrice();
    volume = data.getVolume();
    date = data.getDate();
  }

  /**
   * Method to return the askPrice field.<br>
   *
   * @return Returns the askPrice.
   */
  public double getAskPrice()
  {
    return askPrice;
  }
  /**
   * Method to set the askPrice field.<br>
   * 
   * @param askPrice The askPrice to set.
   */
  public void setAskPrice(double askPrice)
  {
    this.askPrice = askPrice;
  }
  /**
   * Method to return the bidPrice field.<br>
   *
   * @return Returns the bidPrice.
   */
  public double getBidPrice()
  {
    return bidPrice;
  }
  /**
   * Method to set the bidPrice field.<br>
   * 
   * @param bidPrice The bidPrice to set.
   */
  public void setBidPrice(double bidPrice)
  {
    this.bidPrice = bidPrice;
  }
  /**
   * Method to return the closePrice field.<br>
   *
   * @return Returns the closePrice.
   */
  public double getClosePrice()
  {
    return closePrice;
  }
  /**
   * Method to set the closePrice field.<br>
   * 
   * @param closePrice The closePrice to set.
   */
  public void setClosePrice(double closePrice)
  {
    this.closePrice = closePrice;
  }
  /**
   * Method to return the date field.<br>
   *
   * @return Returns the date.
   */
  public Date getDate()
  {
    return date;
  }
  /**
   * Method to set the date field.<br>
   * 
   * @param date The date to set.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }
  /**
   * Method to return the maxPrice field.<br>
   *
   * @return Returns the maxPrice.
   */
  public double getMaxPrice()
  {
    return maxPrice;
  }
  /**
   * Method to set the maxPrice field.<br>
   * 
   * @param maxPrice The maxPrice to set.
   */
  public void setMaxPrice(double maxPrice)
  {
    this.maxPrice = maxPrice;
  }
  /**
   * Method to return the minPrice field.<br>
   *
   * @return Returns the minPrice.
   */
  public double getMinPrice()
  {
    return minPrice;
  }
  /**
   * Method to set the minPrice field.<br>
   * 
   * @param minPrice The minPrice to set.
   */
  public void setMinPrice(double minPrice)
  {
    this.minPrice = minPrice;
  }
  /**
   * Method to return the openPrice field.<br>
   *
   * @return Returns the openPrice.
   */
  public double getOpenPrice()
  {
    return openPrice;
  }
  /**
   * Method to set the openPrice field.<br>
   * 
   * @param openPrice The openPrice to set.
   */
  public void setOpenPrice(double openPrice)
  {
    this.openPrice = openPrice;
  }
  /**
   * Method to return the price field.<br>
   *
   * @return Returns the price.
   */
  public double getPrice()
  {
    return price;
  }
  /**
   * Method to set the price field.<br>
   * 
   * @param price The price to set.
   */
  public void setPrice(double price)
  {
    this.price = price;
  }
  /**
   * Method to return the volume field.<br>
   *
   * @return Returns the volume.
   */
  public int getVolume()
  {
    return volume;
  }
  /**
   * Method to set the volume field.<br>
   * 
   * @param volume The volume to set.
   */
  public void setVolume(int volume)
  {
    this.volume = volume;
  }
}
