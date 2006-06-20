/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.wizards.accounts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.db.PersistentPreferenceStore;
import net.sourceforge.eclipsetrader.core.ui.WizardPageAdapter;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class NewAccountWizard extends Wizard
{
    Account account;
    AccountGroup group;
    PluginSelectionPage pluginSelectionPage = new PluginSelectionPage();
    GeneralPage generalPage = new GeneralPage();
    List additionalPages = new ArrayList();
    PersistentPreferenceStore preferenceStore = new PersistentPreferenceStore();

    public NewAccountWizard()
    {
    }

    public Account open()
    {
        WizardDialog dlg = create();
        dlg.open();
        return account;
    }

    public Account open(AccountGroup group)
    {
        this.group = group;
        WizardDialog dlg = create();
        dlg.open();
        return account;
    }
    
    public WizardDialog create()
    {
        setWindowTitle("New Account Wizard");
        
        addPage(pluginSelectionPage);
        addPage(new WizardPageAdapter(generalPage));

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage page)
    {
        IWizardPage nextPage = null;

        int index = additionalPages.indexOf(page); 
        if (index != -1)
        {
            index++;
            if (index < additionalPages.size())
                nextPage = (IWizardPage) additionalPages.get(index);
        }
        else
        {
            nextPage = super.getNextPage(page);
            if (nextPage == null)
            {
                if (additionalPages.size() != 0)
                    nextPage = (IWizardPage) additionalPages.get(0);
            }
        }

        if (nextPage != null)
            nextPage.setWizard(this);
        
        return nextPage;
    }

    public List getAdditionalPages()
    {
        return additionalPages;
    }

    public IPreferenceStore getPreferenceStore()
    {
        return preferenceStore;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        for (Iterator iter = additionalPages.iterator(); iter.hasNext(); )
        {
            IPreferencePage page = (PreferencePage) ((WizardPageAdapter) iter.next()).getPreferencePage();
            if (page.getControl() != null)
                page.performOk();
        }
        
        account = CorePlugin.createAccount(pluginSelectionPage.getPluginId(), preferenceStore, new ArrayList());
        account.setPluginId(pluginSelectionPage.getPluginId());
        account.setGroup(group);
        account.setDescription(generalPage.getText());
        account.setCurrency(generalPage.getCurrency());
        account.setInitialBalance(generalPage.getBalance());

        if (group != null)
        {
            account.setGroup(group);
            CorePlugin.getRepository().save(group);
        }
        
        CorePlugin.getRepository().save(account);
        
        return true;
    }
}
