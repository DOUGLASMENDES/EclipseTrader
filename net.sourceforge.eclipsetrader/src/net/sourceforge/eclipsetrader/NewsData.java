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
 * Default implementation of the INewsData interface.
 * <p></p>
 */
public class NewsData implements INewsData 
{
  public boolean readed = false;
  public Date date = new Date();
  public String title = "";
  public String source = "";
  public String url = "";

  public NewsData() 
  {
  }

  public NewsData(String title, String url, String source, Date date) 
  {
    this.title = title;
    this.url = url;
    this.source = source;
    this.date = date;
  }

  /**
   * Method to return the readed field.<br>
   *
   * @return Returns the readed.
   */
  public boolean isReaded()
  {
    return readed;
  }
  /**
   * Method to set the readed field.<br>
   * 
   * @param readed The readed to set.
   */
  public void setReaded(boolean readed)
  {
    this.readed = readed;
  }
  /**
   * Method to return the source field.<br>
   *
   * @return Returns the source.
   */
  public String getSource()
  {
    return source;
  }
  /**
   * Method to set the source field.<br>
   * 
   * @param source The source to set.
   */
  public void setSource(String source)
  {
    this.source = source;
  }
  /**
   * Method to return the title field.<br>
   *
   * @return Returns the title.
   */
  public String getTitle()
  {
    return title;
  }
  /**
   * Method to set the title field.<br>
   * 
   * @param title The title to set.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }
  /**
   * Method to return the url field.<br>
   *
   * @return Returns the url.
   */
  public String getUrl()
  {
    return url;
  }
  /**
   * Method to set the url field.<br>
   * 
   * @param url The url to set.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }
  /**
   * Method to set the date field.<br>
   * 
   * @param date The date to set.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }
  /**
   * Method to return the date field.<br>
   *
   * @return Returns the date.
   */
  public Date getDate() 
  {
    return date;
  }
}
