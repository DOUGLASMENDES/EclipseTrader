/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.TableItem;

/**
 * Instances of this class are used to hilight a table cell or row.
 */
public class CellTicker
{
    public static final int BACKGROUND = 0x0001;
    public static final int FOREGROUND = 0x0002;
    private int style;
    private int interval = 5;
    private Color foreground;
    private Color background = new Color(null, 255, 224, 0);
    private Color incrementForeground = new Color(null, 0, 224, 0);
    private Color incrementBackground;
    private Color decrementForeground = new Color(null, 224, 0, 0);
    private Color decrementBackground;
    private TableItem tableItem;
    private Status rowStatus;
    private List status = new ArrayList();
    
    private class Status implements Runnable {
        TableItem item;
        int index = -1;
        Color bg;
        Color fg;
        long started = 0;
        
        public void run()
        {
            if (item != null && !item.isDisposed() && started != 0)
            {
                if (index == -1)
                    item.setBackground(bg);
                else
                    item.setBackground(index, bg);
                if (bg != null)
                    bg = null;
                if (index == -1)
                    item.setForeground(fg);
                else
                    item.setForeground(index, fg);
                if (fg != null)
                    fg = null;
            }
            started = 0;
        }
    }
    
    public CellTicker(TableItem tableItem, int style)
    {
        this.tableItem = tableItem;
        this.style = style;
        while(status.size() < tableItem.getParent().getColumnCount())
            status.add(new Status());
        rowStatus = new Status();
        rowStatus.item = tableItem;
    }
    
    public void dispose()
    {
        if (foreground != null)
            foreground.dispose();
        if (background != null)
            background.dispose();
    }

    public void tick(int index)
    {
        tick(index, background, foreground);
    }

    public void tickIncrement(int index)
    {
        tick(index, incrementBackground, incrementForeground);
    }

    public void tickDecrement(int index)
    {
        tick(index, decrementBackground, decrementForeground);
    }

    /**
     * Ticks a single cell.
     * <p>The ticking occurs only if there isn't already a row tick in progress.</p>
     * 
     * @param index - the column index to tick
     * @param background - the background color
     * @param foreground - the foreground color
     */
    public synchronized void tick(int index, Color background, Color foreground)
    {
        while(status.size() < (index + 1))
            status.add(new Status());
        
        Status s = (Status)status.get(index);
        if (s.started == 0 && rowStatus.started == 0)
        {
            s.item = tableItem;
            s.index = index;
            if ((style & FOREGROUND) != 0)
            {
                s.bg = tableItem.getBackground(index);
                Color bg = tableItem.getBackground();
                if (bg.equals(s.bg))
                    s.bg = null;
            }
            if ((style & BACKGROUND) != 0)
            {
                s.fg = tableItem.getForeground(index);
                Color fg = tableItem.getForeground();
                if (fg.equals(s.fg))
                    s.fg = null;
            }
            if (background != null)
                tableItem.setBackground(index, background);
            if (foreground != null)
                tableItem.setForeground(index, foreground);
            s.started = System.currentTimeMillis();
            tableItem.getDisplay().timerExec(interval * 1000, s);
        }
    }

    /**
     * Ticks the entire row.
     * <p>The existing cell tickers are reset to allow the whole row to
     * be hilighted with the given colors.</p>
     * 
     * @param background - the background color
     * @param foreground - the foreground color
     */
    public synchronized void tick(Color background, Color foreground)
    {
        if (rowStatus.started == 0)
        {
            Status[] stats = (Status[]) status.toArray(new Status[0]);
            for (int i = 0; i < stats.length; i++)
                stats[i].run();
            
            if ((style & FOREGROUND) != 0)
                rowStatus.bg = tableItem.getBackground();
            if ((style & BACKGROUND) != 0)
                rowStatus.fg = tableItem.getForeground();
            if (background != null)
                tableItem.setBackground(background);
            if (foreground != null)
                tableItem.setForeground(foreground);
            rowStatus.started = System.currentTimeMillis();
            tableItem.getDisplay().timerExec(interval * 1000, rowStatus);
        }
    }

    public Color getBackground()
    {
        return background;
    }

    public void setBackground(Color background)
    {
        if (this.background != null)
            this.background.dispose();
        this.background = new Color(tableItem.getDisplay(), background.getRGB());
    }

    public Color getForeground()
    {
        return foreground;
    }

    public void setForeground(Color foreground)
    {
        if (this.foreground != null)
            this.foreground.dispose();
        this.foreground = new Color(tableItem.getDisplay(), foreground.getRGB());
    }

    public Color getDecrementBackground()
    {
        return decrementBackground;
    }

    public void setDecrementBackground(Color decrementBackground)
    {
        this.decrementBackground = decrementBackground;
    }

    public Color getDecrementForeground()
    {
        return decrementForeground;
    }

    public void setDecrementForeground(Color decrementForeground)
    {
        this.decrementForeground = decrementForeground;
    }

    public Color getIncrementBackground()
    {
        return incrementBackground;
    }

    public void setIncrementBackground(Color incrementBackground)
    {
        this.incrementBackground = incrementBackground;
    }

    public Color getIncrementForeground()
    {
        return incrementForeground;
    }

    public void setIncrementForeground(Color incrementForeground)
    {
        this.incrementForeground = incrementForeground;
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }
}
