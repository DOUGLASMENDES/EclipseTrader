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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import net.sourceforge.eclipsetrader.NewsData;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class NewsSource implements INewsSource
{
  protected Vector _data = new Vector();

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.yahoo.INewsSource#update(org.eclipse.core.runtime.IProgressMonitor)
   */
  public Vector update(IProgressMonitor monitor)
  {
    return _data;
  }

  protected void updateNews(URL u) 
  {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
      parseNewsPage(in);
      in.close();
    } catch (Exception ex) {};
  }

  protected void parseNewsPage(BufferedReader in)
  {
  }

  protected boolean isNewsPresent(String title) 
  {
    // Verifica se il titolo è già presente
    for (int i = 0; i < _data.size(); i++) {
      NewsData d = (NewsData)_data.elementAt(i);
      if (title.equalsIgnoreCase(d.title) == true)
        return true;
    }

    return false;
  }

  protected String replaceHtml(String text) 
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
}
