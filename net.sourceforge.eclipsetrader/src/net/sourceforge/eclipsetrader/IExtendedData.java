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
 * Interface to implement an extended portfolio data.<br>
 * Extended data includes descriptions, prices, quantity, exchange time, and others.
 * 
 * @author Marco Maccaferri - 14/08&2004
 */
public interface IExtendedData extends IBasicData
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
   * Method to return the askSize field.<br>
   *
   * @return Returns the askSize.
   */
  public abstract int getAskSize();

  /**
   * Method to set the askSize field.<br>
   * 
   * @param askSize The askSize to set.
   */
  public abstract void setAskSize(int askSize);

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
   * Method to return the bidSize field.<br>
   *
   * @return Returns the bidSize.
   */
  public abstract int getBidSize();

  /**
   * Method to set the bidSize field.<br>
   * 
   * @param bidSize The bidSize to set.
   */
  public abstract void setBidSize(int bidSize);

  /**
   * Method to return the change field.<br>
   *
   * @return Returns the change.
   */
  public abstract String getChange();

  /**
   * Method to set the change field.<br>
   * 
   * @param change The change to set.
   */
  public abstract void setChange(String change);

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
   * Method to return the gain field.<br>
   *
   * @return Returns the gain.
   */
  public abstract double getGain();

  /**
   * Method to set the gain field.<br>
   * 
   * @param gain The gain to set.
   */
  public abstract void setGain(double gain);

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
   * Method to return the marketValue field.<br>
   *
   * @return Returns the marketValue.
   */
  public abstract double getMarketValue();

  /**
   * Method to set the marketValue field.<br>
   * 
   * @param marketValue The marketValue to set.
   */
  public abstract void setMarketValue(double marketValue);

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
   * Method to return the paid field.<br>
   *
   * @return Returns the paid.
   */
  public abstract double getPaid();

  /**
   * Method to set the paid field.<br>
   * 
   * @param paid The paid to set.
   */
  public abstract void setPaid(double paid);

  /**
   * Method to return the quantity field.<br>
   *
   * @return Returns the quantity.
   */
  public abstract int getQuantity();

  /**
   * Method to set the quantity field.<br>
   * 
   * @param quantity The quantity to set.
   */
  public abstract void setQuantity(int quantity);

  /**
   * Method to return the time field.<br>
   *
   * @return Returns the time.
   */
  public abstract String getTime();

  /**
   * Method to set the time field.<br>
   * 
   * @param time The time to set.
   */
  public abstract void setTime(String time);

  /**
   * Method to return the valueChange field.<br>
   *
   * @return Returns the valueChange.
   */
  public abstract double getValueChange();

  /**
   * Method to set the valueChange field.<br>
   * 
   * @param valueChange The valueChange to set.
   */
  public abstract void setValueChange(double valueChange);

  /**
   * Method to return the valuePaid field.<br>
   *
   * @return Returns the valuePaid.
   */
  public abstract double getValuePaid();

  /**
   * Method to set the valuePaid field.<br>
   * 
   * @param valuePaid The valuePaid to set.
   */
  public abstract void setValuePaid(double valuePaid);

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