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

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;


/**
 * Implementation of the IExtendedData interface.
 */
public class ExtendedData extends BasicData implements IExtendedData, IAlertSource 
{
  private double lastPrice = 0;
  private double bidPrice = 0;
  private int bidSize = 0;
  private double askPrice = 0;
  private int askSize = 0;
  private double openPrice = 0;
  private double highPrice = 0;
  private double lowPrice = 0;
  private double closePrice = 0;
  private double valueChange = 0;
  private int volume = 0;
  private int ownedQuantity = 0;
  private double paid = 0;
  private Date date = Calendar.getInstance().getTime();
  private Vector alerts = new Vector();
  private Vector alertsListeners = new Vector();

  public ExtendedData()
  {
  }
  
  public ExtendedData(String symbol)
  {
    super(symbol);
  }
  
  public ExtendedData(String symbol, String ticker)
  {
    super(symbol, ticker);
  }
  
  public ExtendedData(String symbol, String ticker, String description)
  {
    super(symbol, ticker, description);
  }
  
  public ExtendedData(IBasicData data)
  {
    super(data);
  }
  
  public ExtendedData(IExtendedData data)
  {
    super(data);
    setLastPrice(data.getLastPrice());
    setBidPrice(data.getBidPrice());
    setBidSize(data.getBidSize());
    setAskPrice(data.getAskPrice());
    setAskSize(data.getAskSize());
    setOpenPrice(data.getOpenPrice());
    setHighPrice(data.getHighPrice());
    setLowPrice(data.getLowPrice());
    setClosePrice(data.getClosePrice());
    setVolume(data.getVolume());
  }

  public double getLastPrice()
  {
    return lastPrice;
  }

  public void setLastPrice(double lastPrice)
  {
    double oldValue = this.lastPrice;

    if (this.lastPrice != lastPrice)
      setChanged();
    this.lastPrice = lastPrice;

    valueChange = 0;
    if (lastPrice != 0 && closePrice != 0)
      valueChange = (lastPrice - closePrice) / closePrice * 100;
    
    verifyAlerts(IAlertData.LAST_PRICE, lastPrice, oldValue);
  }

  public double getBidPrice()
  {
    return bidPrice;
  }

  public void setBidPrice(double bidPrice)
  {
    double oldValue = this.bidPrice;
    if (this.bidPrice != bidPrice)
      setChanged();
    this.bidPrice = bidPrice;
    verifyAlerts(IAlertData.BID_PRICE, bidPrice, oldValue);
  }

  public int getBidSize()
  {
    return bidSize;
  }

  public void setBidSize(int bidSize)
  {
    if (this.bidSize != bidSize)
      setChanged();
    this.bidSize = bidSize;
  }

  public double getAskPrice()
  {
    return askPrice;
  }

  public void setAskPrice(double askPrice)
  {
    double oldValue = this.askPrice;
    if (this.askPrice != askPrice)
      setChanged();
    this.askPrice = askPrice;
    verifyAlerts(IAlertData.ASK_PRICE, askPrice, oldValue);
  }

  public int getAskSize()
  {
    return askSize;
  }

  public void setAskSize(int askSize)
  {
    if (this.askSize != askSize)
      setChanged();
    this.askSize = askSize;
  }

  public double getOpenPrice()
  {
    return openPrice;
  }

  public void setOpenPrice(double openPrice)
  {
    if (this.openPrice != openPrice)
      setChanged();
    this.openPrice = openPrice;
  }

  public double getHighPrice()
  {
    return highPrice;
  }

  public void setHighPrice(double highPrice)
  {
    if (this.highPrice != highPrice)
      setChanged();
    this.highPrice = highPrice;
  }

  public double getLowPrice()
  {
    return lowPrice;
  }

  public void setLowPrice(double lowPrice)
  {
    if (this.lowPrice != lowPrice)
      setChanged();
    this.lowPrice = lowPrice;
  }

  public double getClosePrice()
  {
    return closePrice;
  }

  public void setClosePrice(double closePrice)
  {
    if (this.closePrice != closePrice)
      setChanged();
    this.closePrice = closePrice;

    valueChange = 0;
    if (lastPrice != 0 && closePrice != 0)
      valueChange = (lastPrice - closePrice) / closePrice * 100;
  }

  public double getChange()
  {
    return valueChange;
  }

  public int getVolume()
  {
    return volume;
  }

  public void setVolume(int volume)
  {
    if (this.volume != volume)
      setChanged();
    this.volume = volume;
  }

  public double getPaid()
  {
    return paid;
  }

  public void setPaid(double paid)
  {
    if (this.paid != paid)
      setChanged();
    this.paid = paid;
  }

  public int getOwnedQuantity()
  {
    return ownedQuantity;
  }

  public void setOwnedQuantity(int ownedQuantity)
  {
    if (this.ownedQuantity != ownedQuantity)
      setChanged();
    this.ownedQuantity = ownedQuantity;
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    if (!this.date.equals(date))
      setChanged();
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
