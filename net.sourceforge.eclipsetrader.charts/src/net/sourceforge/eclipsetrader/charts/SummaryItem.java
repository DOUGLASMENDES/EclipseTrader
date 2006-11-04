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

import org.eclipse.swt.graphics.Color;

public abstract class SummaryItem
{
    private String text = ""; //$NON-NLS-1$
    private Color foreground = new Color(null, 0, 0, 0);

    public SummaryItem(Summary parent, int style)
    {
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void setForeground(Color color)
    {
        foreground = color;
    }
    
    public Color getForeground()
    {
        return foreground;
    }
    
    public void dispose()
    {
    }
}
