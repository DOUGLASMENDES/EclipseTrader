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
package net.sourceforge.eclipsetrader.yahoo;

import java.io.BufferedReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.NewsData;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class NewsSourceUS extends NewsSource
{
  private SimpleDateFormat df = new SimpleDateFormat("EEEE MMMM d, yyyy h:mm a", Locale.US);
  
  public NewsSourceUS()
  {
    try {
      URL url[] = {
        new URL("http://biz.yahoo.com/apf/archive.html"),
        new URL("http://biz.yahoo.com/reuters/archive.html"),
      };
      this.url = url;
    } catch(Exception x) {};
  }

  public Vector update(IProgressMonitor monitor)
  {
    try {
      _data.removeAllElements();
      for (int i = 0; i < url.length; i++)
      {
        monitor.subTask(url[i].toString());
        monitor.worked(1);
        updateNews(url[i]);
        if (monitor.isCanceled() == true)
          break;
      }
    } catch(Exception x) {};

    return _data;
  }

  protected void parseNewsPage(BufferedReader in) 
  {
    String inputLine;
    String day = "";

    try {
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf("<span class=date>") != -1)
        {
          int s = inputLine.indexOf("<span class=date>") + 17;
          int e = inputLine.indexOf("<", s);
          day = inputLine.substring(s, e);
        }
        if (inputLine.indexOf("class=headline") != -1) 
        {
          // La prima riga contiene il titolo e l'url della notizia
          int s = inputLine.indexOf("href=\"") + 6;
          int e = inputLine.indexOf("\"", s);
          String url = inputLine.substring(s, e);

          s = e + 2;
          if ((e = inputLine.indexOf("<", s)) == -1)
            e = inputLine.length();
          String title = inputLine.substring(s, e);

          s = inputLine.indexOf("attrib>", s) + 7;
          e = inputLine.indexOf("<", s);
          String timeSource = inputLine.substring(s, e);
          String[] ar = timeSource.split(" - ");
          String source = ar[1];
          
          if (isNewsPresent(title) == false) 
          {
            INewsData nd = new NewsData(title, url, source, df.parse(day + " " + ar[0]));
            _data.addElement(nd);
          }
        }
      }
    } catch (Exception ex) { ex.printStackTrace(); };
  }
}
