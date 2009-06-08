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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipsetrader.core.internal.trading.TargetPrice;

public class TargetPriceWizardPage extends WizardPage {
	Combo field;
	Spinner value;
	Button cross;
	Label description;

	private SelectionAdapter selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			description.setText(getDescriptionText());
		}
	};

	public TargetPriceWizardPage() {
		super("general", Messages.TargetPriceWizardPage_Title, null); //$NON-NLS-1$
		setDescription(Messages.TargetPriceWizardPage_Description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);

		initializeDialogUnits(content);
		setControl(content);

		Label label = new Label(content, SWT.NONE);
		label.setText(Messages.TargetPriceWizardPage_PriceFieldLabel);
		label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(90), SWT.DEFAULT));
		field = new Combo(content, SWT.READ_ONLY | SWT.DROP_DOWN);
		field.setItems(new String[] {
		    Messages.TargetPriceWizardPage_LastFieldText, Messages.TargetPriceWizardPage_BidFieldText, Messages.TargetPriceWizardPage_AskFieldText
		});
		field.select(0);
		field.addSelectionListener(selectionListener);

		label = new Label(content, SWT.NONE);
		label.setText(Messages.TargetPriceWizardPage_ValueLabel);
		value = new Spinner(content, SWT.BORDER);
		value.setDigits(4);
		value.setMinimum(0);
		value.setMaximum(99999999);
		value.addSelectionListener(selectionListener);

		label = new Label(content, SWT.NONE);
		cross = new Button(content, SWT.CHECK);
		cross.setText(Messages.TargetPriceWizardPage_TriggerIfCrossedLabel);
		cross.addSelectionListener(selectionListener);

		label = new Label(content, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		description = new Label(content, SWT.NONE);
		description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	}

	public Map<String, Object> getParametersMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TargetPrice.K_FIELD, field.getSelectionIndex());
		map.put(TargetPrice.K_PRICE, value.getSelection() / Math.pow(10.0, value.getDigits()));
		map.put(TargetPrice.K_CROSS, cross.getSelection());
		return map;
	}

	String getDescriptionText() {
		return TargetPrice.getDescriptionFor(getParametersMap());
	}
}
