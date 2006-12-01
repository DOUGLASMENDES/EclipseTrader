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

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Label;

public class DateSummaryItem extends SummaryItem
{
    private Label date;
    private SimpleDateFormat df = CorePlugin.getDateFormat();

    public DateSummaryItem(Summary parent, int style)
    {
        super(parent, style);

        Color background = parent.getBackground();
        Color foreground = parent.getForeground();

        date = new Label(parent, SWT.NONE);
        date.setBackground(background);
        date.setForeground(foreground);
        date.setText("--/--/----"); //$NON-NLS-1$
        
        if (background != null)
            background.dispose();
        if (foreground != null)
            foreground.dispose();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.SummaryItem#dispose()
     */
    public void dispose()
    {
        date.dispose();
        super.dispose();
    }

    public void setData(Date value)
    {
        if (value != null)
            date.setText(df.format(value));
        else
            date.setText("--/--/----"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.SummaryItem#setForeground(org.eclipse.swt.graphics.Color)
     */
    public void setForeground(Color color)
    {
        date.setForeground(color);
        super.setForeground(color);
    }
}
