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
public class PortfolioData extends BasicData implements IPortfolioData
{
  private int quantity = 0;
  private double paid = 0;
  private double gain = 0;
  private double value = 0;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#getGain()
   */
  public double getGain()
  {
    return gain;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#setGain(double)
   */
  public void setGain(double gain)
  {
    this.gain = gain;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#getPaid()
   */
  public double getPaid()
  {
    return paid;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#setPaid(double)
   */
  public void setPaid(double paid)
  {
    this.paid = paid;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#getValue()
   */
  public double getValue()
  {
    return value;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#setValue(double)
   */
  public void setValue(double value)
  {
    this.value = value;
    setGain(value * quantity - paid * quantity);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#getQuantity()
   */
  public int getQuantity()
  {
    return quantity;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IPortfolioData#setQuantity(int)
   */
  public void setQuantity(int quantity)
  {
    this.quantity = quantity;
  }

}
