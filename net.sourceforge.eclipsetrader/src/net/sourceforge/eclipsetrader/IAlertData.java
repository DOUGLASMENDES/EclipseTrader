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

import org.eclipse.swt.graphics.RGB;

/**
 * Instances of this interface are used to hold the configuration data
 * for the alerts system.
 */
public interface IAlertData
{
  public static final int LAST_PRICE = 0;
  public static final int BID_PRICE = 1;
  public static final int ASK_PRICE = 2;
  public static final int FALLS_BELOW = 0;
  public static final int RAISE_ABOVE = 1;

  /**
   * Method to return the acknowledge field.<br>
   *
   * @return Returns the acknowledge.
   */
  public boolean isAcknowledge();

  /**
   * Method to set the acknowledge field.<br>
   * 
   * @param hilight The acknowledge to set.
   */
  public void setAcknowledge(boolean acknowledge);

  /**
   * Method to return the trigger field.<br>
   *
   * @return Returns the trigger.
   */
  public boolean isTrigger();

  /**
   * Method to set the trigger field.<br>
   * 
   * @param hilight The trigger to set.
   */
  public void setTrigger(boolean trigger);

  /**
   * Method to return the condition field.<br>
   *
   * @return Returns the condition.
   */
  public int getCondition();

  /**
   * Method to set the condition field.<br>
   * 
   * @param condition The condition to set.
   */
  public void setCondition(int condition);

  /**
   * Method to return the hilight field.<br>
   *
   * @return Returns the hilight.
   */
  public boolean isHilight();

  /**
   * Method to set the hilight field.<br>
   * 
   * @param hilight The hilight to set.
   */
  public void setHilight(boolean hilight);

  /**
   * Method to return the hilightColor field.<br>
   *
   * @return Returns the hilightColor.
   */
  public RGB getHilightColor();

  /**
   * Method to set the hilightColor field.<br>
   * 
   * @param hilightColor The hilightColor to set.
   */
  public void setHilightColor(RGB hilightColor);

  /**
   * Method to return the item field.<br>
   *
   * @return Returns the item.
   */
  public int getItem();

  /**
   * Method to set the item field.<br>
   * 
   * @param item The item to set.
   */
  public void setItem(int item);

  /**
   * Method to return the playSound field.<br>
   *
   * @return Returns the playSound.
   */
  public boolean isPlaySound();

  /**
   * Method to set the playSound field.<br>
   * 
   * @param playSound The playSound to set.
   */
  public void setPlaySound(boolean playSound);

  /**
   * Method to return the price field.<br>
   *
   * @return Returns the price.
   */
  public double getPrice();

  /**
   * Method to set the price field.<br>
   * 
   * @param price The price to set.
   */
  public void setPrice(double price);

  /**
   * Method to return the soundFile field.<br>
   *
   * @return Returns the soundFile.
   */
  public String getSoundFile();

  /**
   * Method to set the soundFile field.<br>
   * 
   * @param soundFile The soundFile to set.
   */
  public void setSoundFile(String soundFile);
}