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
package net.sourceforge.eclipsetrader.ui.views.trading;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookUpdateListener;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Color bar that shows the pressure of each price level in a book / level II
 * data view.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class TrendBar extends Canvas implements PaintListener, IBookUpdateListener
{
  private IBookData[] bid;
  private IBookData[] ask;
  private Color[] band = new Color[5];
  private Color indicator;
  private int bidQuantity[] = new int[5]; 
  private int askQuantity[] = new int[5]; 

  public TrendBar(Composite parent, int style)
  {
    super(parent, style);

    reloadPreferences();
    this.addPaintListener(this);
  }
  
  public void reloadPreferences()
  {
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    indicator = new Color(Display.getCurrent(), PreferenceConverter.getColor(pref, "trendbar.indicator"));
    band[0] = new Color(null, PreferenceConverter.getColor(pref, "book.level1_color"));
    band[1] = new Color(null, PreferenceConverter.getColor(pref, "book.level2_color"));
    band[2] = new Color(null, PreferenceConverter.getColor(pref, "book.level3_color"));
    band[3] = new Color(null, PreferenceConverter.getColor(pref, "book.level4_color"));
    band[4] = new Color(null, PreferenceConverter.getColor(pref, "book.level5_color"));
  }

  public void bookUpdated(IBasicData data, IBookData[] bid, IBookData[] ask)
  {
    this.bid = bid;
    this.ask = ask;
    if (this.isDisposed() == false)
      this.redraw();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    int middle = 3;
    int width = this.getClientArea().width - 3;
    int height = this.getClientArea().height;
    
    if (bid == null || ask == null)
      return;
    
    // Calculates the bid quantity
    int level = 0;
    double levelPrice = 0;
    for (int i = 0; i < bid.length; i++)
    {
      // Update the price level
      if (levelPrice == 0)
      {
        levelPrice = bid[i].getPrice();
        bidQuantity[level] = 0;
      }
      if (levelPrice != bid[i].getPrice())
      {
        level++;
        if (level >= band.length)
          break;
        levelPrice = bid[i].getPrice();
        bidQuantity[level] = 0;
      }
      bidQuantity[level] += bid[i].getQuantity();
    }
    while(level < band.length)
      bidQuantity[level++] = 0;
    
    // Calculates the ask quantity
    level = 0;
    levelPrice = 0;
    for (int i = 0; i < ask.length; i++)
    {
      // Update the price level
      if (levelPrice == 0)
      {
        levelPrice = ask[i].getPrice();
        askQuantity[level] = 0;
      }
      if (levelPrice != ask[i].getPrice())
      {
        level++;
        if (level >= band.length)
          break;
        levelPrice = ask[i].getPrice();
        askQuantity[level] = 0;
      }
      askQuantity[level] += ask[i].getQuantity();
    }
    while(level < band.length)
      askQuantity[level++] = 0;
    
    double total = 0;
    for (int i = 0; i < bidQuantity.length; i++)
      total += bidQuantity[i];
    for (int i = 0; i < askQuantity.length; i++)
      total += askQuantity[i];
    if (total == 0)
      return;

    int last = this.getClientArea().width - middle;
    int[] bidWidth = new int[5];
    for (int i = 0; i < bidQuantity.length; i++)
    {
      bidWidth[i] = (int)((width / total) * bidQuantity[i]);
      last -= bidWidth[i];
    }
    int[] askWidth = new int[5];
    for (int i = 0; i < askQuantity.length - 1; i++)
    {
      askWidth[i] = (int)((width / total) * askQuantity[i]);
      last -= askWidth[i];
    }
    askWidth[4] = last;
    
    int x = 0;
    for (int i = bidWidth.length - 1; i >= 0; i--)
    {
      e.gc.setBackground(band[i]);
      e.gc.fillRectangle(x, 0, bidWidth[i], height);
      x += bidWidth[i];
    }
    e.gc.setBackground(indicator);
    e.gc.fillRectangle(x, 0, middle, height);
    x += middle;
    for (int i = 0; i < askWidth.length && i < 5; i++)
    {
      e.gc.setBackground(band[i]);
      e.gc.fillRectangle(x, 0, askWidth[i], height);
      x += askWidth[i];
    }
  }
}
