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

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IAlertData;
import net.sourceforge.eclipsetrader.IAlertListener;
import net.sourceforge.eclipsetrader.IAlertSource;
import net.sourceforge.eclipsetrader.IExtendedData;

/**
 * Implementation of the IExtendedData interface.
 * <p></p>
 */
public class ExtendedData extends BasicData implements IExtendedData, IAlertSource 
{
  private double lastPrice = 0;
  private String change = "0.00%";
  private double bidPrice = 0;
  private int bidSize = 0;
  private double askPrice = 0;
  private int askSize = 0;
  private int volume = 0;
  private double marketValue = 0;
  private String time = "";
  private double valuePaid = 0;
  private int quantity = 0;
  private double paid = 0;
  private double gain = 0;
  private double openPrice = 0;
  private double closePrice = 0;
  private double lowPrice = 0;
  private double highPrice = 0;
  private double valueChange = 0;
  private Date date = Calendar.getInstance().getTime();
  private Vector alerts = new Vector();
  private Vector alertsListeners = new Vector();

  /**
   * Constructs a new instance of this class.
   */
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
    double oldValue = this.askPrice;
    this.askPrice = askPrice;
    verifyAlerts(IAlertData.ASK_PRICE, askPrice, oldValue);
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
    this.askSize = askSize;
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
    double oldValue = this.bidPrice;
    this.bidPrice = bidPrice;
    verifyAlerts(IAlertData.BID_PRICE, bidPrice, oldValue);
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
    this.bidSize = bidSize;
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
    double oldValue = this.lastPrice;
    this.lastPrice = lastPrice;
    verifyAlerts(IAlertData.LAST_PRICE, lastPrice, oldValue);
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
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertSource#addAlert(IAlertData)
   */
  public void addAlert(IAlertData alertData)
  {
    alerts.add(alertData);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertSource#removeAlert(IAlertData)
   */
  public void removeAlert(IAlertData alertData)
  {
    alerts.remove(alertData);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertSource#removeAllAlerts()
   */
  public void removeAllAlerts()
  {
    alerts.removeAllElements();
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertSource#removeAlertListener(net.sourceforge.eclipsetrader.IAlertListener)
   */
  public IAlertData[] getAlerts()
  {
    IAlertData array[] = new IAlertData[alerts.size()];
    alerts.toArray(array);
    return array;
  }

  /**
   * Verify if the conditions of the configured alerts are met.
   * When a condition is verified the alert data is set to the acknowledged state
   * and a notification is sent to the listeners.
   * 
   * @param item the item to check
   * @param currentValue the current price value
   * @param oldValue the previous price value
   */
  private void verifyAlerts(int item, double currentValue, double oldValue)
  {
    for (int i = 0; i < alerts.size(); i++)
    {
      IAlertData alertData = (IAlertData)alerts.elementAt(i);
      if (alertData.isTrigger() == true || alertData.getItem() != item)
        continue;

      switch(alertData.getCondition())
      {
        case IAlertData.FALLS_BELOW:
          if (currentValue < oldValue && currentValue < alertData.getPrice())
            fireAlertTrigger(alertData);
          break;
        case IAlertData.RAISE_ABOVE:
          if (currentValue > oldValue && currentValue > alertData.getPrice())
            fireAlertTrigger(alertData);
          break;
      }
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertSource#addAlertListener(net.sourceforge.eclipsetrader.IAlertListener)
   */
  public void addAlertListener(IAlertListener listener)
  {
    alertsListeners.add(listener);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertSource#removeAlertListener(net.sourceforge.eclipsetrader.IAlertListener)
   */
  public void removeAlertListener(IAlertListener listener)
  {
    alertsListeners.remove(listener);
  }
  
  /**
   * Notify alerts listeners of an acknolwedged alert.
   * 
   * @param alertData the originating alert
   */
  private void fireAlertTrigger(IAlertData alertData)
  {
    alertData.setTrigger(true);
    
    if (alertData.isPlaySound() == true)
    {
      try {
        File file = new File(alertData.getSoundFile());
        AudioClip clip = Applet.newAudioClip(file.toURL());
        clip.play();
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    
    for (int i = 0; i < alertsListeners.size(); i++)
      ((IAlertListener)alertsListeners.elementAt(i)).alertVerified(this, alertData);
  }
}
