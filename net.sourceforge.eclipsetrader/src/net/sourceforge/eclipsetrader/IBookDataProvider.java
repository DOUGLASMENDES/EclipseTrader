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
 * Interface for implementing a book data provider.
 * <p>Book data providers are capable of retriving the complete price book of a
 * stock exchange item.</p>
 * 
 * @see net.sourceforge.eclipsetrader.IBasicDataProvider
 * 
 * @author Marco Maccaferri - 17/08/2004
 */
public interface IBookDataProvider
{
  /**
   * Get the bid data items.<br>
   * 
   * @param data The portfolio item.
   * @return The book data items.
   */
  public IBookData[] getBidData(IBasicData data);

  /**
   * Get the ask data items.<br>
   * 
   * @param data The portfolio item.
   * @return The book data items.
   */
  public IBookData[] getAskData(IBasicData data);

  /**
   * Start retriving the book data for the given portfolio item.<br>
   * 
   * @param data The portfolio item.
   */
  public void startBook(IBasicData data);

  /**
   * Stop retriving the book data for the given portfolio item.<br>
   * 
   * @param data The portfolio item.
   */
  public void stopBook(IBasicData data);

  /**
   * Add a data update listener for the given portfolio data.<br>
   * 
   * @param data The portfolio item.
   * @param listener The data listener.
   */
  public void addBookListener(IBasicData data, IBookUpdateListener listener);

  /**
   * Remove a data update listener for the given portfolio data.<br>
   * 
   * @param data The portfolio item.
   * @param listener The data listener.
   */
  public void removeBookListener(IBasicData data, IBookUpdateListener listener);
}
