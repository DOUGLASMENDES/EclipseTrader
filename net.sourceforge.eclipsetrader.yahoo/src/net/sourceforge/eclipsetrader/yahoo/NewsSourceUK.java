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
package net.sourceforge.eclipsetrader.yahoo;

import java.io.BufferedReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.NewsData;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Parses the UK Yahoo news pages
 */
public class NewsSourceUK extends NewsSource
{
  private SimpleDateFormat tf = new SimpleDateFormat("h:mm a", Locale.UK);
  private SimpleDateFormat df = new SimpleDateFormat("EEEE d MMMM, h:mm a", Locale.UK);
  
  public NewsSourceUK()
  {
    try {
      URL url[] = {
        new URL("http://uk.biz.yahoo.com/hp/news_v3.html"),
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
        updateNews(url[i]);
        monitor.worked(1);
        if (monitor.isCanceled() == true)
          break;
      }
    } catch(Exception x) {};
    
    return _data;
  }

  protected void parseNewsPage(BufferedReader in) 
  {
    String inputLine;
    Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/London"), Locale.UK);

    try {
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf("<a href=\"/") != -1) 
        {
          // First row with url and title
          int s = inputLine.indexOf("<a href=\"/");
          s += 10;
          int e = inputLine.indexOf(">", s);
          String url = inputLine.substring(s, e);
          if (url.startsWith("/") == true)
            url = "http://de.biz.yahoo.com" + url;

          // Title
          s = e + 1;
          if ((e = inputLine.indexOf("<", s)) == -1)
            e = inputLine.length();
          String title = inputLine.substring(s, e);
          title = replaceHtml(title);
          
          String source = "";

          // Date and Time
          GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"), Locale.UK);
          tf.setTimeZone(c.getTimeZone());
          if ((inputLine = in.readLine()) == null)
            break;
          if (inputLine.indexOf("</b></font>") != -1)
          {
            if ((inputLine = in.readLine()) == null)
              break;
            if ((inputLine = in.readLine()) == null)
              break;
            if ((inputLine = in.readLine()) == null)
              break;
            if (inputLine.indexOf("</i>") == -1 && inputLine.indexOf(" - ") == -1)
              inputLine += in.readLine();
            if (inputLine.indexOf("</i>") == -1 && inputLine.indexOf(" - ") == -1)
              inputLine += in.readLine();
            e = inputLine.indexOf("<");
            source = inputLine.substring(0, e);

            s = inputLine.indexOf(" - ") + 3;
            e = inputLine.indexOf("<", s);
            c.setTime(df.parse(inputLine.substring(s, e)));
            c.set(Calendar.YEAR, cal.get(Calendar.YEAR));
          }
          else
          {
            if (inputLine.indexOf("</font>") == -1)
              inputLine = in.readLine();
            if ((inputLine = in.readLine()) == null)
              break;
            c.setTime(tf.parse(inputLine));
            c.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
            c.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            c.set(Calendar.YEAR, cal.get(Calendar.YEAR));

            // Next row is the press agency
            if ((inputLine = in.readLine()) == null)
              break;
            if ((s = inputLine.indexOf("[")) == -1) {
              if ((inputLine = in.readLine()) == null)
                break;
              s = inputLine.indexOf("]");
            }
            s += 1;
            if ((e = inputLine.indexOf("]", s)) == -1)
              inputLine += in.readLine();
            if ((e = inputLine.indexOf("]", s)) == -1)
              inputLine += in.readLine();
            e = inputLine.indexOf("]", s);
            source = inputLine.substring(s, e);
          }
          c.setTimeZone(TimeZone.getDefault());

          // Trim the strings
          while (title.startsWith(" ") == true)
            title = title.substring(1);
          while (title.endsWith(" ") == true)
            title = title.substring(0, title.length() - 1);

          // Insert the news into the list
          if (isNewsPresent(title) == false) 
          {
            INewsData nd = new NewsData(title, url, source, c.getTime());
            _data.addElement(nd);
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    };
  }
}
