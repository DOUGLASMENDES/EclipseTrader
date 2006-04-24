/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 */
public class BarData
{
    public static final int OPEN = 0;
    public static final int HIGH = 1;
    public static final int LOW = 2;
    public static final int CLOSE = 3;
    public static final int VOLUME = 4;
    public static final int INTERVAL_MINUTE1 = 0;
    public static final int INTERVAL_MINUTE2 = 2;
    public static final int INTERVAL_MINUTE5 = 3;
    public static final int INTERVAL_MINUTE10 = 4;
    public static final int INTERVAL_MINUTE15 = 5;
    public static final int INTERVAL_MINUTE30 = 6;
    public static final int INTERVAL_MINUTE60 = 7;
    public static final int INTERVAL_DAILY = 8;
    public static final int INTERVAL_WEEKLY = 9;
    public static final int INTERVAL_MONTHLY = 10;
    private List barList = new ArrayList();
    private double max = -99999999;
    private double min = 99999999;
    private int compression = INTERVAL_DAILY;
    
    public BarData()
    {
    }
    
    public BarData(List barList)
    {
        Bar[] bar = new Bar[barList.size()];
        barList.toArray(bar);
        addAll(bar);
    }

    public boolean append(Bar obj)
    {
        if (barList.contains(obj))
            return false;
        if (obj.getHigh() == 0 || obj.getLow() == 0)
            return false;

        if (obj.getHigh() > max)
            max = obj.getHigh();
        if (obj.getLow() < min)
            min = obj.getLow();

        return barList.add(obj);
    }

    public void prepend(Bar obj)
    {
        if (barList.contains(obj))
            return;
        if (obj.getHigh() == 0 || obj.getLow() == 0)
            return;

        if (obj.getHigh() > max)
            max = obj.getHigh();
        if (obj.getLow() < min)
            min = obj.getLow();

        barList.add(0, obj);
    }

    public void addAll(Bar[] data)
    {
        for (int i = 0; i < data.length; i++)
            append(data[i]);
    }

    public boolean remove(Bar arg0)
    {
        return barList.remove(arg0);
    }

    public Bar get(int index)
    {
        return (Bar) barList.get(index);
    }

    public int size()
    {
        return barList.size();
    }

    public Iterator iterator()
    {
        return barList.iterator();
    }

    public int indexOf(Bar obj)
    {
        return barList.indexOf(obj);
    }

    public void clear()
    {
        barList.clear();
        max = -99999999;
        min = 99999999;
    }

    public int getCompression()
    {
        return compression;
    }

    public void setCompression(int compression)
    {
        this.compression = compression;
    }

    public double getClose(int index)
    {
        return get(index).getClose();
    }

    public double getOpen(int index)
    {
        return get(index).getOpen();
    }

    public double getHigh(int index)
    {
        return get(index).getHigh();
    }

    public double getLow(int index)
    {
        return get(index).getLow();
    }

    public Date getDate(int x)
    {
        return get(x).getDate();
    }

    public long getVolume(int x)
    {
        return get(x).getVolume();
    }

    public int getX(Date date)
    {
        if (date == null)
            return -1;

        for (int i = 0; i < size(); i++)
        {
            if (get(i).getDate().equals(date) || get(i).getDate().after(date))
                return i;
        }

        return -1;
    }

    public double getMax()
    {
        return this.max;
    }

    public double getMin()
    {
        return this.min;
    }
    
    public BarData getPeriod(Date begin, Date end)
    {
        BarData barData = new BarData();
        barData.compression = compression;

        for (Iterator iter = barList.iterator(); iter.hasNext(); )
        {
            Bar bar = (Bar)iter.next();
            if (bar.getDate().before(begin) || bar.getDate().after(end))
                continue;
            barData.append(bar);
        }

        return barData;
    }
    
    public BarData getCompressed(int interval)
    {
        BarData barData = new BarData();
        barData.compression = interval;

        if (interval < INTERVAL_DAILY)
        {
            int minutes = 1;
            Bar currentBar = null;
            Calendar nextBarTime = Calendar.getInstance();
            Calendar barTime = Calendar.getInstance();
            
            switch (interval)
            {
                case BarData.INTERVAL_MINUTE1:
                    minutes = 1;
                    break;
                case BarData.INTERVAL_MINUTE2:
                    minutes = 2;
                    break;
                case BarData.INTERVAL_MINUTE5:
                    minutes = 5;
                    break;
                case BarData.INTERVAL_MINUTE10:
                    minutes = 10;
                    break;
                case BarData.INTERVAL_MINUTE15:
                    minutes = 15;
                    break;
                case BarData.INTERVAL_MINUTE30:
                    minutes = 30;
                    break;
                case BarData.INTERVAL_MINUTE60:
                    minutes = 60;
                    break;
                default:
                    minutes = 1;
                    break;
            }

            for (Iterator iter = barList.iterator(); iter.hasNext(); )
            {
                Bar bar = (Bar)iter.next();
                
                if (currentBar != null && currentBar.getDate() != null)
                {
                    barTime.setTime(bar.getDate());
                    if (barTime.after(nextBarTime) || barTime.equals(nextBarTime))
                    {
                        barData.append(currentBar);
                        currentBar = null;
                    }
                }
                
                if (currentBar == null)
                {
                    currentBar = new Bar();
                    currentBar.setOpen(bar.getOpen());
                    barTime.setTime(bar.getDate());
                    barTime.set(Calendar.MILLISECOND, 0);
                    barTime.add(Calendar.MINUTE, - (barTime.get(Calendar.MINUTE) % minutes));
                    currentBar.setDate(barTime.getTime());
                    nextBarTime.setTime(currentBar.getDate());
                    nextBarTime.add(Calendar.MINUTE, minutes);
                }

                currentBar.update(bar);
            }
            
            if (currentBar != null)
                barData.append(currentBar);
        }
        else if (interval == INTERVAL_WEEKLY)
        {
            Bar currentBar = null;
            Calendar nextBarTime = Calendar.getInstance();
            Calendar barTime = Calendar.getInstance();
            
            for (Iterator iter = barList.iterator(); iter.hasNext(); )
            {
                Bar bar = (Bar)iter.next();
                
                if (currentBar != null && currentBar.getDate() != null)
                {
                    barTime.setTime(bar.getDate());
                    if (barTime.after(nextBarTime) || barTime.equals(nextBarTime))
                    {
                        barData.append(currentBar);
                        currentBar = null;
                    }
                }
                
                if (currentBar == null)
                {
                    currentBar = new Bar();
                    currentBar.setOpen(bar.getOpen());
                    barTime.setTime(bar.getDate());
                    barTime.set(Calendar.MILLISECOND, 0);
                    barTime.set(Calendar.SECOND, 0);
                    barTime.set(Calendar.MINUTE, 0);
                    barTime.set(Calendar.HOUR, 0);
                    barTime.add(Calendar.DATE, - (barTime.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY));
                    currentBar.setDate(barTime.getTime());
                    nextBarTime.setTime(currentBar.getDate());
                    nextBarTime.add(Calendar.DATE, 7);
                }

                currentBar.update(bar);
            }
            
            if (currentBar != null)
                barData.append(currentBar);
        }
        else if (interval == INTERVAL_MONTHLY)
        {
            Bar currentBar = null;
            Calendar nextBarTime = Calendar.getInstance();
            Calendar barTime = Calendar.getInstance();
            
            for (Iterator iter = barList.iterator(); iter.hasNext(); )
            {
                Bar bar = (Bar)iter.next();
                
                if (currentBar != null && currentBar.getDate() != null)
                {
                    barTime.setTime(bar.getDate());
                    if (barTime.after(nextBarTime) || barTime.equals(nextBarTime))
                    {
                        barData.append(currentBar);
                        currentBar = null;
                    }
                }
                
                if (currentBar == null)
                {
                    currentBar = new Bar();
                    currentBar.setOpen(bar.getOpen());
                    barTime.setTime(bar.getDate());
                    barTime.set(Calendar.MILLISECOND, 0);
                    barTime.set(Calendar.SECOND, 0);
                    barTime.set(Calendar.MINUTE, 0);
                    barTime.set(Calendar.HOUR, 0);
                    barTime.add(Calendar.DATE, - (barTime.get(Calendar.DAY_OF_MONTH) - 1));
                    currentBar.setDate(barTime.getTime());
                    nextBarTime.setTime(currentBar.getDate());
                    nextBarTime.add(Calendar.MONTH, 1);
                }

                currentBar.update(bar);
            }
            
            if (currentBar != null)
                barData.append(currentBar);
        }
        else
            barData = new BarData(barList);
        
        return barData;
    }
}
