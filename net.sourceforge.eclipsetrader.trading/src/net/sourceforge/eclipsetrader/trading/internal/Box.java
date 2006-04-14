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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class Box extends Composite implements PaintListener
{
    private Composite row1;
    private Label name;
    private Label time;
    private Composite row2;
    private Label value;
    private Label change;
    private Label icon;
    private Color background = new Color(null, 255, 255, 255);
    private Color foreground = new Color(null, 0, 0, 0);
    private Color negativeForeground = new Color(null, 240, 0, 0);
    private Color positiveForeground = new Color(null, 0, 192, 0);

    public Box(Composite parent, int style)
    {
        super(parent, style);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 3;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 2;
        setLayout(gridLayout);
        setBackground(background);
        addPaintListener(this);
        
        row1 = new Composite(this, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 3;
        gridLayout.verticalSpacing = 0;
        row1.setLayout(gridLayout);
        row1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        row1.setBackground(background);
        
        name = new Label(row1, SWT.NONE);
        name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        name.setForeground(foreground);
        name.setBackground(background);
        
        time = new Label(row1, SWT.NONE);
        time.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        time.setForeground(foreground);
        time.setBackground(background);
        
        row2 = new Composite(this, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 3;
        gridLayout.verticalSpacing = 0;
        row2.setLayout(gridLayout);
        row2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        row2.setBackground(background);
        
        value = new Label(row2, SWT.NONE);
        FontData[] fontData = value.getFont().getFontData();
        for (int i = 0; i < fontData.length; i++)
            fontData[i].setStyle(fontData[i].getStyle() | SWT.BOLD);
        value.setFont(new Font(value.getDisplay(), fontData));
        value.setForeground(foreground);
        value.setBackground(background);
        
        Composite composite = new Composite(row2, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 3;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        composite.setBackground(background);
        
        change = new Label(composite, SWT.NONE);
        change.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        change.setForeground(foreground);
        change.setBackground(background);
        change.setForeground(negativeForeground);
        
        icon = new Label(composite, SWT.NONE);
        icon.setBackground(background);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose()
    {
        background.dispose();
        foreground.dispose();
        negativeForeground.dispose();
        positiveForeground.dispose();
        super.dispose();
    }
    
    public void setChange(String change)
    {
        this.change.setText(change);
        if (change.startsWith("-"))
            this.change.setForeground(negativeForeground);
        else if (change.startsWith("+"))
            this.change.setForeground(positiveForeground);
        else
            this.change.setForeground(foreground);
    }

    public void setName(String name)
    {
        this.name.setText(name);
    }

    public void setTime(String time)
    {
        this.time.setText(time);
    }

    public void setValue(String value)
    {
        this.value.setText(value);
    }

    public void setImage(Image image)
    {
        icon.setImage(image);
    }
    
    public void clear()
    {
        name.setText("");
        value.setText("");
        change.setText("");
        time.setText("");
        icon.setImage(null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e)
    {
        e.gc.setForeground(foreground);
        Rectangle rect = getClientArea();
        e.gc.drawRectangle(0, 0, rect.width - 1, rect.height - 1);
    }
}
