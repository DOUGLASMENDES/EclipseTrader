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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

public class Box extends Canvas implements PaintListener
{
    Composite row1;
    Label name;
    Label time;
    Composite row2;
    Label value;
    Label change;
    Label icon;
    Color background = new Color(null, 255, 255, 255);
    Color foreground = new Color(null, 0, 0, 0);
    Color negativeForeground = new Color(null, 240, 0, 0);
    Color positiveForeground = new Color(null, 0, 192, 0);
    List selectionListeners = new ArrayList();
    MouseListener mouseListener = new MouseAdapter() {

        public void mouseDown(MouseEvent e)
        {
            Event event = new Event();
            event.display = e.display;
            event.x = e.x;
            event.y = e.y;
            event.item = Box.this;
            event.widget = Box.this;
            event.time = e.time;

            SelectionEvent selection = new SelectionEvent(event);
            
            SelectionListener[] listeners = (SelectionListener[])selectionListeners.toArray(new SelectionListener[selectionListeners.size()]);
            for (int i = 0; i < listeners.length; i++)
                listeners[i].widgetSelected(selection);
        }

        public void mouseUp(MouseEvent e)
        {
        }
    };

    public Box(Composite parent, int style)
    {
        super(parent, style);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 3;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 2;
        setLayout(gridLayout);
        super.setBackground(background);
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
        
        row1.addMouseListener(mouseListener);
        name.addMouseListener(mouseListener);
        time.addMouseListener(mouseListener);
        row2.addMouseListener(mouseListener);
        value.addMouseListener(mouseListener);
        change.addMouseListener(mouseListener);
        icon.addMouseListener(mouseListener);
        addMouseListener(mouseListener);
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
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
     */
    public void setMenu(Menu menu)
    {
        row1.setMenu(menu);
        name.setMenu(menu);
        time.setMenu(menu);
        row2.setMenu(menu);
        value.setMenu(menu);
        change.setMenu(menu);
        icon.setMenu(menu);
        super.setMenu(menu);
    }
    
    public void addSelectionListener(SelectionListener listener)
    {
        selectionListeners.add(listener);
    }
    
    public void removeSelectionListener(SelectionListener listener)
    {
        selectionListeners.remove(listener);
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
        name = name.replaceAll("[&]", "&&");
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

    public void setBackground(Color background)
    {
        if (this.background != null)
            this.background.dispose();
        this.background = new Color(null, background.getRGB());

        super.setBackground(background);
        row1.setBackground(background);
        name.setBackground(background);
        time.setBackground(background);
        row2.setBackground(background);
        value.setBackground(background);
        change.setBackground(background);
        change.getParent().setBackground(background);
        icon.setBackground(background);
    }

    public void setForeground(Color foreground)
    {
        if (this.foreground != null)
            this.foreground.dispose();
        this.foreground = new Color(null, foreground.getRGB());
        name.setForeground(foreground);
        time.setForeground(foreground);
        value.setForeground(foreground);
        if (!change.getText().startsWith("-") && !change.getText().startsWith("+"))
            this.change.setForeground(foreground);
        redraw();
    }

    public void setNegativeForeground(Color negativeForeground)
    {
        if (this.negativeForeground != null)
            this.negativeForeground.dispose();
        this.negativeForeground = new Color(null, negativeForeground.getRGB());
        if (change.getText().startsWith("-"))
            this.change.setForeground(negativeForeground);
    }

    public void setPositiveForeground(Color positiveForeground)
    {
        if (this.positiveForeground != null)
            this.positiveForeground.dispose();
        this.positiveForeground = new Color(null, positiveForeground.getRGB());
        if (change.getText().startsWith("+"))
            this.change.setForeground(positiveForeground);
    }
}
