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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GeneralPage extends WizardPage {
	Text name;

	ComboViewer account;

	ComboViewer tradingProvider;

	public GeneralPage() {
		super("");
		setTitle("General");
		setDescription("Set the name, account and trading provider");
		setPageComplete(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		setControl(content);

		Label label = new Label(content, SWT.NONE);
		label.setText("Name");
		label.setLayoutData(new GridData(127, SWT.DEFAULT));
		name = new Text(content, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(isPageComplete());
			}
		});

		label = new Label(content, SWT.NONE);
		label.setText("Account");
		label.setLayoutData(new GridData(127, SWT.DEFAULT));
		account = new ComboViewer(content, SWT.READ_ONLY);
		account.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		account.setContentProvider(new ArrayContentProvider());
		account.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Account) element).getDescription();
			}
		});
		account.setInput(CorePlugin.getRepository().allAccounts().toArray());
		account.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(isPageComplete());
			}
		});

		label = new Label(content, SWT.NONE);
		label.setText("Trading Provider");
		label.setLayoutData(new GridData(127, SWT.DEFAULT));
		tradingProvider = new ComboViewer(content, SWT.READ_ONLY);
		tradingProvider.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		tradingProvider.setContentProvider(new ArrayContentProvider());
		tradingProvider.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((IConfigurationElement) element).getAttribute("name");
			}
		});
		tradingProvider.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(isPageComplete());
			}
		});

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.TRADING_PROVIDERS_EXTENSION_POINT);
		if (extensionPoint != null)
			tradingProvider.setInput(extensionPoint.getConfigurationElements());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		if (name.getText().length() == 0)
			return false;
		if (account.getSelection() == null || account.getSelection().isEmpty())
			return false;
		if (tradingProvider.getSelection() == null || tradingProvider.getSelection().isEmpty())
			return false;

		return true;
	}
}
