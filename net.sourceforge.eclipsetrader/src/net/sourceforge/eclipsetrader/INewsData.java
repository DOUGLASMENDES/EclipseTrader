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

import java.util.Date;

/**
 * Interface for news data items.
 * <p></p>
 */
public interface INewsData
{
  /**
   * Method to return the readed field.<br>
   *
   * @return Returns the readed.
   */
  public boolean isReaded();

  /**
   * Method to set the readed field.<br>
   * 
   * @param readed The readed to set.
   */
  public void setReaded(boolean readed);

  /**
   * Method to return the source field.<br>
   *
   * @return Returns the source.
   */
  public String getSource();

  /**
   * Method to set the source field.<br>
   * 
   * @param source The source to set.
   */
  public void setSource(String source);

  /**
   * Method to return the title field.<br>
   *
   * @return Returns the title.
   */
  public String getTitle();

  /**
   * Method to set the title field.<br>
   * 
   * @param title The title to set.
   */
  public void setTitle(String title);

  /**
   * Method to return the url field.<br>
   *
   * @return Returns the url.
   */
  public String getUrl();

  /**
   * Method to set the url field.<br>
   * 
   * @param url The url to set.
   */
  public void setUrl(String url);

  /**
   * Method to set the date field.<br>
   * 
   * @param date The date to set.
   */
  public void setDate(Date date);

  /**
   * Method to return the date field.<br>
   *
   * @return Returns the date.
   */
  public Date getDate();
}