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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Label;

public class NumberSummaryItem extends SummaryItem
{
    private Label number;
    private NumberFormat pf = ChartsPlugin.getPriceFormat();

    public NumberSummaryItem(Summary parent, int style)
    {
        super(parent, style);

        number = new Label(parent, SWT.NONE);
        number.setBackground(parent.getBackground());
        number.setText(getText() + "=" + pf.format(0)); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.SummaryItem#dispose()
     */
    public void dispose()
    {
        number.dispose();
        super.dispose();
    }

    public void setData(double value)
    {
        number.setText(getText() + "=" + pf.format(value)); //$NON-NLS-1$
    }

    public void setData(Double value)
    {
        if (value != null)
        {
            String s = pf.format(value);
            if (s.length() >= 9)
                s = s.substring(0, s.length() - 5);
            number.setText(getText() + "=" + s); //$NON-NLS-1$
        }
        else
            number.setText(getText() + "="); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.SummaryItem#setForeground(org.eclipse.swt.graphics.Color)
     */
    public void setForeground(Color color)
    {
        number.setForeground(color);
        super.setForeground(color);
    }
}
