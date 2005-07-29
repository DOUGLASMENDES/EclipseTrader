/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader;

import java.util.Observable;

/**
 * Implementation of the IBasicData interface.
 */
public class BasicData extends Observable implements IBasicData 
{
  public String symbol = "";
  public String ticker = "";
  public String description = "";
  public int minimumQuantity = 1;

  public BasicData()
  {
  }
  
  public BasicData(String symbol)
  {
    this.symbol = symbol;
    this.ticker = symbol;
  }
  
  public BasicData(String symbol, String ticker)
  {
    this.symbol = symbol;
    this.ticker = ticker;
    this.description = ticker;
  }
  
  public BasicData(String symbol, String ticker, String description)
  {
    this.symbol = symbol;
    this.ticker = ticker;
    this.description = description;
  }

  public BasicData(IBasicData data)
  {
    this.symbol = data.getSymbol();
    this.ticker = data.getTicker();
    this.description = data.getDescription();
  }

  public String getSymbol()
  {
    return symbol;
  }

  public void setSymbol(String symbol)
  {
    if (!this.symbol.equals(symbol))
      setChanged();
    this.symbol = symbol;
  }

  public String getTicker()
  {
    return ticker;
  }

  public void setTicker(String ticker)
  {
    if (!this.ticker.equals(ticker))
      setChanged();
    this.ticker = ticker;
  }
  
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    if (!this.description.equals(description))
      setChanged();
    this.description = description;
  }

  public int getMinimumQuantity()
  {
    return minimumQuantity;
  }

  public void setMinimumQuantity(int minimumQuantity)
  {
    if (this.minimumQuantity != minimumQuantity)
      setChanged();
    this.minimumQuantity = minimumQuantity;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof BasicData)
      return getSymbol().equals(((BasicData)obj).getSymbol());
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return getSymbol().hashCode();
  }
}
