/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader;

import java.text.NumberFormat;

import org.eclipse.swt.graphics.RGB;

/**
 * Instances of this class are used to hold the configuration data
 * for the alerts system.
 */
public class AlertData implements IAlertData
{
  private boolean trigger = false;
  private boolean acknowledge = false;
  private int item = LAST_PRICE;
  private int condition = FALLS_BELOW;
  private double price = 0;
  private boolean hilight = false;
  private boolean playSound = false;
  private RGB hilightColor = new RGB(255, 0, 0);
  private String soundFile = "";

  /**
   * Constructs a new instance of this class.
   */
  public AlertData()
  {
  }
  
  /**
   * Constructs a new instance of this class.
   */
  public AlertData(IAlertData data)
  {
    this.trigger = data.isTrigger();
    this.acknowledge = data.isAcknowledge();
    this.item = data.getItem();
    this.condition = data.getCondition();
    this.price = data.getPrice();
    this.hilight = data.isHilight();
    this.playSound = data.isPlaySound();
    this.hilightColor = new RGB(data.getHilightColor().red, data.getHilightColor().green, data.getHilightColor().blue);
    this.soundFile = data.getSoundFile();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#isTrigger()
   */
  public boolean isTrigger()
  {
    return trigger;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setTrigger(boolean)
   */
  public void setTrigger(boolean trigger)
  {
    this.trigger = trigger;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#isAcknowledge()
   */
  public boolean isAcknowledge()
  {
    return acknowledge;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setAcknowledge(boolean)
   */
  public void setAcknowledge(boolean acknowledge)
  {
    this.acknowledge = acknowledge;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#getCondition()
   */
  public int getCondition()
  {
    return condition;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setCondition(int)
   */
  public void setCondition(int condition)
  {
    this.condition = condition;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#isHilight()
   */
  public boolean isHilight()
  {
    return hilight;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setHilight(boolean)
   */
  public void setHilight(boolean hilight)
  {
    this.hilight = hilight;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#getHilightColor()
   */
  public RGB getHilightColor()
  {
    return hilightColor;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setHilightColor(RGB)
   */
  public void setHilightColor(RGB hilightColor)
  {
    this.hilightColor = hilightColor;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#getItem()
   */
  public int getItem()
  {
    return item;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setItem(int)
   */
  public void setItem(int item)
  {
    this.item = item;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#isPlaySound()
   */
  public boolean isPlaySound()
  {
    return playSound;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setPlaySound(boolean)
   */
  public void setPlaySound(boolean playSound)
  {
    this.playSound = playSound;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#getPrice()
   */
  public double getPrice()
  {
    return price;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setPrice(double)
   */
  public void setPrice(double price)
  {
    this.price = price;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#getSoundFile()
   */
  public String getSoundFile()
  {
    return soundFile;
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IAlertData#setSoundFile(String)
   */
  public void setSoundFile(String soundFile)
  {
    this.soundFile = soundFile;
  }
  
  public String toString()
  {
    String s = "";
    NumberFormat pf = NumberFormat.getInstance();
    
    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);

    switch(item)
    {
      case IAlertData.LAST_PRICE:
        s += "Last Price ";
        break;
      case IAlertData.BID_PRICE:
        s += "Bid Price ";
        break;
      case IAlertData.ASK_PRICE:
        s += "Ask Price ";
        break;
    }
    
    switch(condition)
    {
      case IAlertData.FALLS_BELOW:
        s += "Falls Below ";
        break;
      case IAlertData.RAISE_ABOVE:
        s += "Raise Above ";
        break;
    }
    
    s += pf.format(price);
 
    return s;
  }
}
