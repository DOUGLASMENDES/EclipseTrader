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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipsetrader.core.views.IEditableDataProvider;

public class WatchListColumEditingSupport extends EditingSupport {

    private final String id;
    private final IEditableDataProvider dataProvider;
    private CellEditor editor;

    public WatchListColumEditingSupport(TableViewer viewer, IEditableDataProvider dataProvider, String id) {
        super(viewer);

        this.id = id;
        this.dataProvider = dataProvider;

        this.editor = new TextCellEditor(viewer.getTable());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
     */
    @Override
    protected boolean canEdit(Object element) {
        return true;
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

        IAdaptable value = (IAdaptable) viewItem.getValue(id);
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
        if (!value.equals(getValue(element))) {
            dataProvider.setValue(viewItem, value);
        }
    }
}
