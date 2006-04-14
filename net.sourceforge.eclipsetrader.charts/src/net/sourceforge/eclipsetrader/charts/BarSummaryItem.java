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

import net.sourceforge.eclipsetrader.core.db.Bar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Label;

public class BarSummaryItem extends SummaryItem
{
    private Label change;
    private Label open;
    private Label high;
    private Label low;
    private Label close;
    private Color positive = new Color(null, 0, 192, 0);
    private Color negative = new Color(null, 192, 0, 0);
    private NumberFormat pf = ChartsPlugin.getPriceFormat();
    private NumberFormat pcf = ChartsPlugin.getPercentageFormat();
    private double previousChange = 99999999;

    public BarSummaryItem(Summary parent, int style)
    {
        super(parent, style);
        
        change = new Label(parent, SWT.NONE);
        change.setBackground(parent.getBackground());
        change.setText("CH=" + pcf.format(0) + "%");
        
        open = new Label(parent, SWT.NONE);
        open.setBackground(parent.getBackground());
        open.setText("O=" + pf.format(0));

        high = new Label(parent, SWT.NONE);
        high.setBackground(parent.getBackground());
        high.setText("H=" + pf.format(0));
        
        low = new Label(parent, SWT.NONE);
        low.setBackground(parent.getBackground());
        low.setText("L=" + pf.format(0));
        
        close = new Label(parent, SWT.NONE);
        close.setBackground(parent.getBackground());
        close.setText("C=" + pf.format(0));
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.SummaryItem#dispose()
     */
    public void dispose()
    {
        change.dispose();
        open.dispose();
        high.dispose();
        low.dispose();
        close.dispose();
        super.dispose();
    }

    public void setData(Bar bar, Bar previous)
    {
        double ch = 0;
        if (previous != null)
            ch = (bar.getClose() - previous.getClose()) / previous.getClose() * 100.0; 

        if (previousChange != ch)
        {
            change.setText("CH=" + pcf.format(ch) + "%");
            if (ch > 0)
                change.setForeground(positive);
            else if (ch < 0)
                change.setForeground(negative);
            else
                change.setForeground(getForeground());
            previousChange = ch;
        }
        open.setText("O=" + pf.format(bar.getOpen()));
        high.setText("H=" + pf.format(bar.getHigh()));
        low.setText("L=" + pf.format(bar.getLow()));
        close.setText("C=" + pf.format(bar.getClose()));
    }
    
    public void setData(Bar bar)
    {
        if (bar != null)
        {
            if (previousChange != 0)
            {
                change.setText("CH=" + pcf.format(0) + "%");
                change.setForeground(getForeground());
                previousChange = 0;
            }
            open.setText("O=" + pf.format(bar.getOpen()));
            high.setText("H=" + pf.format(bar.getHigh()));
            low.setText("L=" + pf.format(bar.getLow()));
            close.setText("C=" + pf.format(bar.getClose()));
        }
        else
        {
            change.setText("CH=");
            change.setForeground(getForeground());
            open.setText("O=");
            high.setText("H=");
            low.setText("L=");
            close.setText("C=");
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.SummaryItem#setForeground(org.eclipse.swt.graphics.Color)
     */
    public void setForeground(Color color)
    {
        open.setForeground(color);
        high.setForeground(color);
        low.setForeground(color);
        close.setForeground(color);
        super.setForeground(color);
    }
}
