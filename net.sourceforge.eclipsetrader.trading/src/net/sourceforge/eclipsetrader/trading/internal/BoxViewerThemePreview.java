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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemePreview;

public class BoxViewerThemePreview implements IThemePreview, IPropertyChangeListener
{
    private Box box1;
    private Box box2;

    public BoxViewerThemePreview()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IThemePreview#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.themes.ITheme)
     */
    public void createControl(Composite parent, ITheme currentTheme)
    {
        Color foreground = currentTheme.getColorRegistry().get(WatchlistBoxViewer.FOREGROUND);
        Color background = currentTheme.getColorRegistry().get(WatchlistBoxViewer.BACKGROUND);
        Color positiveForeground = currentTheme.getColorRegistry().get(WatchlistBoxViewer.POSITIVE_FOREGROUND);
        Color negativeForeground = currentTheme.getColorRegistry().get(WatchlistBoxViewer.NEGATIVE_FOREGROUND);

        Composite content = new Composite(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        rowLayout.pack = true;
        rowLayout.fill = true;
        rowLayout.justify = false;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.marginLeft = 2;
        rowLayout.marginTop = 2;
        rowLayout.marginRight = 2;
        rowLayout.marginBottom = 2;
        rowLayout.spacing = 3;
        content.setLayout(rowLayout);

        box1 = new Box(content, SWT.NONE);
        box1.setForeground(foreground);
        box1.setBackground(background);
        box1.setPositiveForeground(positiveForeground);
        box1.setNegativeForeground(negativeForeground);
        box1.setName("DowJones");
        box1.setTime("11:35");
        box1.setValue("10,816.92");
        box1.setChange("+110.78 (+1.03%)");

        box2 = new Box(content, SWT.NONE);
        box2.setForeground(foreground);
        box2.setBackground(background);
        box2.setPositiveForeground(positiveForeground);
        box2.setNegativeForeground(negativeForeground);
        box2.setName("FTSE 100");
        box2.setTime("11:38");
        box2.setValue("5,564.40");
        box2.setChange("-57.60 (-1.08%)");
        
        currentTheme.addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IThemePreview#dispose()
     */
    public void dispose()
    {
        box1.dispose();
        box2.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(WatchlistBoxViewer.BACKGROUND))
        {
            Color color = new Color(null, (RGB) event.getNewValue());
            box1.setBackground(color);
            box2.setBackground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistBoxViewer.FOREGROUND))
        {
            Color color = new Color(null, (RGB) event.getNewValue());
            box1.setForeground(color);
            box2.setForeground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistBoxViewer.POSITIVE_FOREGROUND))
        {
            Color color = new Color(null, (RGB) event.getNewValue());
            box1.setPositiveForeground(color);
            box2.setPositiveForeground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistBoxViewer.NEGATIVE_FOREGROUND))
        {
            Color color = new Color(null, (RGB) event.getNewValue());
            box1.setNegativeForeground(color);
            box2.setNegativeForeground(color);
            color.dispose();
        }
    }
}
