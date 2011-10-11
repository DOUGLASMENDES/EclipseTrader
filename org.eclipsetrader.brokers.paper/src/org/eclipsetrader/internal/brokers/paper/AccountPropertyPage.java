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

package org.eclipsetrader.internal.brokers.paper;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.internal.brokers.paper.schemes.LimitedProportional1Scheme;
import org.eclipsetrader.internal.brokers.paper.schemes.LimitedProportional2Scheme;
import org.eclipsetrader.internal.brokers.paper.schemes.NoExpensesScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.SimpleFixedScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.TwoLevelsPerShareScheme;

public class AccountPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    Text name;
    ComboViewer currency;
    ComboViewer expenses;
    Spinner balance;

    private IExpenseScheme[] availableSchemes = new IExpenseScheme[] {
        new NoExpensesScheme(),
        new SimpleFixedScheme(),
        new LimitedProportional1Scheme(),
        new LimitedProportional2Scheme(),
        new TwoLevelsPerShareScheme(),
    };

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            setValid(isValid());
        }
    };

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            setValid(isValid());
        }
    };

    public AccountPropertyPage() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Name");
        name = new Text(composite, SWT.BORDER);
        name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(composite, SWT.NONE);
        label.setText("Currency");
        currency = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        currency.setLabelProvider(new LabelProvider());
        currency.setContentProvider(new ArrayContentProvider());
        currency.setSorter(new ViewerSorter());
        Locale[] l = Locale.getAvailableLocales();
        Set<Currency> c = new HashSet<Currency>();
        for (int i = 0; i < l.length; i++) {
            try {
                c.add(Currency.getInstance(l[i]));
            } catch (Exception e) {
                // Ignore, some locales may throw an exception
            }
        }
        currency.setInput(c.toArray());

        label = new Label(composite, SWT.NONE);
        label.setText("Balance");
        balance = new Spinner(composite, SWT.BORDER);
        balance.setValues(0, 0, 999999999, 2, 10000, 100000);

        label = new Label(composite, SWT.NONE);
        label.setText("Expenses Scheme");
        expenses = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        expenses.setLabelProvider(new LabelProvider());
        expenses.setContentProvider(new ArrayContentProvider());
        expenses.setInput(availableSchemes);

        name.addModifyListener(modifyListener);
        currency.addSelectionChangedListener(selectionChangedListener);
        balance.addModifyListener(modifyListener);
        expenses.addSelectionChangedListener(selectionChangedListener);

        performDefaults();

        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        Account account = (Account) getElement().getAdapter(Account.class);

        name.setText(account.getDescription());
        if (account.getCurrency() != null) {
            currency.setSelection(new StructuredSelection(account.getCurrency()));
        }
        balance.setSelection((int) (account.getBalance().getAmount() * Math.pow(10, balance.getDigits())));
        if (account.getExpenseScheme() != null) {
            expenses.setSelection(new StructuredSelection(account.getExpenseScheme()));
        }

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        if (name.getText().equals("")) {
            return false;
        }
        return super.isValid();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        Account account = (Account) getElement().getAdapter(Account.class);

        account.setDescription(name.getText());
        account.setCurrency((Currency) ((IStructuredSelection) currency.getSelection()).getFirstElement());
        account.setBalance(balance.getSelection() / Math.pow(10, balance.getDigits()));
        account.setExpenseScheme((IExpenseScheme) ((IStructuredSelection) expenses.getSelection()).getFirstElement());

        return super.performOk();
    }
}
