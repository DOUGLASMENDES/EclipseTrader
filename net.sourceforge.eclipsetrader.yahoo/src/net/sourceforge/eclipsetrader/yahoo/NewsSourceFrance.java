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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.NewsData;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class NewsSourceFrance extends NewsSource
{

  public NewsSourceFrance()
  {
    try
    {
      URL url[] = {
        new URL("http://fr.biz.yahoo.com/bourse/"),
      };
      this.url = url;
    }
    catch (Exception x) {}
  }

  public Vector update(IProgressMonitor monitor)
  {
    try
    {
      _data.removeAllElements();
      for (int i = 0; i < url.length; i++)
      {
        monitor.subTask(url[i].toString());
        updateNews(url[i]);
        monitor.worked(1);
        if (monitor.isCanceled() == true)
          break;
      }
    }
    catch (Exception x) {}

    return _data;
  }

  public void parseNewsPage(BufferedReader in)
  {
    int s, e;
    String inputLine, url, title, source;
    Date date;
    Calendar cal = GregorianCalendar.getInstance(Locale.FRANCE);
    SimpleDateFormat df = new SimpleDateFormat("EEEE d MMMM yyyy,H'h'm", Locale.FRANCE);
    
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);

    try
    {
      while ((inputLine = in.readLine()) != null)
      {
        if (!inputLine.startsWith("<b><a href=/") && !inputLine.startsWith("<li><a href=/"))
          continue;

        try {
          while (inputLine.indexOf("</a>") == -1)
            inputLine += in.readLine();

          s = inputLine.indexOf("href=/");
          s += 6;
          e = inputLine.indexOf(">", s);
          url = inputLine.substring(s, e);
          if (!url.startsWith("http"))
            url = "http://fr.biz.yahoo.com" + url;

          s = e + 1;
          e = inputLine.indexOf("</a>", s);
          title = inputLine.substring(s, e);
          title = replaceHtml(title);

          if ((inputLine = in.readLine()) == null)
            break;
          if (inputLine.startsWith("<font"))
          {
            if ((inputLine = in.readLine()) == null)
              break;
          }
          source = "";
          if ((s = inputLine.indexOf("(")) != -1)
          {
            s++;
            if ((e = inputLine.indexOf(")", s)) == -1)
              inputLine += in.readLine();
            if ((e = inputLine.indexOf(")", s)) != -1)
              source = inputLine.substring(s, e);
          }

          if ((inputLine = in.readLine()) == null)
            break;
          date = cal.getTime();
          try {
            if ((s = inputLine.indexOf(">")) != -1)
            {
              s++;
              if ((e = inputLine.indexOf("<", s)) != -1)
              {
                inputLine = inputLine.replaceAll("aout", "août");
                date = df.parse(inputLine.substring(s, e));
              }
            }
          }
          catch (Exception ex) {
            System.out.println(ex.getMessage());
          }

          INewsData nd = new NewsData(title, url, source, date);
          _data.addElement(nd);
        }
        catch (Exception ex) {}
      }
    }
    catch (Exception ex)
    {
    }
  }
}
