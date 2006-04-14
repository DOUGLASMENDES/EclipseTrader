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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class CommonWizardPage extends WizardPage
{
    private CommonPreferencePage page;

    public CommonWizardPage(CommonPreferencePage page)
    {
        super("");
        this.page = page;
        this.page.setContainer(this);
        setTitle(page.getTitle());
        setDescription(page.getDescription());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        page.createControl(parent);
        setControl(page.getControl());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    public boolean isPageComplete()
    {
        return page.isPageComplete();
    }
    
    public CommonPreferencePage getPreferencePage()
    {
        return page;
    }
    
    public void performFinish()
    {
        page.performFinish();
    }
}
