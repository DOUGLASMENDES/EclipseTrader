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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class Summary extends Composite implements DisposeListener, PaintListener
{
    private Color foreground = new Color(null, 0, 0, 0);
    private Color background = new Color(null, 255, 255, 255);
    
    public Summary(Composite parent, int style)
    {
        super(parent, style);
        
        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.wrap = true;
        rowLayout.pack = true;
        rowLayout.marginLeft = 5;
        rowLayout.marginRight = 5;
        rowLayout.marginTop = 0;
        rowLayout.marginBottom = 0;
        rowLayout.spacing = 5;
        setLayout(rowLayout);
        setBackground(background);
        
        addPaintListener(this);
        addDisposeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
     */
    public Point computeSize(int wHint, int hHint, boolean changed)
    {
        Point p = super.computeSize(wHint, hHint, changed);
        p.y += 2;
        return p;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e)
    {
        e.gc.setForeground(foreground);
        e.gc.drawLine(0, getClientArea().height - 1, getClientArea().width, getClientArea().height - 1); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
        if (background != null && !background.isDisposed())
            background.dispose();
        if (foreground != null && !foreground.isDisposed())
            foreground.dispose();
    }
}
