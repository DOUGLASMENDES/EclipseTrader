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

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem.wizards;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SecuritySelectionPage extends WizardPage {
	ListViewer available;

	Button toSelected;

	Button toAvailable;

	ListViewer selected;

	Collection availableItems;

	Collection selectedItems = new ArrayList();

	ViewerComparator comparator = new ViewerComparator() {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Security) e1).getDescription().compareTo(((Security) e2).getDescription());
		}
	};

	public SecuritySelectionPage() {
		super("");
		setTitle("Securities");
		setDescription("Select the securities to trade");
		setPageComplete(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		setControl(content);

		available = new ListViewer(content, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.widthHint = 200;
		gridData.heightHint = 250;
		available.getControl().setLayoutData(gridData);
		available.setContentProvider(new ArrayContentProvider());
		available.setLabelProvider(new LabelProvider());
		available.setComparator(comparator);
		available.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				toSelected.setEnabled(event.getSelection() != null && !event.getSelection().isEmpty());
			}
		});

		Composite buttons = new Composite(content, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		buttons.setLayout(gridLayout);
		buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		toSelected = new Button(buttons, SWT.PUSH);
		toSelected.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/right.gif").createImage());
		toSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) available.getSelection();
				if (selection != null && !selection.isEmpty()) {
					selectedItems.addAll(selection.toList());
					availableItems.removeAll(selection.toList());
					selected.add(selection.toArray());
					available.remove(selection.toArray());
				}
			}
		});
		toSelected.setEnabled(false);

		Button button = new Button(buttons, SWT.PUSH);
		button.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/all-right.gif").createImage());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selected.add(availableItems.toArray());
				available.remove(availableItems.toArray());
				selectedItems.addAll(availableItems);
				availableItems.clear();
			}
		});

		button = new Button(buttons, SWT.PUSH);
		button.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/all-left.gif").createImage());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selected.remove(selectedItems.toArray());
				available.add(selectedItems.toArray());
				availableItems.addAll(selectedItems);
				selectedItems.clear();
			}
		});

		toAvailable = new Button(buttons, SWT.PUSH);
		toAvailable.setImage(ATSPlugin.getImageDescriptor("icons/buttons16/left.gif").createImage());
		toAvailable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) selected.getSelection();
				if (selection != null && !selection.isEmpty()) {
					selectedItems.removeAll(selection.toList());
					availableItems.addAll(selection.toList());
					available.add(selection.toArray());
					selected.remove(selection.toArray());
				}
			}
		});
		toAvailable.setEnabled(false);

		selected = new ListViewer(content, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.widthHint = 200;
		gridData.heightHint = 250;
		selected.getControl().setLayoutData(gridData);
		selected.setContentProvider(new ArrayContentProvider());
		selected.setComparator(comparator);
		selected.setLabelProvider(new LabelProvider());
		selected.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				toAvailable.setEnabled(event.getSelection() != null && !event.getSelection().isEmpty());
			}
		});

		availableItems = new ArrayList(CorePlugin.getRepository().allSecurities());
		available.setInput(availableItems.toArray());
	}

	public Collection getSelectedItems() {
		return selectedItems;
	}
}
