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

package org.eclipsetrader.ui.internal.markets;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.jface.cdatetime.CDateTimeCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipsetrader.core.internal.markets.MarketTimeExclude;
import org.eclipsetrader.ui.internal.markets.MarketTimeElement.MarketTimeExcludeElement;

public class ExclusionScheduleEditor {
	private Composite content;
	private TableViewer viewer;
	private Button add;
	private Button remove;

	private List<MarketTimeExcludeElement> input = new ArrayList<MarketTimeExcludeElement>();

	private class ScheduleElementLabelProvider extends LabelProvider implements ITableLabelProvider {
		private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);

		public ScheduleElementLabelProvider() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof MarketTimeExcludeElement) {
				MarketTimeExcludeElement e = (MarketTimeExcludeElement) element;
				switch (columnIndex) {
					case 0:
						return dateFormat.format(e.getFromDate());
					case 1:
						return dateFormat.format(e.getToDate());
				}
			}
			return ""; //$NON-NLS-1$
		}
	}

	protected ExclusionScheduleEditor() {
	}

	public ExclusionScheduleEditor(Composite parent) {
		content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createViewer(content);
		createButtons(content);
	}

	protected void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.heightHint = viewer.getTable().getItemHeight() * 5 + viewer.getTable().getBorderWidth() * 2;
		viewer.getControl().setLayoutData(gridData);

		TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("From");
		tableColumn.setWidth(70);
		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("To");
		tableColumn.setWidth(70);

		viewer.setLabelProvider(new ScheduleElementLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((MarketTimeExcludeElement) e1).compareTo((MarketTimeExcludeElement) e2);
			}
		});

		viewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return true;
			}

			public Object getValue(Object element, String property) {
				MarketTimeExcludeElement e = (MarketTimeExcludeElement) element;
				int columnIndex = Integer.valueOf(property);
				switch (columnIndex) {
					case 0:
						return e.getFromDate();
					case 1:
						return e.getToDate();
				}
				return "";
			}

			public void modify(Object element, String property, Object value) {
				MarketTimeExcludeElement e = (MarketTimeExcludeElement) (element instanceof TableItem ? ((TableItem) element).getData() : element);
				int columnIndex = Integer.valueOf(property);
				switch (columnIndex) {
					case 0:
						e.setFromDate(normalizeDate((Date) value));
						break;
					case 1:
						e.setToDate(normalizeDate((Date) value));
						break;
				}
				viewer.refresh();
			}

			private Date normalizeDate(Date date) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				return calendar.getTime();
			}
		});
		viewer.setColumnProperties(new String[] {
		    "0", "1"
		});
		viewer.setCellEditors(new CellEditor[] {
		    new CDateTimeCellEditor(viewer.getTable(), CDT.DATE_SHORT), new CDateTimeCellEditor(viewer.getTable(), CDT.DATE_SHORT),
		});

		viewer.setInput(input);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (remove != null)
					remove.setEnabled(!event.getSelection().isEmpty());
			}
		});
	}

	protected void createButtons(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		add = new Button(content, SWT.PUSH);
		add.setText("Add");
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				MarketTimeExcludeElement element = new MarketTimeExcludeElement(calendar.getTime(), calendar.getTime());
				input.add(element);
				viewer.refresh();
				viewer.getControl().setFocus();
				viewer.setSelection(new StructuredSelection(element), true);
				viewer.editElement(element, 0);
			}
		});
		add.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		remove = new Button(content, SWT.PUSH);
		remove.setText("Remove");
		remove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					input.removeAll(selection.toList());
					viewer.refresh();
				}
			}
		});
		remove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		remove.setEnabled(false);
	}

	public void setSchedule(MarketTimeExclude[] schedule) {
		input.clear();
		for (MarketTimeExclude time : schedule)
			input.add(new MarketTimeExcludeElement(time));
		this.viewer.refresh();
	}

	public MarketTimeExclude[] getSchedule() {
		MarketTimeExclude[] result = new MarketTimeExclude[input.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = input.get(i).getMarketTimeExclude();
		return result;
	}

	public Control getControl() {
		return content;
	}

	protected TableViewer getViewer() {
		return viewer;
	}

	protected List<MarketTimeExcludeElement> getInput() {
		return input;
	}

	public void setInput(List<MarketTimeExcludeElement> input) {
		this.input = input;
		this.viewer.setInput(this.input);
	}
}
