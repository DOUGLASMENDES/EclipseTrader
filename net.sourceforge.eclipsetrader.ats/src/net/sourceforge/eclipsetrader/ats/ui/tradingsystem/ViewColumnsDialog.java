/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ViewColumnsDialog extends Dialog {
	TableViewer available;

	TableViewer selected;

	Button moveToSelected;

	Button moveAllToSelected;

	Button moveToAvailable;

	Button moveAllToAvailable;

	Button moveUp;

	Button moveDown;

	List availableElements = new ArrayList();

	List selectedElements = new ArrayList();

	LabelProvider labelProvider = new LabelProvider() {
		public String getText(Object element) {
			return ((IConfigurationElement) element).getAttribute("name");
		}
	};

	Comparator comparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			return ((IConfigurationElement) o1).getAttribute("name").compareTo(((IConfigurationElement) o2).getAttribute("name"));
		}
	};

	public ViewColumnsDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		newShell.setText("Configure Columns");
		super.configureShell(newShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite content = (Composite) super.createDialogArea(parent);
		((GridLayout) content.getLayout()).numColumns = 2;
		((GridLayout) content.getLayout()).makeColumnsEqualWidth = true;

		Label label = new Label(content, SWT.NONE);
		label.setText("Available columns");

		label = new Label(content, SWT.NONE);
		label.setText("Selected columns");

		Composite column = new Composite(content, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		column.setLayout(gridLayout);
		column.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Table table = new Table(column, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		gridData.heightHint = table.getItemHeight() * 15;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		table.setLayoutData(gridData);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		available = new TableViewer(table);
		available.setContentProvider(new ArrayContentProvider());
		available.setLabelProvider(labelProvider);
		available.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsEnablement();
			}
		});

		Composite buttons = new Composite(column, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		buttons.setLayout(gridLayout);
		buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		moveToSelected = new Button(buttons, SWT.PUSH);
		moveToSelected.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/right.gif").createImage());
		moveToSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveHorizontally(available, selected, false);
			}
		});

		moveAllToSelected = new Button(buttons, SWT.PUSH);
		moveAllToSelected.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/all-right.gif").createImage());
		moveAllToSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveHorizontally(available, selected, true);
			}
		});

		moveAllToAvailable = new Button(buttons, SWT.PUSH);
		moveAllToAvailable.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/all-left.gif").createImage());
		moveAllToAvailable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveHorizontally(selected, available, true);
			}
		});

		moveToAvailable = new Button(buttons, SWT.PUSH);
		moveToAvailable.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/left.gif").createImage());
		moveToAvailable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveHorizontally(selected, available, false);
			}
		});

		column = new Composite(content, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		column.setLayout(gridLayout);
		column.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		table = new Table(column, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 150;
		gridData.heightHint = table.getItemHeight() * 15;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		table.setLayoutData(gridData);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		selected = new TableViewer(table);
		selected.setContentProvider(new ArrayContentProvider());
		selected.setLabelProvider(labelProvider);
		selected.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsEnablement();
			}
		});

		buttons = new Composite(column, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		buttons.setLayout(gridLayout);
		buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		moveUp = new Button(buttons, SWT.PUSH);
		moveUp.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/up.gif").createImage());
		moveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveVertically(selected, true);
			}
		});

		moveDown = new Button(buttons, SWT.PUSH);
		moveDown.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/down.gif").createImage());
		moveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveVertically(selected, false);
			}
		});

		availableElements = getAvailableElements();

		available.setInput(availableElements);
		selected.setInput(selectedElements);
		updateButtonsEnablement();

		return content;
	}

	protected List getAvailableElements() {
		List list = new ArrayList();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.LABEL_PROVIDERS_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				if (TradingSystemView.VIEW_ID.equals(members[i].getAttribute("targetID")))
					list.addAll(Arrays.asList(members[i].getChildren()));
			}
		}

		list.removeAll(selectedElements);
		Collections.sort(list, comparator);

		return list;
	}

	public void setSelectedElements(IConfigurationElement[] elements) {
		selectedElements = new ArrayList(Arrays.asList(elements));
	}

	public List getSelectedElements() {
		return selectedElements;
	}

	protected void updateButtonsEnablement() {
		moveToSelected.setEnabled(((IStructuredSelection) available.getSelection()).size() != 0);
		moveAllToSelected.setEnabled(availableElements.size() != 0);
		moveToAvailable.setEnabled(((IStructuredSelection) selected.getSelection()).size() != 0);
		moveAllToAvailable.setEnabled(selectedElements.size() != 0);
		moveUp.setEnabled(selectedElements.size() > 1 && ((IStructuredSelection) selected.getSelection()).size() == 1);
		moveDown.setEnabled(selectedElements.size() > 1 && ((IStructuredSelection) selected.getSelection()).size() == 1);
	}

	protected void moveHorizontally(TableViewer from, TableViewer to, boolean moveAll) {
		List selection = ((IStructuredSelection) from.getSelection()).toList();
		if (moveAll)
			selection = (List) from.getInput();

		((List) to.getInput()).addAll(selection);
		((List) from.getInput()).removeAll(selection);

		if (to == available)
			Collections.sort((List) to.getInput(), comparator);

		from.refresh();
		to.refresh();
		updateButtonsEnablement();
	}

	protected void moveVertically(TableViewer from, boolean moveUp) {
		Object element = ((IStructuredSelection) from.getSelection()).getFirstElement();
		List list = (List) from.getInput();
		int index = list.indexOf(element);
		if (index == 0 && moveUp)
			return;
		if (index == (list.size() - 1) && !moveUp)
			return;
		list.remove(index);
		index = moveUp ? (index - 1) : (index + 1);
		list.add(index, element);

		from.refresh();
		updateButtonsEnablement();
	}
}
