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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.NewsData;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Parses the Deutschland Yahoo news pages
 */
public class NewsSourceGermany extends NewsSource
{
  
  public NewsSourceGermany()
  {
    try {
      URL url[] = {
        new URL("http://de.biz.yahoo.com/179/"),
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
    Calendar cal = GregorianCalendar.getInstance(Locale.ITALY);
    int dd1 = cal.get(Calendar.DAY_OF_YEAR) * 24 + cal.get(Calendar.HOUR_OF_DAY);

    try {
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf("href=/") != -1) 
        {
          // First row with url and title
          int s = inputLine.indexOf("href=");
          s += 5;
          int e = inputLine.indexOf(">", s);
          String url = inputLine.substring(s, e);
          if (url.startsWith("/") == true)
            url = "http://de.biz.yahoo.com" + url;

          s = e + 1;
          if ((e = inputLine.indexOf("<", s)) == -1)
            e = inputLine.length();
          String title = inputLine.substring(s, e);
          title = replaceHtml(title);

          // Next row is the press agency
          if ((inputLine = in.readLine()) == null)
            break;
          if (inputLine.indexOf("(") == -1 || inputLine.indexOf(")") == -1)
            inputLine += in.readLine();
          if (inputLine.indexOf("(") == -1 || inputLine.indexOf(")") == -1)
            inputLine += in.readLine();

          if ((s = inputLine.indexOf("(")) == -1) {
            if ((inputLine = in.readLine()) == null)
              break;
            s = inputLine.indexOf("(");
          }
          s += 1;
          if ((e = inputLine.indexOf(")", s)) == -1) {
            inputLine += in.readLine();
            e = inputLine.indexOf(")", s);
          }
          String source = inputLine.substring(s, e);

          // Date and time
          if ((inputLine = in.readLine()) == null)
            break;
          Calendar gc = Calendar.getInstance();
          
          try {
            StringTokenizer st = new StringTokenizer(inputLine, " .,:");
            st.nextToken();
            Integer vint = new Integer(st.nextToken());
            gc.set(Calendar.DAY_OF_MONTH, vint.intValue());
  
            // Check if the the short date, without year, was used
            String mm = st.nextToken();
            gc.set(Calendar.MONTH, getMonth(mm) - 1);
            if (mm.length() > 3)
            {
              vint = new Integer(st.nextToken());
              gc.set(Calendar.YEAR, vint.intValue());
            }
            
            vint = new Integer(st.nextToken());
            gc.set(Calendar.HOUR_OF_DAY, vint.intValue());
            vint = new Integer(st.nextToken());
            gc.set(Calendar.MINUTE, vint.intValue());
            gc.set(Calendar.SECOND, 0);
          } catch (Exception ex) {};

          // Trim the strings
          while (title.startsWith(" ") == true)
            title = title.substring(1);
          while (title.endsWith(" ") == true)
            title = title.substring(0, title.length() - 1);

          // Insert the news into the list
          if (isNewsPresent(title) == false) 
          {
            if (gc.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) 
            {
              int dd2 = gc.get(Calendar.DAY_OF_YEAR) * 24 + gc.get(Calendar.HOUR_OF_DAY);
              if ((dd1 - dd2) <= 24)
              {
                INewsData nd = new NewsData(title, url, source, gc.getTime());
                _data.addElement(nd);
              }
            }
          }
        }
      }
    } catch (Exception ex) {};
  }

  private int getMonth(String t) 
  {
    if (t.startsWith("Jan") == true)
      return 1;
    if (t.startsWith("Feb") == true)
      return 2;
    if (t.startsWith("Mär") == true)
      return 3;
    if (t.startsWith("Apr") == true)
      return 4;
    if (t.startsWith("Mai") == true)
      return 5;
    if (t.startsWith("Jun") == true)
      return 6;
    if (t.startsWith("Jul") == true)
      return 7;
    if (t.startsWith("Aug") == true)
      return 8;
    if (t.startsWith("Sep") == true)
      return 9;
    if (t.startsWith("Okt") == true)
      return 10;
    if (t.startsWith("Nov") == true)
      return 11;
    if (t.startsWith("Dez") == true)
      return 12;

    return 0;
  }
}
