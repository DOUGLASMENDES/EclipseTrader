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

package org.eclipsetrader.internal.brokers.paper;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.internal.brokers.paper.schemes.LimitedProportional1Scheme;
import org.eclipsetrader.internal.brokers.paper.schemes.LimitedProportional2Scheme;
import org.eclipsetrader.internal.brokers.paper.schemes.NoExpensesScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.SimpleFixedScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.TwoLevelsPerShareScheme;

public class AccountDialog extends Dialog {
	private Text name;
	private ComboViewer currency;
	private ComboViewer expenses;
	private Text initialBalance;

	private Account account;
	private NumberFormat formatter = NumberFormat.getInstance(Locale.US);

	private IExpenseScheme[] availableSchemes = new IExpenseScheme[] {
			new NoExpensesScheme(),
			new SimpleFixedScheme(),
			new LimitedProportional1Scheme(),
			new LimitedProportional2Scheme(),
			new TwoLevelsPerShareScheme(),
		};

	private ModifyListener modifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
        	getButton(IDialogConstants.OK_ID).setEnabled(isValid());
        }
	};

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
        	getButton(IDialogConstants.OK_ID).setEnabled(isValid());
        }
	};

	public AccountDialog(Shell parentShell, Account account) {
		super(parentShell);
		this.account = account;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Account");
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
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
			} catch(Exception e) {
				// Ignore, some locales may throw an exception
			}
		}
		currency.setInput(c.toArray());

		label = new Label(composite, SWT.NONE);
		label.setText("Initial Balance");
		initialBalance = new Text(composite, SWT.BORDER);
		initialBalance.setLayoutData(new GridData(convertWidthInCharsToPixels(12), SWT.DEFAULT));

		label = new Label(composite, SWT.NONE);
		label.setText("Expenses Scheme");
		expenses = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		expenses.setLabelProvider(new LabelProvider());
		expenses.setContentProvider(new ArrayContentProvider());
		expenses.setInput(availableSchemes);
		expenses.setSelection(new StructuredSelection(availableSchemes[0]));

		formatter.setGroupingUsed(true);
		formatter.setMinimumIntegerDigits(1);
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);

		if (account != null) {
			name.setText(account.getDescription());
			if (account.getCurrency() != null)
				currency.setSelection(new StructuredSelection(account.getCurrency()));
			initialBalance.setText(formatter.format(account.getInitialBalance()));
			if (account.getExpenseScheme() != null)
				expenses.setSelection(new StructuredSelection(account.getExpenseScheme()));
		}

		name.addModifyListener(modifyListener);
		currency.addSelectionChangedListener(selectionChangedListener);
		initialBalance.addModifyListener(modifyListener);
		expenses.addSelectionChangedListener(selectionChangedListener);

	    return composite;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent) {
    	Control control = super.createButtonBar(parent);
    	getButton(IDialogConstants.OK_ID).setEnabled(isValid());
    	return control;
    }

	protected boolean isValid() {
    	if (name.getText().equals(""))
    		return false;

    	try {
            formatter.parse(initialBalance.getText()).doubleValue();
        } catch (Exception e) {
        	return false;
        }

        if (expenses.getSelection().isEmpty())
        	return false;

        return true;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	if (account != null) {
    		try {
    			account.setDescription(name.getText());
    			account.setCurrency(currency.getSelection().isEmpty() ? null : (Currency) ((IStructuredSelection) currency.getSelection()).getFirstElement());
	            account.setInitialBalance(formatter.parse(initialBalance.getText()).doubleValue());
    			account.setExpenseScheme(expenses.getSelection().isEmpty() ? null : (IExpenseScheme) ((IStructuredSelection) expenses.getSelection()).getFirstElement());
            } catch (Exception e) {
	            e.printStackTrace();
            }
    	}
	    super.okPressed();
    }
}
