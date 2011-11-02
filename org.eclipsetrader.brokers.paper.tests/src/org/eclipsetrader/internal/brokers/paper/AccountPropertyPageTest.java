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

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.internal.brokers.paper.schemes.NoExpensesScheme;
import org.eclipsetrader.internal.brokers.paper.schemes.SimpleFixedScheme;

public class AccountPropertyPageTest extends TestCase {

    Shell shell;
    Account account;
    IAdaptable element;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getDefault());

        account = new Account();
        account.setDescription("Test");
        account.setCurrency(Currency.getInstance("USD"));
        account.setBalance(1500.0);
        account.setExpenseScheme(new SimpleFixedScheme());

        element = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(account.getClass())) {
                    return account;
                }
                return null;
            }
        };
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
    }

    public void testFillAccountSettings() throws Exception {
        AccountPropertyPage dialog = new AccountPropertyPage();
        dialog.setElement(element);

        dialog.createContents(shell);

        assertEquals("Test", dialog.name.getText());
        assertEquals(Currency.getInstance("USD"), ((IStructuredSelection) dialog.currency.getSelection()).getFirstElement());
        assertTrue(((IStructuredSelection) dialog.expenses.getSelection()).getFirstElement() instanceof SimpleFixedScheme);
        assertEquals(150000, dialog.balance.getSelection());
    }

    public void testUpdateBalance() throws Exception {
        AccountPropertyPage dialog = new AccountPropertyPage();
        dialog.setElement(element);
        dialog.createContents(shell);

        dialog.balance.setSelection(350000);
        dialog.performOk();

        assertEquals(new Cash(3500.0, Currency.getInstance("USD")), account.getBalance());
    }

    public void testUpdateDescription() throws Exception {
        AccountPropertyPage dialog = new AccountPropertyPage();
        dialog.setElement(element);
        dialog.createContents(shell);

        dialog.name.setText("New Description");
        dialog.performOk();

        assertEquals("New Description", account.getDescription());
    }

    public void testUpdateExpensesScheme() throws Exception {
        AccountPropertyPage dialog = new AccountPropertyPage();
        dialog.setElement(element);
        dialog.createContents(shell);

        dialog.expenses.setSelection(new StructuredSelection(new NoExpensesScheme()));
        dialog.performOk();

        assertTrue(account.getExpenseScheme() instanceof NoExpensesScheme);
    }

    public void testIsValid() throws Exception {
        AccountPropertyPage dialog = new AccountPropertyPage();
        dialog.setElement(element);

        dialog.createContents(shell);

        assertTrue(dialog.isValid());
    }

    public void testInvalidDescription() throws Exception {
        AccountPropertyPage dialog = new AccountPropertyPage();
        dialog.setElement(element);

        dialog.createContents(shell);
        dialog.name.setText("");

        assertFalse(dialog.isValid());
    }
}
