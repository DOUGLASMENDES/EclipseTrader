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
package net.sourceforge.eclipsetrader.ui.views.news;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NewsProvider
{
  private Vector _data = new Vector();
  private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm");
  
  public NewsProvider()
  {
    load();
  }
  
  public void load()
  {
    _data.removeAllElements();
    
    File file = new File(Platform.getLocation().toFile(), "news.xml");
    if (file.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data"))
            _data.add(decodeData(n.getChildNodes()));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
  }

  /**
   * Method to store the chart data.<br>
   */
  public void store()
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "news", null);

      for (int i = 0; i < _data.size(); i++)
      {
        Element element = document.createElement("data");
        document.getDocumentElement().appendChild(element);

        NewsData item = (NewsData)_data.elementAt(i);
        Node node = document.createElement("date");
        element.appendChild(node);
        node.appendChild(document.createTextNode(df.format(item.getDate())));
        node = document.createElement("title");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getTitle()));
        node = document.createElement("source");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getSource()));
        node = document.createElement("url");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getUrl()));
      }

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), "news.xml")));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) {};
  }
  
  private NewsData decodeData(NodeList parent)
  {
    NewsData item = new NewsData();
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node n = parent.item(i);
      Node value = n.getFirstChild();
      if (value != null)
      {
        if (n.getNodeName().equalsIgnoreCase("title") == true)
          item.setTitle(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("source") == true)
          item.setSource(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("url") == true)
          item.setUrl(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("date") == true)
        {
          try {
            item.setDate(df.parse(value.getNodeValue()));
          } catch(Exception e) {};
        }
      }
    }
    return item;
  }
  
  public NewsData[] getData()
  {
    NewsData[] nd = new NewsData[_data.size()];
    _data.toArray(nd);
    return nd;
  }

  public void update(IProgressMonitor monitor)
  {
    try {
      URL url[] = {
        new URL("http://it.biz.yahoo.com/finance_top_business.html"),
        new URL("http://it.biz.yahoo.com/researchalerts.html"),
        new URL("http://it.biz.yahoo.com/attualita/ratings.html"),
        new URL("http://it.biz.yahoo.com/attualita/companyres.html"),
        new URL("http://it.biz.yahoo.com/attualita/aumenticap.html"),
        new URL("http://it.biz.yahoo.com/attualita/agenda.html"),
        new URL("http://it.biz.yahoo.com/attualita/cda.html"),
        new URL("http://it.biz.yahoo.com/attualita/comunicati.html")
      };
      
      monitor.beginTask("News Update", url.length);
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
    monitor.done();

    java.util.Collections.sort(_data, new Comparator() {
      public int compare(Object o1, Object o2) 
      {
        NewsData d1 = (NewsData)o1;
        NewsData d2 = (NewsData)o2;
        if (d1.getDate().before(d2.getDate()) == true)
          return 1;
        else if (d1.getDate().after(d2.getDate()) == true)
          return -1;
        return 0;
      }
    });
    store();
  }

  private void updateNews(URL u) 
  {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
      parseNewsPage(in);
      in.close();
    } catch (Exception ex) {};
  }

  private void parseNewsPage(BufferedReader in) 
  {
    String inputLine;
    Calendar cal = GregorianCalendar.getInstance(Locale.ITALY);
    int dd1 = cal.get(Calendar.DAY_OF_YEAR) * 24 + cal.get(Calendar.HOUR_OF_DAY);

    try {
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.startsWith("<b><font face=arial><a href=http://") == true || inputLine.startsWith("<b><font face=arial><a href=/") == true || inputLine.startsWith("<b><a href=http://") == true || inputLine.startsWith("<b><a href=/") == true || inputLine.startsWith("<a href=http://") == true || inputLine.startsWith("<a href=/") == true) 
        {
          if (inputLine.indexOf("it.yahoo.com") != -1)
            continue;
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
                NewsData nd = new NewsData(title, url, source, gc.getTime());
                _data.addElement(nd);
              }
            }
          }
        }
      }
    } catch (Exception ex) {};
  }

  private boolean isNewsPresent(String title) 
  {
    // Verifica se il titolo è già presente
    for (int i = 0; i < _data.size(); i++) {
      NewsData d = (NewsData)_data.elementAt(i);
      if (title.equalsIgnoreCase(d.title) == true)
        return true;
    }

    return false;
  }

  private String replaceHtml(String text) 
  {
    int s;

    while ((s = text.toLowerCase().indexOf("</td><td align=right valign=top>")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf("top>", s) + 4);

    // Sostituisce i terminatori di linea e paragrafo
    while ((s = text.toLowerCase().indexOf("<p")) != -1)
      text = text.substring(0, s) + "\r\n" + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("<br")) != -1)
      text = text.substring(0, s) + "\r\n" + text.substring(text.indexOf(">", s) + 1);

    // Rimuove gli elementi html che non servono
    while ((s = text.toLowerCase().indexOf("<a")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("</a")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("<table")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf("</table>", s) + 8);
    while ((s = text.toLowerCase().indexOf("</p")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("<i")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("</i")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("<b")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("</b")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("<font")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("</font")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf(">", s) + 1);
    while ((s = text.toLowerCase().indexOf("<!--")) != -1)
      text = text.substring(0, s) + text.substring(text.indexOf("-->", s) + 3);

    // Elimina una fine di tabella che non si sa come rimane all'inizio del testo
    if ((s = text.toLowerCase().indexOf("</table>")) != -1)
      text = text.substring(s + 8);

    // Sostituisce le codifiche dei caratteri accentati e speciali
    while ((s = text.toLowerCase().indexOf("&agrave;")) != -1)
      text = text.substring(0, s) + "à" + text.substring(s + 8);
    while ((s = text.toLowerCase().indexOf("&egrave;")) != -1)
      text = text.substring(0, s) + "è" + text.substring(s + 8);
    while ((s = text.toLowerCase().indexOf("&eacute;")) != -1)
      text = text.substring(0, s) + "é" + text.substring(s + 8);
    while ((s = text.toLowerCase().indexOf("&igrave;")) != -1)
      text = text.substring(0, s) + "ì" + text.substring(s + 8);
    while ((s = text.toLowerCase().indexOf("&ograve;")) != -1)
      text = text.substring(0, s) + "ò" + text.substring(s + 8);
    while ((s = text.toLowerCase().indexOf("&ugrave;")) != -1)
      text = text.substring(0, s) + "ù" + text.substring(s + 8);
    while ((s = text.toLowerCase().indexOf("&quot;")) != -1)
      text = text.substring(0, s) + "\"" + text.substring(s + 6);
    while ((s = text.toLowerCase().indexOf("&amp;")) != -1)
      text = text.substring(0, s) + "&" + text.substring(s + 5);
    while ((s = text.indexOf("&#36;")) != -1)
      text = text.substring(0, s) + "$" + text.substring(s + 5);
    while ((s = text.indexOf("&#39;")) != -1)
      text = text.substring(0, s) + "'" + text.substring(s + 5);
    while ((s = text.indexOf("&#224;")) != -1)
      text = text.substring(0, s) + "à" + text.substring(s + 6);
    while ((s = text.indexOf("&#232;")) != -1)
      text = text.substring(0, s) + "è" + text.substring(s + 6);
    while ((s = text.indexOf("&#233;")) != -1)
      text = text.substring(0, s) + "é" + text.substring(s + 6);
    while ((s = text.indexOf("&#236;")) != -1)
      text = text.substring(0, s) + "ì" + text.substring(s + 6);
    while ((s = text.indexOf("&#242;")) != -1)
      text = text.substring(0, s) + "ò" + text.substring(s + 6);
    while ((s = text.indexOf("&#249;")) != -1)
      text = text.substring(0, s) + "ù" + text.substring(s + 6);
    while ((s = text.indexOf("&lt;")) != -1)
      text = text.substring(0, s) + "<" + text.substring(s + 4);
    while ((s = text.indexOf("&gt;")) != -1)
      text = text.substring(0, s) + ">" + text.substring(s + 4);

    return text;
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
