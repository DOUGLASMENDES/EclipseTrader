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

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TrendBar extends Canvas implements PaintListener, IBookUpdateListener
{
  private IBookData[] bid;
  private IBookData[] ask;
  private Color[] band = new Color[5];
  private Color indicator;

  public TrendBar(Composite parent, int style)
  {
    super(parent, style);

    indicator = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "trendbar.indicator"));
    band[0] = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "trendbar.band1_color"));
    band[1] = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "trendbar.band2_color"));
    band[2] = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "trendbar.band3_color"));
    band[3] = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "trendbar.band4_color"));
    band[4] = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "trendbar.band5_color"));
    
    this.addPaintListener(this);
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
    
    double total = 0;
    for (int i = 0; i < bid.length && i < 5; i++)
      total += bid[i].getQuantity();
    for (int i = 0; i < ask.length && i < 5; i++)
      total += ask[i].getQuantity();
    if (total == 0)
      return;

    int last = this.getClientArea().width - middle;
    int[] bidWidth = new int[5];
    for (int i = 0; i < bid.length && i < 5; i++)
    {
      bidWidth[i] = (int)((width / total) * bid[i].getQuantity());
      last -= bidWidth[i];
    }
    int[] askWidth = new int[5];
    for (int i = 0; i < ask.length && i < 4; i++)
    {
      askWidth[i] = (int)((width / total) * ask[i].getQuantity());
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
