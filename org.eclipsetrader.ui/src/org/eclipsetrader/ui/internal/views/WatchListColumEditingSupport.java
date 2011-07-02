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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Table;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IEditableDataProvider;

public class WatchListColumEditingSupport extends EditingSupport {

    private WatchListView view;
    private WatchListViewColumn column;
    private CellEditor editor;

    public WatchListColumEditingSupport(WatchListView view, WatchListViewColumn column) {
        super(view.getViewer());
        this.view = view;
        this.column = column;
        this.editor = new TextCellEditor((Table) view.getViewer().getControl());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
     */
    @Override
    protected boolean canEdit(Object element) {
        WatchListViewItem viewItem = (WatchListViewItem) element;
        IDataProvider dataProvider = viewItem.getDataProvider(column.getDataProviderFactory().getId());
        return dataProvider instanceof IEditableDataProvider;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
     */
    @Override
    protected CellEditor getCellEditor(Object element) {
        return editor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
     */
    @Override
    protected Object getValue(Object element) {
        WatchListViewItem viewItem = (WatchListViewItem) element;

        IAdaptable value = viewItem.getValue(column.getDataProviderFactory().getId());
        if (value != null) {
            String s = (String) value.getAdapter(String.class);
            return s;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
     */
    @Override
    protected void setValue(Object element, Object value) {
        WatchListViewItem viewItem = (WatchListViewItem) element;

        IDataProvider dataProvider = viewItem.getDataProvider(column.getDataProviderFactory().getId());
        if (dataProvider instanceof IEditableDataProvider) {
            if (!value.equals(getValue(element))) {
                ((IEditableDataProvider) dataProvider).setValue(viewItem, value);

                view.doUpdateItem(viewItem);
                view.setDirty();
            }
        }
    }
}
