/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

public class BoxViewer extends AbstractTableViewer {
	private Composite control;
	private List<BoxViewerRow> rows = new ArrayList<BoxViewerRow>();
	private BoxItem selection;

	public BoxViewer(Composite parent) {
		this(parent, SWT.NONE);
	}

	public BoxViewer(Composite parent, int style) {
		control = new Composite(parent, style);

		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = true;
		control.setLayout(layout);

		control.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
            	if (selection != event.item) {
            		selection = (BoxItem) event.item;
            		control.notifyListeners(SWT.Selection, event);
            		fireSelectionChanged(new SelectionChangedEvent(BoxViewer.this, selection != null ? new StructuredSelection(selection) : StructuredSelection.EMPTY));
            	}
            }
		});
	}

    BoxItem getItem(int index) {
	    return (BoxItem) rows.get(index).getItem();
    }

    BoxViewerRow getRow(int index) {
	    return rows.get(index);
    }

    int getRowCount() {
    	return rows.size();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doClear(int)
     */
    @Override
    protected void doClear(int index) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doClearAll()
     */
    @Override
    protected void doClearAll() {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doDeselectAll()
     */
    @Override
    protected void doDeselectAll() {
    	selection = null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doGetColumn(int)
     */
    @Override
    protected Widget doGetColumn(int index) {
	    return control;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doGetItem(int)
     */
    @Override
    protected Item doGetItem(int index) {
	    return (Item) rows.get(index).getItem();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doGetItemCount()
     */
    @Override
    protected int doGetItemCount() {
	    return rows.size();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doGetItems()
     */
    @Override
    protected Item[] doGetItems() {
    	Item[] items = new Item[rows.size()];
    	for (int i = 0; i < rows.size(); i++)
    		items[i] = (Item) rows.get(i).getItem();
	    return items;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doGetSelection()
     */
    @Override
    protected Item[] doGetSelection() {
	    return selection != null ? new Item[] { selection } : new Item[0];
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doGetSelectionIndices()
     */
    @Override
    protected int[] doGetSelectionIndices() {
	    return new int[0];
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doIndexOf(org.eclipse.swt.widgets.Item)
     */
    @Override
    protected int doIndexOf(Item item) {
    	for (int i = 0; i < rows.size(); i++) {
    		if (rows.get(i).getItem() == item)
    			return i;
    	}
	    return -1;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doRemove(int, int)
     */
    @Override
    protected void doRemove(int start, int end) {
    	BoxViewerRow[] r = rows.toArray(new BoxViewerRow[rows.size()]);
    	for (int i = start; i <= end; i++) {
    		((BoxItem) r[i].getItem()).setMenu(null);
    		r[i].getItem().dispose();
    		rows.remove(r[i]);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doRemove(int[])
     */
    @Override
    protected void doRemove(int[] indices) {
    	BoxViewerRow[] r = rows.toArray(new BoxViewerRow[rows.size()]);
    	for (int i = 0; i < indices.length; i++) {
    		((BoxItem) r[indices[i]].getItem()).setMenu(null);
    		r[indices[i]].getItem().dispose();
    		rows.remove(r[indices[i]]);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doRemoveAll()
     */
    @Override
    protected void doRemoveAll() {
    	for (int i = 0; i < rows.size(); i++) {
    		((BoxItem) rows.get(i).getItem()).setMenu(null);
    		rows.get(i).getItem().dispose();
    	}
    	rows.clear();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doResetItem(org.eclipse.swt.widgets.Item)
     */
    @Override
    protected void doResetItem(Item item) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doSelect(int[])
     */
    @Override
    protected void doSelect(int[] indices) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doSetItemCount(int)
     */
    @Override
    protected void doSetItemCount(int count) {
    	while(rows.size() > count) {
    		BoxViewerRow row = rows.get(rows.size() - 1);
    		((BoxItem) row.getItem()).setMenu(null);
    		row.getItem().dispose();
    		rows.remove(rows.size() - 1);
    	}

    	int columns = doGetColumnCount();
    	while(rows.size() < count) {
        	BoxItem item = new BoxItem(control, SWT.NONE);
       		item.setMenu(control.getMenu());
        	BoxViewerRow viewerRow = new BoxViewerRow(item, columns);
        	rows.add(viewerRow);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doSetSelection(int[])
     */
    @Override
    protected void doSetSelection(int[] indices) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doSetSelection(org.eclipse.swt.widgets.Item[])
     */
    @Override
    protected void doSetSelection(Item[] items) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doShowItem(org.eclipse.swt.widgets.Item)
     */
    @Override
    protected void doShowItem(Item item) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doShowSelection()
     */
    @Override
    protected void doShowSelection() {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#internalCreateNewRowPart(int, int)
     */
    @Override
    protected ViewerRow internalCreateNewRowPart(int style, int rowIndex) {
    	if (rowIndex >= rows.size())
    		doSetItemCount(rowIndex + 1);
	    return rows.get(rowIndex);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnViewer#createViewerEditor()
     */
    @Override
    protected ColumnViewerEditor createViewerEditor() {
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnViewer#doGetColumnCount()
     */
    @Override
    protected int doGetColumnCount() {
	    return 5;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnViewer#getItemAt(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Item getItemAt(Point point) {
    	Point displayPoint = control.toDisplay(point);
    	for (int i = 0; i < rows.size(); i++) {
    		BoxItem item = (BoxItem) rows.get(i).getItem();
    		Point p = item.getCanvas().toControl(displayPoint);
    		Rectangle bounds = item.getBounds();
    		if (p.x >= bounds.x && p.x < (bounds.x + bounds.width) && p.y >= bounds.y && p.y < (bounds.y + bounds.height)) {
    			System.out.println("Item is " + item);
    			return item;
    		}
    	}
		System.out.println("Item is null");
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnViewer#getViewerRowFromItem(org.eclipse.swt.widgets.Widget)
     */
    @Override
    protected ViewerRow getViewerRowFromItem(Widget item) {
    	for (int i = 0; i < rows.size(); i++) {
    		if (rows.get(i).getItem() == item)
    			return rows.get(i);
    	}
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.Viewer#getControl()
     */
    @Override
    public Control getControl() {
	    return control;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnViewer#update(java.lang.Object, java.lang.String[])
     */
    @Override
    public void update(Object element, String[] properties) {
	    super.update(element, properties);
	    control.layout();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTableViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object, boolean)
     */
    @Override
    protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
	    super.doUpdateItem(widget, element, fullMap);
	    ((BoxItem) widget).getCanvas().layout();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.StructuredViewer#refresh()
     */
    @Override
    public void refresh() {
	    super.refresh();
	    control.layout();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.StructuredViewer#refresh(boolean)
     */
    @Override
    public void refresh(boolean updateLabels) {
	    super.refresh(updateLabels);
	    control.layout();
    }
}
