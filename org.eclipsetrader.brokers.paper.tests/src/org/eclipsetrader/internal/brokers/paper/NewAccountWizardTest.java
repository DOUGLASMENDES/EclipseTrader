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
import java.util.Locale;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.internal.brokers.paper.schemes.NoExpensesScheme;

public class NewAccountWizardTest extends TestCase {

    Shell shell;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getDefault());
        new AccountRepository();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
    }

    public void testCanFinish() throws Exception {
        NewAccountWizard wizard = new NewAccountWizard();
        wizard.addPages();
        wizard.createPageControls(shell);

        assertFalse(wizard.canFinish());
    }

    public void testCanFinishWithDescription() throws Exception {
        NewAccountWizard wizard = new NewAccountWizard();
        wizard.addPages();
        wizard.createPageControls(shell);

        wizard.namePage.name.setText("Test");

        assertTrue(wizard.canFinish());
    }

    public void testCreateAccount() throws Exception {
        NewAccountWizard wizard = new NewAccountWizard();
        wizard.addPages();
        wizard.createPageControls(shell);

        wizard.namePage.name.setText("Test");
        wizard.performFinish();

        Account account = wizard.getAccount();
        assertEquals("Test", account.getDescription());
        assertEquals(new Cash(0.0, Currency.getInstance(Locale.getDefault())), account.getBalance());
        assertTrue(account.getExpenseScheme() instanceof NoExpensesScheme);
        assertEquals(Currency.getInstance(Locale.getDefault()), account.getCurrency());
    }
}
