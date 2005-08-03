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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.NewsData;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class NewsSourceItaly extends NewsSource
{
  
  public NewsSourceItaly()
  {
    try {
      URL url[] = {
        new URL("http://it.biz.yahoo.com/finance_top_business.html"),
        new URL("http://it.biz.yahoo.com/attualita/mib30/index.html"),
        new URL("http://it.biz.yahoo.com/attualita/nasdaq.html"),
        new URL("http://it.biz.yahoo.com/francoforte.html"),
        new URL("http://it.biz.yahoo.com/londra.html"),
        new URL("http://it.biz.yahoo.com/parigi.html"),
        new URL("http://it.biz.yahoo.com/attualita/giappone.html"),
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

  public void parseNewsPage(BufferedReader in) 
  {
    String inputLine;
    Calendar cal = GregorianCalendar.getInstance(Locale.ITALY);
    int dd1 = cal.get(Calendar.DAY_OF_YEAR) * 24 + cal.get(Calendar.HOUR_OF_DAY);

    try {
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.startsWith("<b><font face=arial><a href=/") || inputLine.startsWith("<a href=/")) 
          try {
            // La prima riga contiene il titolo e l'url della notizia
            int s = inputLine.indexOf("href=");
            s += 5;
            int e = inputLine.indexOf(">", s);
            String url = inputLine.substring(s, e);
            if (url.startsWith("/") == true)
              url = "http://it.biz.yahoo.com" + url;

            s = e + 1;
            if ((e = inputLine.indexOf("<", s)) == -1)
              e = inputLine.length();
            String title = inputLine.substring(s, e);
            title = replaceHtml(title);

            // La seconda riga contiene l'agenzia stampa
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

            // La terza riga contiene la data e l'ora
            if ((inputLine = in.readLine()) == null)
              break;
            s = inputLine.indexOf("ll>") + 3;
            e = inputLine.indexOf("<", s);
            String date = inputLine.substring(s, e);
            Calendar gc = GregorianCalendar.getInstance(Locale.ITALY);
            StringTokenizer st = new StringTokenizer(date, " ,:");
            st.nextToken();
            Integer vint = new Integer(st.nextToken());
            gc.set(Calendar.DAY_OF_MONTH, vint.intValue());
            gc.set(Calendar.MONTH, getMonth(st.nextToken()) - 1);
            vint = new Integer(st.nextToken());
            gc.set(Calendar.YEAR, vint.intValue());
            vint = new Integer(st.nextToken());
            gc.set(Calendar.HOUR_OF_DAY, vint.intValue());
            vint = new Integer(st.nextToken());
            gc.set(Calendar.MINUTE, vint.intValue());
            gc.set(Calendar.SECOND, 0);

            // Toglie gli spazi in testa e in coda
            while (title.startsWith(" ") == true)
              title = title.substring(1);
            while (title.endsWith(" ") == true)
              title = title.substring(0, title.length() - 1);
            // Inserisce la notizia nell'elenco
            if (isNewsPresent(title) == false) {
              if (gc.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                int dd2 = gc.get(Calendar.DAY_OF_YEAR) * 24 + gc.get(Calendar.HOUR_OF_DAY);
                if ((dd1 - dd2) <= 24)
                {
                  INewsData nd = new NewsData(title, url, source, gc.getTime());
                  _data.addElement(nd);
                }
              }
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          };
      }
    } catch (Exception ex) {};
  }

  private int getMonth(String t) 
  {
    if (t.equalsIgnoreCase("Gennaio") == true)
      return 1;
    if (t.equalsIgnoreCase("Febbraio") == true)
      return 2;
    if (t.equalsIgnoreCase("Marzo") == true)
      return 3;
    if (t.equalsIgnoreCase("Aprile") == true)
      return 4;
    if (t.equalsIgnoreCase("Maggio") == true)
      return 5;
    if (t.equalsIgnoreCase("Giugno") == true)
      return 6;
    if (t.equalsIgnoreCase("Luglio") == true)
      return 7;
    if (t.equalsIgnoreCase("Agosto") == true)
      return 8;
    if (t.equalsIgnoreCase("Settembre") == true)
      return 9;
    if (t.equalsIgnoreCase("Ottobre") == true)
      return 10;
    if (t.equalsIgnoreCase("Novembre") == true)
      return 11;
    if (t.equalsIgnoreCase("Dicembre") == true)
      return 12;

    return 0;
  }
}
