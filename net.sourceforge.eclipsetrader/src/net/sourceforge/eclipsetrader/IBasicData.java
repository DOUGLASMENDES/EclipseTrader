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
 * Interface to implement a basic portfolio data.<br>
 * 
 * @author Marco Maccaferri - 14/08&2004
 */
public interface IBasicData
{

  /**
   * Method to return the description field.<br>
   *
   * @return Returns the description.
   */
  public abstract String getDescription();

  /**
   * Method to set the description field.<br>
   * 
   * @param description The description to set.
   */
  public abstract void setDescription(String description);

  /**
   * Method to return the symbol field.<br>
   *
   * @return Returns the symbol.
   */
  public abstract String getSymbol();

  /**
   * Method to set the symbol field.<br>
   * 
   * @param symbol The symbol to set.
   */
  public abstract void setSymbol(String symbol);

  /**
   * Method to return the ticker field.<br>
   *
   * @return Returns the ticker.
   */
  public abstract String getTicker();

  /**
   * Method to set the ticker field.<br>
   * 
   * @param ticker The ticker to set.
   */
  public abstract void setTicker(String ticker);

  /**
   * Method to return the minimumQuantity field.<br>
   *
   * @return Returns the minimumQuantity.
   */
  public abstract int getMinimumQuantity();

  /**
   * Method to set the minimumQuantity field.<br>
   * 
   * @param minimumQuantity The minimumQuantity to set.
   */
  public abstract void setMinimumQuantity(int minimumQuantity);
}
