/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.ui.trading;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public class AlertSelectionPage extends WizardSelectionPage {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private TableViewer viewer;

	public AlertSelectionPage(IWorkbench workbench, IStructuredSelection selection) {
		super("selection"); //$NON-NLS-1$

		this.workbench = workbench;
		this.selection = selection;

		setTitle(Messages.AlertSelectionPage_Title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput(getContributionItems());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				setSelectedNode((IWizardNode) (selection.isEmpty() ? null : selection.getFirstElement()));
			}
		});
		setControl(viewer.getControl());
	}

	AlertWizardNode[] getContributionItems() {
		List<AlertWizardNode> list = new ArrayList<AlertWizardNode>();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.newWizards"); //$NON-NLS-1$
		IConfigurationElement[] element = extensionPoint.getConfigurationElements();
		for (int i = 0; i < element.length; i++) {
			String category = element[i].getAttribute("category"); //$NON-NLS-1$
			if ("org.eclipsetrader.ui.trading.alerts".equals(category)) //$NON-NLS-1$
				list.add(new AlertWizardNode(element[i], workbench, selection));
		}

		return list.toArray(new AlertWizardNode[list.size()]);
	}
}
