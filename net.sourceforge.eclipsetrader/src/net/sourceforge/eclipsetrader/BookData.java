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
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BookData implements IBookData
{
  private int number = 0;
  private int quantity = 0;
  private double price = 0;
  private int numberVariance = 0;
  private int quantityVariance = 0;
  private double priceVariance = 0;
  private long numberTimestamp = 0;
  private long quantityTimestamp = 0;
  private long priceTimestamp = 0;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookData#getNumber()
   */
  public int getNumber()
  {
    return number;
  }
  public int getNumberVariance()
  {
    if ((System.currentTimeMillis() - numberTimestamp) >= 15000)
      numberVariance = 0;
    return numberVariance;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookData#setNumber(int)
   */
  public void setNumber(int number)
  {
    if (this.number != 0 && number != 0 && (this.number != number || (System.currentTimeMillis() - numberTimestamp) >= 15000))
    {
      this.numberVariance = number - this.number;
      this.numberTimestamp = System.currentTimeMillis();
    }
    this.number = number;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookData#getQuantity()
   */
  public int getQuantity()
  {
    return quantity;
  }
  public int getQuantityVariance()
  {
    if ((System.currentTimeMillis() - quantityTimestamp) >= 15000)
      quantityVariance = 0;
    return quantityVariance;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookData#setQuantity(int)
   */
  public void setQuantity(int quantity)
  {
    if (this.quantity != 0 && quantity != 0 && (this.quantity != quantity || (System.currentTimeMillis() - quantityTimestamp) >= 15000))
    {
      this.quantityVariance = quantity - this.quantity;
      this.quantityTimestamp = System.currentTimeMillis();
    }
    this.quantity = quantity;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookData#getPrice()
   */
  public double getPrice()
  {
    return price;
  }
  public double getPriceVariance()
  {
    if ((System.currentTimeMillis() - priceTimestamp) >= 15000)
      priceVariance = 0;
    return priceVariance;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookData#setPrice(double)
   */
  public void setPrice(double price)
  {
    if (this.price != 0 && price != 0 && (this.price != price || (System.currentTimeMillis() - priceTimestamp) >= 15000))
    {
      this.priceVariance = price - this.price;
      this.priceTimestamp = System.currentTimeMillis();
    }
    this.price = price;
  }

}
