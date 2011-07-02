/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public class DecoratorToolTip extends ToolTip {

    public int OFFSET_X = 12;
    public int OFFSET_Y = 12;

    private Control parent;
    private CLabel label;
    private String text;

    public DecoratorToolTip(Control control) {
        super(control, ToolTip.NO_RECREATE, true);
        this.parent = control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ToolTip#createToolTipContentArea(org.eclipse.swt.widgets.Event, org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Composite createToolTipContentArea(Event event, Composite parent) {
        label = new CLabel(parent, SWT.SHADOW_NONE);
        label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        if (text != null) {
            label.setText(text);
        }
        return label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        if (label != null && !label.isDisposed()) {
            label.setText(text);
            label.getParent().pack();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ToolTip#shouldCreateToolTip(org.eclipse.swt.widgets.Event)
     */
    @Override
    protected boolean shouldCreateToolTip(Event event) {
        return label == null || label.isDisposed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ToolTip#getLocation(org.eclipse.swt.graphics.Point, org.eclipse.swt.widgets.Event)
     */
    @Override
    public Point getLocation(Point tipSize, Event event) {
        Rectangle bounds = parent.getBounds();
        Point p = new Point(event.x + OFFSET_X, event.y + OFFSET_Y);
        if (p.x + tipSize.x > bounds.width) {
            p.x = event.x - tipSize.x - OFFSET_X;
            if (p.x < 0) {
                p.x = event.x + OFFSET_X;
            }
        }
        if (p.y + tipSize.y > bounds.height) {
            p.y = event.y - tipSize.y - OFFSET_Y;
            if (p.y < 0) {
                p.y = event.y + OFFSET_Y;
            }
        }
        return parent.toDisplay(p);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ToolTip#show(org.eclipse.swt.graphics.Point)
     */
    @Override
    public void show(Point location) {
        location = parent.toControl(location);
        if (label != null && !label.isDisposed()) {
            Shell tip = (Shell) label.getParent();
            Event event = new Event();
            event.x = location.x;
            event.y = location.y;
            event.widget = parent;
            tip.setLocation(getLocation(tip.getSize(), event));
        }
        else {
            super.show(location);
        }
    }
}
