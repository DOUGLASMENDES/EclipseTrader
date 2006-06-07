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

package net.sourceforge.eclipsetrader.charts;

import java.text.NumberFormat;
import java.util.List;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 */
public class ScalePlot extends Canvas implements ControlListener, DisposeListener, PaintListener
{
    private Image image;
    private Scaler scaler;
    private NumberFormat nf = NumberFormat.getInstance();
    private Label label;
    private Color labelColor = new Color(null, 255, 255, 0);
    private Color separatorColor = new Color(null, 0, 0, 0);
    private boolean needRepaint = true;

    public ScalePlot(Composite parent, int style)
    {
        super(parent, style|SWT.DOUBLE_BUFFERED);

        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
        
        label = new Label(this, SWT.NONE);
        label.setBackground(labelColor);
        label.setBounds(0, 0, 0, 0);
        
        addControlListener(this);
        addDisposeListener(this);
        addPaintListener(this);
    }
    
    public void setScaler(Scaler scaler)
    {
        this.scaler = scaler;
        if (this.scaler != null)
            this.scaler.set(getSize().y);
    }

    public Color getSeparatorColor()
    {
        return new Color(null, separatorColor.getRGB());
    }

    public void setSeparatorColor(Color separatorColor)
    {
        if (this.separatorColor != null && !this.separatorColor.isDisposed())
            this.separatorColor.dispose();
        this.separatorColor = new Color(null, separatorColor.getRGB());
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
     */
    public void controlMoved(ControlEvent e)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
     */
    public void controlResized(ControlEvent e)
    {
        if (image != null && !image.isDisposed())
            image.dispose();

        if (getSize().x != 0 && getSize().y != 0)
        {
            image = new Image(getDisplay(), getSize().x, getSize().y);
            
            if (scaler != null)
                scaler.set(getSize().y);
    
            GC gc = new GC(image);
            draw(gc);
            gc.dispose();
        }
    }
    
    public void redrawAll()
    {
        if (needRepaint == false)
        {
            needRepaint = true;
            redraw();
        }
    }

    public void draw(GC gc)
    {
        Rectangle bounds = image.getBounds(); 
        gc.fillRectangle(bounds);

        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);

        if (scaler != null)
        {
            List scaleArray = scaler.getScaleArray();

            int loop;
            for (loop = 0; loop < scaleArray.size(); loop++)
            {
                int y = scaler.convertToY(((Double) scaleArray.get(loop)).doubleValue());
                double value = ((Double) scaleArray.get(loop)).doubleValue();
                String s = "";
                
                if (Math.abs(value) > 1000000)
                {
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    s = nf.format(value / 1000000) + "m";
                }
                else if (Math.abs(value) > 100)
                {
                    nf.setMinimumFractionDigits(2);
                    nf.setMaximumFractionDigits(2);
                    s = nf.format(value);
                }
                else
                {
                    nf.setMinimumFractionDigits(4);
                    nf.setMaximumFractionDigits(4);
                    s = nf.format(value);
                }
                
/*                if (Math.abs(value) > 1000000000)
                {
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    s = nf.format(value / 1000000000) + "b";
                }
                else if (Math.abs(value) > 1000000)
                {
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    s = nf.format(value / 1000000) + "m";
                }
                else if (Math.abs(value) > 1000)
                {
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    s = nf.format(value / 1000) + "k";
                }*/
                int h = gc.stringExtent(s).y / 2;

                if ((y - h) >= 0 && (y + h) <= bounds.height)
                {
                    gc.drawLine(0, y, 4, y);
                    gc.drawString(s, 7, y - h);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e)
    {
        if (image != null && !image.isDisposed())
        {
            if (needRepaint)
            {
                GC gc = new GC(image);
                draw(gc);
                gc.dispose();
                needRepaint = false;
            }
            e.gc.drawImage(image, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
        }

        e.gc.setForeground(separatorColor);
        e.gc.drawLine(0, 0, 0, getSize().y);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
        if (image != null && !image.isDisposed())
            image.dispose();
        if (separatorColor != null && !separatorColor.isDisposed())
            separatorColor.dispose();
        if (labelColor != null && !labelColor.isDisposed())
            labelColor.dispose();
    }
    
    public void setLabel(int y)
    {
        if (scaler != null)
        {
            String s = nf.format(Scaler.roundToTick(scaler.convertToValue(y)));
            if (s.length() >= 9)
                s = s.substring(0, s.length() - 4) + "k";
            label.setText(" " + s);
            label.setBounds(1, y - 7, getSize().x - 1, 14);
        }
    }
    
    public void hideLabel()
    {
        label.setBounds(0, 0, 0, 0);
    }
}
