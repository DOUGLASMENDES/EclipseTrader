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
 * Interface for the book data.
 * <p></p>
 * 
 * @author Marco Maccaferri - 17/08/2004
 */
public interface IBookData
{
  /**
   * Return the number of contracts on the book.
   */
  public int getNumber();
  public void setNumber(int number);
  public int getNumberVariance();
  
  /**
   * Return the share quantity.
   */
  public int getQuantity();
  public void setQuantity(int quantity);
  public int getQuantityVariance();
  
  /**
   * Return the price.
   */
  public double getPrice();
  public void setPrice(double price);
  public double getPriceVariance();
}
