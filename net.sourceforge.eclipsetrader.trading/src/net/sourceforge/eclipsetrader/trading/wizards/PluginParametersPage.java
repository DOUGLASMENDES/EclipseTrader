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

package net.sourceforge.eclipsetrader.trading.wizards;

import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class PluginParametersPage extends WizardPage
{
    private AlertPluginPreferencePage page;

    public PluginParametersPage(AlertPluginPreferencePage page)
    {
        super("");
        this.page = page;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        setControl(page.createContents(parent));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    public IWizardPage getNextPage()
    {
        List pages = ((NewAlertWizard)getWizard()).getAdditionalPages();
        int index = pages.indexOf(this);
        if (index < (pages.size() - 1))
        {
            IWizardPage page = (IWizardPage)pages.get(index + 1);
            page.setWizard(getWizard());
            return page;
        }
        return null;
    }
    
    public void performFinish()
    {
        page.performOk();
    }
    
    public Map getParameters()
    {
        return page.getParameters();
    }
}
