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

package org.eclipsetrader.ui.internal.views;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;

public class WatchListViewCellLabelProvider extends ObservableMapOwnerDrawCellLabelProvider {

    static final int LINE_WIDTH = 2;

    private ColumnViewer viewer;
    private ViewerColumn column;

    private int columnIndex = -1;

    public WatchListViewCellLabelProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public WatchListViewCellLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
     */
    @Override
    protected void initialize(ColumnViewer viewer, ViewerColumn column) {
        Assert.isTrue(this.viewer == null && this.column == null, "Label provider instance already in use"); //$NON-NLS-1$
        this.viewer = viewer;
        this.column = column;
        super.initialize(viewer, column);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void measure(Event event, Object element) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#erase(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void erase(Event event, Object element) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void paint(Event event, Object element) {
        if (attributeMaps.length == 1) {
            return;
        }
        WatchListViewCellAttribute attribute = (WatchListViewCellAttribute) attributeMaps[1].get(element);
        if (attribute == null) {
            return;
        }

        int rowIndex = event.index;
        Color color = (rowIndex & 1) != 0 ? attribute.oddBackground : attribute.evenBackground;
        if (color == null || columnIndex == -1) {
            return;
        }

        Table table = (Table) event.widget;
        int width = table.getColumn(columnIndex).getWidth();

        event.gc.setLineWidth(LINE_WIDTH);
        event.gc.setForeground(color);
        event.gc.drawRectangle(event.x, event.y + 1, width - LINE_WIDTH - 1, event.height - LINE_WIDTH - 1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        WatchListViewItem element = (WatchListViewItem) cell.getElement();

        this.columnIndex = cell.getColumnIndex();

        Object value = attributeMaps[0].get(element);
        if (!(value instanceof IAdaptable)) {
            cell.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
            return;
        }

        IAdaptable adaptableValue = (IAdaptable) value;

        String text = (String) adaptableValue.getAdapter(String.class);
        cell.setText(text == null ? "" : text); //$NON-NLS-1$

        cell.setForeground((Color) adaptableValue.getAdapter(Color.class));
        cell.setFont((Font) adaptableValue.getAdapter(Font.class));
        cell.setImage((Image) adaptableValue.getAdapter(Image.class));
    }
}
