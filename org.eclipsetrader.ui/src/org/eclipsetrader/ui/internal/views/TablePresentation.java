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

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IViewItem;

public class TablePresentation implements IWatchListViewerPresentation {
	private int sortColumn = 0;
	private int sortDirection = SWT.UP;
	private IDialogSettings dialogSettings;
	private TableViewer viewer;

	private ControlAdapter columnControlListener = new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {
        	TableColumn tableColumn = (TableColumn) e.widget;
        	if (dialogSettings != null) {
        		IDialogSettings columnsSection = dialogSettings.getSection("columns");
        		if (columnsSection == null)
        			columnsSection = dialogSettings.addNewSection("columns");
        		columnsSection.put(tableColumn.getText(), tableColumn.getWidth());
        	}
        }
	};

	private SelectionAdapter columnSelectionAdapter = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
        	TableColumn tableColumn = (TableColumn) e.widget;
        	Table table = tableColumn.getParent();

        	sortColumn = tableColumn.getParent().indexOf(tableColumn);
        	if (table.getSortColumn() == tableColumn)
            	sortDirection = sortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
            else {
            	sortDirection = SWT.UP;
            	table.setSortColumn(table.getColumn(sortColumn));
            }
        	table.setSortDirection(sortDirection);

        	IColumn column = (IColumn) tableColumn.getData();
            dialogSettings.put("sortColumn", column.getDataProviderFactory().getId());
            dialogSettings.put("sortDirection", sortDirection == SWT.UP ? 1 : -1);
            viewer.refresh();
        }
	};

	public TablePresentation(Composite parent, IDialogSettings dialogSettings) {
		this.dialogSettings = dialogSettings;

		viewer = createViewer(parent);
		sortDirection = dialogSettings != null && dialogSettings.get("sortDirection") != null ? (dialogSettings.getInt("sortDirection") == -1 ? SWT.DOWN : SWT.UP) : SWT.UP;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.views.IWatchListViewerPresentation#getViewer()
     */
	public StructuredViewer getViewer() {
    	return viewer;
    }

	protected TableViewer createViewer(Composite parent) {
    	Composite container = new Composite(parent, SWT.NONE);
		TableColumnLayout tableLayout = new TableColumnLayout();
		container.setLayout(tableLayout);

		TableViewer viewer = new TableViewer(container, SWT.MULTI | SWT.FULL_SELECTION) {
            @Override
            protected void inputChanged(Object input, Object oldInput) {
	            super.inputChanged(input, oldInput);
	    		if (sortColumn >= getTable().getColumnCount()) {
	    			sortColumn = 0;
	    			sortDirection = SWT.UP;
	    		}
	    		getTable().setSortDirection(sortDirection);
	    		getTable().setSortColumn(getTable().getColumn(sortColumn));
            }
		};
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		viewer.setUseHashlookup(true);

		viewer.setLabelProvider(new ViewItemLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	IAdaptable[] v1 = ((IViewItem) e1).getValues();
            	IAdaptable[] v2 = ((IViewItem) e2).getValues();
            	if (sortDirection == SWT.DOWN) {
                	v1 = ((IViewItem) e2).getValues();
                	v2 = ((IViewItem) e1).getValues();
            	}
            	return compareValues(v1, v2, sortColumn);
            }
		});

		return viewer;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.views.IWatchListViewerPresentation#dispose()
     */
    public void dispose() {
    	viewer.getControl().getParent().dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.views.IWatchListViewerPresentation#updateColumns(org.eclipsetrader.core.views.IColumn[])
     */
	public void updateColumns(IColumn[] columns) {
		if (viewer.getControl() instanceof Table)
			viewer.setColumnProperties(createColumns((Table) viewer.getControl(), columns));
	}

    @SuppressWarnings("unchecked")
	protected String[] createColumns(Table table, IColumn[] columns) {
		TableColumnLayout tableLayout = (TableColumnLayout) table.getParent().getLayout();
		IDialogSettings columnsSection = dialogSettings != null ? dialogSettings.getSection("columns") : null;

		String[] properties = new String[columns.length];

		int index = 0;
		for (IColumn column : columns) {
			int alignment = SWT.LEFT;
			if (column.getDataProviderFactory().getType() != null && column.getDataProviderFactory().getType().length != 0) {
				Class type = column.getDataProviderFactory().getType()[0];
				if (type == Long.class || type == Double.class || type == Date.class)
					alignment = SWT.RIGHT;
			}
			TableColumn tableColumn = index < table.getColumnCount() ? table.getColumn(index) : new TableColumn(table, SWT.NONE);
			tableColumn.setAlignment(alignment);
			tableColumn.setText(column.getName() != null ? column.getName() : column.getDataProviderFactory().getName());
			tableColumn.setData(column);

			properties[index] = String.valueOf(index);

			if ("org.eclipsetrader.ui.providers.SecurityName".equals(column.getDataProviderFactory().getId()))
				tableLayout.setColumnData(tableColumn, new ColumnWeightData(100));
			else {
				int width = columnsSection != null && columnsSection.get(tableColumn.getText()) != null ? columnsSection.getInt(tableColumn.getText()) : 100;
				tableLayout.setColumnData(tableColumn, new ColumnPixelData(width));
			}

			tableColumn.addControlListener(columnControlListener);
			tableColumn.addSelectionListener(columnSelectionAdapter);

			if (dialogSettings != null) {
				if (column.getDataProviderFactory().getId().equals(dialogSettings.get("sortColumn")))
					sortColumn = index;
			}

			index++;
		}

		while(table.getColumnCount() > index)
			table.getColumn(table.getColumnCount() - 1).dispose();

		return properties;
	}

    @SuppressWarnings("unchecked")
    protected int compareValues(IAdaptable[] v1, IAdaptable[] v2, int sortColumn) {
    	if (sortColumn < 0 || sortColumn >= v1.length || sortColumn >= v2.length)
    		return 0;
    	if (v1[sortColumn] == null || v2[sortColumn] == null)
    		return 0;

    	Object o1 = v1[sortColumn].getAdapter(Comparable.class);
    	Object o2 = v2[sortColumn].getAdapter(Comparable.class);
    	if (o1 != null && o2 != null)
    		return ((Comparable) o1).compareTo(o2);

    	o1 = v1[sortColumn].getAdapter(Number.class);
    	o2 = v2[sortColumn].getAdapter(Number.class);
    	if (o1 != null && o2 != null) {
    		if (((Number) o1).doubleValue() < ((Number) o2).doubleValue())
    			return -1;
    		if (((Number) o1).doubleValue() > ((Number) o2).doubleValue())
    			return 1;
    		return 0;
    	}

    	return 0;
    }

    Table getTable() {
    	return viewer.getTable();
    }
}
