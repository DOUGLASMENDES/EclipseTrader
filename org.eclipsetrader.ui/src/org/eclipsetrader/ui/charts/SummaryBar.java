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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SummaryBar {

    Composite control;

    public SummaryBar(Composite parent, int style) {
        control = new Composite(parent, SWT.NONE);

        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.wrap = true;
        rowLayout.pack = true;
        rowLayout.marginLeft = 5;
        rowLayout.marginRight = 5;
        rowLayout.marginTop = 0;
        rowLayout.marginBottom = 1;
        rowLayout.spacing = 5;
        control.setLayout(rowLayout);

        control.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        control.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        control.setBackgroundMode(SWT.INHERIT_FORCE);

        control.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                Rectangle bounds = control.getBounds();
                e.gc.drawLine(0, bounds.height - 1, bounds.width, bounds.height - 1);
            }
        });
    }

    public Control getControl() {
        return control;
    }

    /**
     * Sets the layout data associated with the receiver to the argument.
     *
     * @param layoutData the new layout data for the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLayoutData(Object layOutData) {
        control.setLayoutData(layOutData);
    }

    /**
     * Returns layout data which is associated with the receiver.
     *
     * @return the receiver's layout data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Object getLayoutData() {
        return control.getLayoutData();
    }

    /**
     * If the receiver has a layout, asks the layout to <em>lay out</em>
     * (that is, set the size and location of) the receiver's children.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void layout() {
        control.layout();
    }

    public void removeAll() {
        Control[] children = control.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].dispose();
        }
    }

    public Composite getParent() {
        return control.getParent();
    }

    public Composite getCompositeControl() {
        return control;
    }
}
