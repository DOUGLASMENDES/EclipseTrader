/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class DateScale extends Canvas implements PaintListener
{
  private int interval = BarData.INTERVAL_DAILY;
  private BarData barData = new BarData();
  private List dateList = new ArrayList();
  private int width = 5;
  private int margin = 2;
  private Color foregroundColor = new Color(null, 0, 0, 0);
  private Color hilightColor = new Color(null, 255, 0, 0);
  private Image image;
  
  private class TickItem
  {
    public boolean flag = false;
    public boolean tick = false;
    public String text = "";
  }
  
  public DateScale(Composite parent, int style)
  {
    super(parent, style);
    addPaintListener(this);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  public void dispose()
  {
    removePaintListener(this);
    foregroundColor.dispose();
    hilightColor.dispose();
    if (image != null)
      image.dispose();
    super.dispose();
  }

  public void setBarData(BarData barData)
  {
    this.barData = barData;
    interval = barData.getCompression();

    dateList.clear();
    if (barData.size() > 0)
    {
      switch (interval)
      {
        case BarData.INTERVAL_MINUTE1:
        case BarData.INTERVAL_MINUTE2:
        case BarData.INTERVAL_MINUTE5:
        case BarData.INTERVAL_MINUTE10:
        case BarData.INTERVAL_MINUTE15:
        case BarData.INTERVAL_MINUTE30:
        case BarData.INTERVAL_MINUTE60:
          getMinuteDate();
          break;
        case BarData.INTERVAL_WEEKLY:
//          getWeeklyDate();
          break;
        case BarData.INTERVAL_MONTHLY:
//          getMonthlyDate();
          break;
        default:
          getDailyDate();
          break;
      }
    }

    if (barData != null)
    {
      int w = barData.size() * width + margin * 2;
      if (w > getClientArea().width)
        setSize(w, getClientArea().height);
      else
        setSize(getClientArea().width, getClientArea().height);
    }
    else
      setSize(getClientArea().width, getClientArea().height);

    if (image != null)
      image.dispose();
    image = new Image(getDisplay(), getSize().x, getSize().y);
  }

  private void getDailyDate()
  {
    SimpleDateFormat monthYearFormatter = new SimpleDateFormat("MMM, yyyy"); //$NON-NLS-1$

    int loop = 0;
    Calendar oldDate = Calendar.getInstance();
    oldDate.setTime(barData.get(loop).getDate());

    while(loop < barData.size())
    {
      TickItem item = new TickItem();
    
      Calendar date = Calendar.getInstance();
      date.setTime(barData.get(loop).getDate());

      if (date.get(Calendar.MONTH) != oldDate.get(Calendar.MONTH))
      {
        item.tick = true;
        if (date.get(Calendar.YEAR) != oldDate.get(Calendar.YEAR))
          item.flag = true;
        item.text = monthYearFormatter.format(date.getTime());
        oldDate = date;
      }

      dateList.add(item);
      loop++;
    }
  }

  private void getMinuteDate()
  {
    SimpleDateFormat monthDayFormatter = new SimpleDateFormat("MMM d"); //$NON-NLS-1$

    int loop = 0;
    Calendar nextHour = Calendar.getInstance();
    nextHour.setTime(barData.get(loop).getDate());
    nextHour.set(Calendar.MINUTE, 0);
    nextHour.set(Calendar.SECOND, 0);
    Calendar oldDay = Calendar.getInstance();
    oldDay.setTime(barData.get(loop).getDate());
    
    if (interval != BarData.INTERVAL_MINUTE1 && interval != BarData.INTERVAL_MINUTE2)
      nextHour.add(Calendar.SECOND, 7200);
    else
      nextHour.add(Calendar.SECOND, 3600);

    while(loop < barData.size())
    {
      TickItem item = new TickItem();
    
      Calendar date = Calendar.getInstance();
      date.setTime(barData.get(loop).getDate());

      if (date.get(Calendar.DATE) != oldDay.get(Calendar.DATE))
      {
        item.tick = true;
        item.flag = true;
        item.text = monthDayFormatter.format(date.getTime());
        oldDay = date;
      }
      else
      {
        if (date.after(nextHour) || date.equals(nextHour))
        {
          if (interval < BarData.INTERVAL_MINUTE30)
          {
            item.tick = true;
            item.flag = false;
            item.text = date.get(Calendar.HOUR_OF_DAY) + ":00";
          }
        }
      }

      if (date.after(nextHour) || date.equals(nextHour))
      {
        nextHour = (Calendar)date.clone();
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);
        if (interval != BarData.INTERVAL_MINUTE1 && interval != BarData.INTERVAL_MINUTE2)
          nextHour.add(Calendar.SECOND, 7200);
        else
          nextHour.add(Calendar.SECOND, 3600);
      }

      dateList.add(item);
      loop++;
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#redraw()
   */
  public void redraw()
  {
    if (image != null)
    {
      GC gc = new GC(image);

      gc.setBackground(getBackground());
      gc.fillRectangle(0, 0, getSize().x, getSize().y);
      
      gc.drawLine(0, 0, getSize().x, 0);
      
      int x = margin + width / 2;
      for (int i = 0; i < dateList.size(); i++, x += width)
      {
        TickItem item = (TickItem)dateList.get(i);
        if (item.tick)
        {
          if (item.flag)
            gc.setForeground(hilightColor);
          else
            gc.setForeground(foregroundColor);
          gc.drawLine (x, 1, x, 4);
          gc.drawString(item.text, x - 1, 5);
        }
      }

      gc.dispose();
    }

    super.redraw();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    if (image != null)
      e.gc.drawImage(image, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
  }
}
