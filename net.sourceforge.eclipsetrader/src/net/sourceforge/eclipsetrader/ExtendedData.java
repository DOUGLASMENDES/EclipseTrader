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

import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of the IExtendedData interface.
 * <p></p>
 * 
 * @author Marco Maccaferri - 10/08/2004
 */
public class ExtendedData extends BasicData implements IExtendedData 
{
  private double lastPrice = 0;
  private double lastPriceVariance = 0;
  private long lastPriceTimestamp = 0;
  private String change = "0.00%";
  private double bidPrice = 0;
  private double bidPriceVariance = 0;
  private long bidPriceTimestamp = 0;
  public int bidSize = 0;
  public double bidSizeVariance = 0;
  private long bidSizeTimestamp = 0;
  public double askPrice = 0;
  public double askPriceVariance = 0;
  private long askPriceTimestamp = 0;
  public int askSize = 0;
  public double askSizeVariance = 0;
  private long askSizeTimestamp = 0;
  public int volume = 0;
  public double marketValue = 0;
  public String time = "";
  public double valuePaid = 0;
  public int quantity = 0;
  public double paid = 0;
  public double gain = 0;
  public double openPrice = 0;
  public double closePrice = 0;
  public double lowPrice = 0;
  public double highPrice = 0;
  public double valueChange = 0;
  private Date date = Calendar.getInstance().getTime();

  public ExtendedData()
  {
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
    if (askPrice != 0 && this.askPrice != 0 && (this.askPrice != askPrice || (System.currentTimeMillis() - askPriceTimestamp) >= 15000))
    {
      this.askPriceVariance = askPrice - this.askPrice;
      this.askPriceTimestamp = System.currentTimeMillis();
    }
    this.askPrice = askPrice;
  }
  /**
   * Method to return the askPriceVariance field.<br>
   *
   * @return Returns the askPriceVariance.
   */
  public double getAskPriceVariance()
  {
    if ((System.currentTimeMillis() - askPriceTimestamp) >= 15000)
      askPriceVariance = 0;
    return askPriceVariance;
  }
  /**
   * Method to return the askSize field.<br>
   *
   * @return Returns the askSize.
   */
  public int getAskSize()
  {
    return askSize;
  }
  /**
   * Method to set the askSize field.<br>
   * 
   * @param askSize The askSize to set.
   */
  public void setAskSize(int askSize)
  {
    if (askSize != 0 && this.askSize != 0 && (this.askSize != askSize || (System.currentTimeMillis() - askSizeTimestamp) >= 15000))
    {
      this.askSizeVariance = askSize - this.askSize;
      this.askSizeTimestamp = System.currentTimeMillis();
    }
    this.askSize = askSize;
  }
  /**
   * Method to return the askSizeVariance field.<br>
   *
   * @return Returns the askSizeVariance.
   */
  public double getAskSizeVariance()
  {
    if ((System.currentTimeMillis() - askSizeTimestamp) >= 15000)
      askSizeVariance = 0;
    return askSizeVariance;
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
    if (bidPrice != 0 && this.bidPrice != 0 && (this.bidPrice != bidPrice || (System.currentTimeMillis() - bidPriceTimestamp) >= 15000))
    {
      this.bidPriceVariance = bidPrice - this.bidPrice;
      this.bidPriceTimestamp = System.currentTimeMillis();
    }
    this.bidPrice = bidPrice;
  }
  /**
   * Method to return the bidPriceVariance field.<br>
   *
   * @return Returns the bidPriceVariance.
   */
  public double getBidPriceVariance()
  {
    if ((System.currentTimeMillis() - bidPriceTimestamp) >= 15000)
      bidPriceVariance = 0;
    return bidPriceVariance;
  }
  /**
   * Method to return the bidSize field.<br>
   *
   * @return Returns the bidSize.
   */
  public int getBidSize()
  {
    return bidSize;
  }
  /**
   * Method to set the bidSize field.<br>
   * 
   * @param bidSize The bidSize to set.
   */
  public void setBidSize(int bidSize)
  {
    if (bidSize != 0 && this.bidSize != 0 && (this.bidSize != bidSize || (System.currentTimeMillis() - bidSizeTimestamp) >= 15000))
    {
      this.bidSizeVariance = bidSize - this.bidSize;
      this.bidSizeTimestamp = System.currentTimeMillis();
    }
    this.bidSize = bidSize;
  }
  /**
   * Method to return the bidSizeVariance field.<br>
   *
   * @return Returns the bidSizeVariance.
   */
  public double getBidSizeVariance()
  {
    if ((System.currentTimeMillis() - bidSizeTimestamp) >= 15000)
      bidSizeVariance = 0;
    return bidSizeVariance;
  }
  /**
   * Method to return the change field.<br>
   *
   * @return Returns the change.
   */
  public String getChange()
  {
    return change;
  }
  /**
   * Method to set the change field.<br>
   * 
   * @param change The change to set.
   */
  public void setChange(String change)
  {
    this.change = change;
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
   * Method to return the gain field.<br>
   *
   * @return Returns the gain.
   */
  public double getGain()
  {
    return gain;
  }
  /**
   * Method to set the gain field.<br>
   * 
   * @param gain The gain to set.
   */
  public void setGain(double gain)
  {
    this.gain = gain;
  }
  /**
   * Method to return the highPrice field.<br>
   *
   * @return Returns the highPrice.
   */
  public double getHighPrice()
  {
    return highPrice;
  }
  /**
   * Method to set the highPrice field.<br>
   * 
   * @param highPrice The highPrice to set.
   */
  public void setHighPrice(double highPrice)
  {
    this.highPrice = highPrice;
  }
  /**
   * Method to return the lastPrice field.<br>
   *
   * @return Returns the lastPrice.
   */
  public double getLastPrice()
  {
    return lastPrice;
  }
  /**
   * Method to set the lastPrice field.<br>
   * 
   * @param lastPrice The lastPrice to set.
   */
  public void setLastPrice(double lastPrice)
  {
    if (lastPrice != 0 && this.lastPrice != 0 && (this.lastPrice != lastPrice || (System.currentTimeMillis() - lastPriceTimestamp) >= 15000))
    {
      this.lastPriceVariance = lastPrice - this.lastPrice;
      this.lastPriceTimestamp = System.currentTimeMillis();
    }
    this.lastPrice = lastPrice;
  }
  /**
   * Method to return the lastPriceVariance field.<br>
   *
   * @return Returns the lastPriceVariance.
   */
  public double getLastPriceVariance()
  {
    if ((System.currentTimeMillis() - lastPriceTimestamp) >= 15000)
      lastPriceVariance = 0;
    return lastPriceVariance;
  }
  /**
   * Method to return the lowPrice field.<br>
   *
   * @return Returns the lowPrice.
   */
  public double getLowPrice()
  {
    return lowPrice;
  }
  /**
   * Method to set the lowPrice field.<br>
   * 
   * @param lowPrice The lowPrice to set.
   */
  public void setLowPrice(double lowPrice)
  {
    this.lowPrice = lowPrice;
  }
  /**
   * Method to return the marketValue field.<br>
   *
   * @return Returns the marketValue.
   */
  public double getMarketValue()
  {
    return marketValue;
  }
  /**
   * Method to set the marketValue field.<br>
   * 
   * @param marketValue The marketValue to set.
   */
  public void setMarketValue(double marketValue)
  {
    this.marketValue = marketValue;
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
   * Method to return the paid field.<br>
   *
   * @return Returns the paid.
   */
  public double getPaid()
  {
    return paid;
  }
  /**
   * Method to set the paid field.<br>
   * 
   * @param paid The paid to set.
   */
  public void setPaid(double paid)
  {
    this.paid = paid;
  }
  /**
   * Method to return the quantity field.<br>
   *
   * @return Returns the quantity.
   */
  public int getQuantity()
  {
    return quantity;
  }
  /**
   * Method to set the quantity field.<br>
   * 
   * @param quantity The quantity to set.
   */
  public void setQuantity(int quantity)
  {
    this.quantity = quantity;
  }
  /**
   * Method to return the time field.<br>
   *
   * @return Returns the time.
   */
  public String getTime()
  {
    return time;
  }
  /**
   * Method to set the time field.<br>
   * 
   * @param time The time to set.
   */
  public void setTime(String time)
  {
    this.time = time;
  }
  /**
   * Method to return the valueChange field.<br>
   *
   * @return Returns the valueChange.
   */
  public double getValueChange()
  {
    return valueChange;
  }
  /**
   * Method to set the valueChange field.<br>
   * 
   * @param valueChange The valueChange to set.
   */
  public void setValueChange(double valueChange)
  {
    this.valueChange = valueChange;
  }
  /**
   * Method to return the valuePaid field.<br>
   *
   * @return Returns the valuePaid.
   */
  public double getValuePaid()
  {
    return valuePaid;
  }
  /**
   * Method to set the valuePaid field.<br>
   * 
   * @param valuePaid The valuePaid to set.
   */
  public void setValuePaid(double valuePaid)
  {
    this.valuePaid = valuePaid;
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
}
