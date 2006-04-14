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

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.db.Level2;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class Trendbar extends Canvas implements DisposeListener, PaintListener
{
    private int bidQuantity[] = new int[5];
    private int askQuantity[] = new int[5];
    private Color indicator;
    private Color[] band = new Color[5];

    public Trendbar(Composite parent, int style)
    {
        super(parent, style);
        addPaintListener(this);
        addDisposeListener(this);
    }

    public void setData(Level2 bid, Level2 ask)
    {
        int index = 0;
        for (Iterator iter = bid.getGrouped().iterator(); iter.hasNext() && index < 5; index++)
            bidQuantity[index] = ((Level2.Item)iter.next()).quantity;
        while(index < 5)
            bidQuantity[index++] = 0;

        index = 0;
        for (Iterator iter = ask.getGrouped().iterator(); iter.hasNext() && index < 5; index++)
            askQuantity[index] = ((Level2.Item)iter.next()).quantity;
        while(index < 5)
            askQuantity[index++] = 0;
        
        redraw();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e)
    {
        int middle = 3;
        int width = this.getClientArea().width - 3;
        int height = this.getClientArea().height;

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
            bidWidth[i] = (int) ((width / total) * bidQuantity[i]);
            last -= bidWidth[i];
        }
        int[] askWidth = new int[5];
        for (int i = 0; i < askQuantity.length - 1; i++)
        {
            askWidth[i] = (int) ((width / total) * askQuantity[i]);
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

    public void setBandColors(Color[] colors)
    {
        for (int i = 0; i < band.length; i++)
        {
            if (band[i] != null && !band[i].isDisposed())
                band[i].dispose();
        }
        for (int i = 0; i < band.length && i < colors.length; i++)
            band[i] = new Color(null, colors[i].getRGB());
    }

    public void setBandColor(int index, Color color)
    {
        if (band[index] != null && !band[index].isDisposed())
            band[index].dispose();
        band[index] = new Color(null, color.getRGB());
    }
    
    public void setIndicatorColor(Color color)
    {
        if (indicator != null && !indicator.isDisposed())
            indicator.dispose();
        indicator = new Color(null, color.getRGB());
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
        for (int i = 0; i < band.length; i++)
        {
            if (band[i] != null && !band[i].isDisposed())
                band[i].dispose();
        }
    }
}
