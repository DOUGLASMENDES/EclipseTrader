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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipsetrader.internal.brokers.paper.schemes.LimitedProportional1Scheme;
import org.eclipsetrader.internal.brokers.paper.schemes.LimitedProportional2Scheme;
import org.eclipsetrader.internal.brokers.paper.schemes.NoExpensesScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.SimpleFixedScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.TwoLevelsPerShareScheme;

public class SettingsPage extends WizardPage {

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

    public SettingsPage() {
        super("settings");
        setTitle("Settings");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
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
        currency.setSelection(new StructuredSelection(Currency.getInstance(Locale.getDefault())));

        label = new Label(composite, SWT.NONE);
        label.setText("Initial Balance");
        balance = new Spinner(composite, SWT.BORDER);
        balance.setValues(0, 0, 999999999, 2, 10000, 100000);

        label = new Label(composite, SWT.NONE);
        label.setText("Expenses Scheme");
        expenses = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        expenses.setLabelProvider(new LabelProvider());
        expenses.setContentProvider(new ArrayContentProvider());
        expenses.setInput(availableSchemes);
        expenses.setSelection(new StructuredSelection(availableSchemes[0]));

        setControl(composite);
    }

    public Currency getCurrency() {
        return (Currency) ((IStructuredSelection) currency.getSelection()).getFirstElement();
    }

    public IExpenseScheme getExpenseScheme() {
        return (IExpenseScheme) ((IStructuredSelection) expenses.getSelection()).getFirstElement();
    }

    public Double getBalance() {
        return balance.getSelection() / Math.pow(10, balance.getDigits());
    }
}
