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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewAccountWizard extends Wizard implements INewWizard {

    NamePage namePage;
    SettingsPage settingsPage;

    private Account account;

    public NewAccountWizard() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        addPage(namePage = new NamePage());
        addPage(settingsPage = new SettingsPage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        account = new Account();
        account.setDescription(namePage.getAccountDescription());
        account.setCurrency(settingsPage.getCurrency());
        account.setBalance(settingsPage.getBalance());
        account.setExpenseScheme(settingsPage.getExpenseScheme());

        AccountRepository.getInstance().add(account);

        return true;
    }

    public Account getAccount() {
        return account;
    }
}
