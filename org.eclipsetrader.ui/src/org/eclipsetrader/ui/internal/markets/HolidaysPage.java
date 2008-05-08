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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketHoliday;

public class HolidaysPage extends PropertyPage {
	TableViewer viewer;
	Button add;
	Button edit;
	Button remove;

	List<MarketHolidayElement> input;

	public HolidaysPage() {
		setTitle("Holidays");
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		((GridLayout) content.getLayout()).marginWidth = 0;
		((GridLayout) content.getLayout()).marginHeight = 0;

		createViewer(content);
		createButtons(content);

		input = new ArrayList<MarketHolidayElement>();
		if (getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				for (MarketHoliday day : market.getHolidays())
					input.add(new MarketHolidayElement(day));
			}
		}
		viewer.setInput(input);

		updateButtonsEnablement();

		return content;
	}

	protected void createViewer(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		TableColumnLayout tableLayout = new TableColumnLayout();
		content.setLayout(tableLayout);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		viewer = new TableViewer(content, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData) viewer.getControl().getLayoutData()).heightHint = viewer.getTable().getItemHeight() * 8 + viewer.getTable().getBorderWidth() * 2;

		TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.RIGHT);
		tableColumn.setText("Date");
		tableLayout.setColumnData(tableColumn, new ColumnPixelData(convertHorizontalDLUsToPixels(60)));
		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Description");
		tableLayout.setColumnData(tableColumn, new ColumnWeightData(100));

		viewer.setLabelProvider(new MarketHolidayLabelProvider());
		viewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	return ((MarketHolidayElement) e1).getDate().compareTo(((MarketHolidayElement) e2).getDate());
            }
		});
		viewer.setContentProvider(new ArrayContentProvider());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
        		updateButtonsEnablement();
            }
		});
	}

	protected void createButtons(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		((GridLayout) content.getLayout()).marginWidth = 0;
		((GridLayout) content.getLayout()).marginHeight = 0;
		content.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		add = new Button(content, SWT.PUSH);
		add.setText("Add");
		add.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		((GridData) add.getLayoutData()).widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	HolidayDialog dlg = new HolidayDialog(add.getShell(), null);
            	if (dlg.open() == HolidayDialog.OK) {
            		input.add(dlg.getElement());
            		viewer.refresh();
            	}
            }
		});

		edit = new Button(content, SWT.PUSH);
		edit.setText("Edit");
		edit.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		((GridData) edit.getLayoutData()).widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		edit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            	if (!selection.isEmpty()) {
            		MarketHolidayElement element = (MarketHolidayElement) selection.getFirstElement();
                	HolidayDialog dlg = new HolidayDialog(edit.getShell(), element);
                	if (dlg.open() == HolidayDialog.OK)
                		viewer.refresh();
            	}
            }
		});

		remove = new Button(content, SWT.PUSH);
		remove.setText("Remove");
		remove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
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
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
		if (isControlCreated() && getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				MarketHoliday[] holidays = new MarketHoliday[input.size()];
				for (int i = 0; i < input.size(); i++)
					holidays[i] = input.get(i).getMarketHoliday();
				market.setHolidays(holidays);
			}
		}
	    return super.performOk();
    }

    protected void updateButtonsEnablement() {
    	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    	edit.setEnabled(selection.size() == 1);
    	remove.setEnabled(!selection.isEmpty());
    }
}
