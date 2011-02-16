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

package org.eclipsetrader.ui.internal.securities.properties;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.feed.Dividend;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.ui.DateCellEditor;
import org.eclipsetrader.ui.DoubleCellEditor;
import org.eclipsetrader.ui.Util;

public class DividendsProperties extends PropertyPage implements IWorkbenchPropertyPage {
	TableViewer viewer;
	Button add;
	Button remove;

	List<IDividend> input;
	DateFormat dateFormat = Util.getDateFormat();
	NumberFormat numberFormat = NumberFormat.getInstance();

	class DividendLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			IDividend dividend = (IDividend) element;

			switch (columnIndex) {
				case 0:
					return dateFormat.format(dividend.getExDate());
				case 1:
					return numberFormat.format(dividend.getValue());
			}

			return "";
		}
	}

	class DividendCellModifier implements ICellModifier {

		@Override
		public boolean canModify(Object element, String property) {
			return element instanceof Dividend;
		}

		@Override
		public Object getValue(Object element, String property) {
			Dividend dividend = (Dividend) element;

			if ("ex-date".equals(property))
				return dividend.getExDate();

			if ("amount".equals(property))
				return dividend.getValue();

			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			Dividend dividend = (Dividend) ((element instanceof Dividend) ? element : ((TableItem) element).getData());

			if ("ex-date".equals(property))
				dividend.setExDate((Date) value);
			else if ("amount".equals(property))
				dividend.setValue((Double) value);

			viewer.update(dividend, null);
		}
	}

	public DividendsProperties() {
		setTitle("Dividends");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		initializeDialogUnits(content);

		viewer = new TableViewer(content, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData) viewer.getControl().getLayoutData()).heightHint = viewer.getTable().getItemHeight() * 5;

		TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Ex. Date");
		tableColumn.setWidth(convertHorizontalDLUsToPixels(70));

		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Amount");
		tableColumn.setWidth(convertHorizontalDLUsToPixels(70));

		viewer.setLabelProvider(new DividendLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((IDividend) e1).getExDate().compareTo(((IDividend) e2).getExDate());
			}
		});

		viewer.setCellModifier(new DividendCellModifier());
		viewer.setColumnProperties(new String[] {
		    "ex-date", "amount"
		});
		viewer.setCellEditors(new CellEditor[] {
		    new DateCellEditor(viewer.getTable()), new DoubleCellEditor(viewer.getTable()),
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				remove.setEnabled(!event.getSelection().isEmpty());
			}
		});

		createButtonsGroup(content);

		performDefaults();

		return content;
	}

	void createButtonsGroup(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		add = new Button(content, SWT.PUSH);
		add.setText("Add");
		add.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		add.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				input.add(new Dividend(new Date(), 0.0));
				viewer.refresh();
			}
		});

		remove = new Button(content, SWT.PUSH);
		remove.setText("Remove");
		remove.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		remove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				input.removeAll(selection.toList());
				viewer.refresh();
			}
		});
		remove.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IStock security = (IStock) getElement().getAdapter(IStock.class);

		IDividend[] dividends = security.getDividends();
		input = new ArrayList<IDividend>(Arrays.asList(dividends));
		viewer.setInput(input);

		super.performDefaults();
	}

	protected void applyChanges() {
		Stock security = (Stock) getElement().getAdapter(Stock.class);

		security.setDividends(input.toArray(new IDividend[input.size()]));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (getControl() != null)
			applyChanges();
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		applyChanges();
		super.performApply();
	}
}
