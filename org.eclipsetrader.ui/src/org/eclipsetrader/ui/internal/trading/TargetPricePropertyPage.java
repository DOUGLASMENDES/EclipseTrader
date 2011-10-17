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

package org.eclipsetrader.ui.internal.trading;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.internal.trading.TargetPrice;

public class TargetPricePropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

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

    public TargetPricePropertyPage() {
        setTitle(Messages.TargetPricePropertyPage_Title);
        noDefaultAndApplyButton();
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

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.TargetPricePropertyPage_PriceField);
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(50), SWT.DEFAULT));
        field = new Combo(content, SWT.READ_ONLY | SWT.DROP_DOWN);
        field.setItems(new String[] {
                Messages.TargetPricePropertyPage_LastFieldText,
                Messages.TargetPricePropertyPage_BidFieldText,
                Messages.TargetPricePropertyPage_AskFieldText
        });
        field.select(0);
        field.addSelectionListener(selectionListener);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.TargetPricePropertyPage_ValueLabel);
        value = new Spinner(content, SWT.BORDER);
        value.setDigits(4);
        value.setMinimum(0);
        value.setMaximum(99999999);
        value.addSelectionListener(selectionListener);

        label = new Label(content, SWT.NONE);
        cross = new Button(content, SWT.CHECK);
        cross.setText(Messages.TargetPricePropertyPage_TriggerIfCrossedLabel);
        cross.addSelectionListener(selectionListener);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        description = new Label(content, SWT.NONE);
        description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        TargetPrice element = (TargetPrice) getElement().getAdapter(TargetPrice.class);
        Map<String, Object> map = element.getParameters();

        field.select((Integer) map.get(TargetPrice.K_FIELD));
        value.setSelection((int) ((Double) map.get(TargetPrice.K_PRICE) * Math.pow(10.0, value.getDigits())));
        cross.setSelection((Boolean) map.get(TargetPrice.K_CROSS));

        description.setText(getDescriptionText());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (isControlCreated()) {
            TargetPrice element = (TargetPrice) getElement().getAdapter(TargetPrice.class);
            element.setParameters(getParametersMap());
        }
        return super.performOk();
    }

    Map<String, Object> getParametersMap() {
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
