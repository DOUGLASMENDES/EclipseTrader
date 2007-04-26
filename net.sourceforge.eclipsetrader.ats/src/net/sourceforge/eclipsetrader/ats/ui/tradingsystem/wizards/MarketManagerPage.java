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

import net.sourceforge.eclipsetrader.ats.ATSPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MarketManagerPage extends WizardPage {
	ListViewer list;

	public MarketManagerPage() {
		super("");
		setTitle("Market Manager");
		setDescription("Select the market manager to use");
		setPageComplete(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		setControl(content);

		list = new ListViewer(content, SWT.BORDER);
		list.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		list.setContentProvider(new ArrayContentProvider());
		list.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((IConfigurationElement) element).getAttribute("name");
			}
		});
		list.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(isPageComplete());
			}
		});

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(ATSPlugin.COMPONENTS_EXTENSION_ID);
		if (extensionPoint != null) {
			ArrayList input = new ArrayList();
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("market"))
					input.add(elements[i]);
			}
			list.setInput(input.toArray());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		if (getSelection() == null)
			return false;

		return true;
	}

	public IConfigurationElement getSelection() {
		IStructuredSelection selection = (IStructuredSelection) list.getSelection();
		if (!selection.isEmpty())
			return (IConfigurationElement) selection.getFirstElement();
		return null;
	}
}
