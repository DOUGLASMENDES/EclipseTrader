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
 * Interface for basic portfolio / stockwatch data.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public interface IPortfolioData extends IBasicData
{

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
   * Method to return the value field.<br>
   *
   * @return Returns the value.
   */
  public abstract double getValue();

  /**
   * Method to set the value field.<br>
   * 
   * @param paid The value to set.
   */
  public abstract void setValue(double value);

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
}
