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

package org.eclipsetrader.news.internal.ui;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.internal.Activator;

public class HeadLineViewer extends ViewPart {
	private static final String K_COLUMNS = "columns";

	private TableViewer viewer;

	private IDialogSettings dialogSettings;

	private ControlAdapter controlListener = new ControlAdapter() {
		@Override
        public void controlResized(ControlEvent e) {
			TableColumn tableColumn = (TableColumn) e.widget;
			int index = viewer.getTable().indexOf(tableColumn);

        	if (dialogSettings != null) {
        		IDialogSettings columnsSection = dialogSettings.getSection(K_COLUMNS);
        		if (columnsSection == null)
        			columnsSection = dialogSettings.addNewSection(K_COLUMNS);
        		columnsSection.put(String.valueOf(index), tableColumn.getWidth());
        	}
		}
	};

	public HeadLineViewer() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);

	    IDialogSettings bundleDialogSettings = Activator.getDefault().getDialogSettings();
	    dialogSettings = bundleDialogSettings.getSection(getClass().getName());
	    if (dialogSettings == null)
	    	dialogSettings = bundleDialogSettings.addNewSection(getClass().getName());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected void createViewer(Composite parent) {
    	Composite container = new Composite(parent, SWT.NONE);
		TableColumnLayout tableLayout = new TableColumnLayout();
		container.setLayout(tableLayout);

		viewer = new TableViewer(container, SWT.MULTI | SWT.FULL_SELECTION);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);

		IDialogSettings columnsSection = dialogSettings != null ? dialogSettings.getSection(K_COLUMNS) : null;

		TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Date");
		tableColumn.addControlListener(controlListener);
		tableLayout.setColumnData(tableColumn, new ColumnPixelData(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 70));

		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Title");
		tableColumn.addControlListener(controlListener);
		tableLayout.setColumnData(tableColumn, new ColumnWeightData(100));

		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Security");
		tableColumn.addControlListener(controlListener);
		tableLayout.setColumnData(tableColumn, new ColumnPixelData(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 100));

		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Source");
		tableColumn.addControlListener(controlListener);
		tableLayout.setColumnData(tableColumn, new ColumnPixelData(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 100));

		viewer.setLabelProvider(new HeadLineLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
	            return ((IHeadLine) e1).getDate().compareTo(((IHeadLine) e2).getDate());
            }
		});
	}
}
