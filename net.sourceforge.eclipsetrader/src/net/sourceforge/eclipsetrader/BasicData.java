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
 * Implementation of the IBasicData interface.
 * <p></p>
 * 
 * @author Marco Maccaferri - 10/08/2004
 */
public class BasicData implements IBasicData 
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
  
  /**
   * Method to return the description field.<br>
   *
   * @return Returns the description.
   */
  public String getDescription()
  {
    return description;
  }
  /**
   * Method to set the description field.<br>
   * 
   * @param description The description to set.
   */
  public void setDescription(String description)
  {
    this.description = description;
  }
  /**
   * Method to return the symbol field.<br>
   *
   * @return Returns the symbol.
   */
  public String getSymbol()
  {
    return symbol;
  }
  /**
   * Method to set the symbol field.<br>
   * 
   * @param symbol The symbol to set.
   */
  public void setSymbol(String symbol)
  {
    this.symbol = symbol;
  }
  /**
   * Method to return the ticker field.<br>
   *
   * @return Returns the ticker.
   */
  public String getTicker()
  {
    return ticker;
  }
  /**
   * Method to set the ticker field.<br>
   * 
   * @param ticker The ticker to set.
   */
  public void setTicker(String ticker)
  {
    this.ticker = ticker;
  }
  /**
   * Method to return the minimumQuantity field.<br>
   *
   * @return Returns the minimumQuantity.
   */
  public int getMinimumQuantity()
  {
    return minimumQuantity;
  }
  /**
   * Method to set the minimumQuantity field.<br>
   * 
   * @param minimumQuantity The minimumQuantity to set.
   */
  public void setMinimumQuantity(int minimumQuantity)
  {
    this.minimumQuantity = minimumQuantity;
  }
}
