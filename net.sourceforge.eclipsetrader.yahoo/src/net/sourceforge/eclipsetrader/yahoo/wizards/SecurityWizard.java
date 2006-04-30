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

package net.sourceforge.eclipsetrader.yahoo.wizards;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SecurityWizard extends Wizard
{
    private ISecurityPage securityPage;

    public SecurityWizard()
    {
    }

    public void open()
    {
        setWindowTitle("U.S. Security Wizard");

        securityPage = new SecurityPage();
        addPage(securityPage);
        
        WizardDialog dlg = create();
        dlg.open();
    }

    public void openGerman()
    {
        setWindowTitle("German Security Wizard");

        securityPage = new GermanSecurityPage();
        addPage(securityPage);
        
        WizardDialog dlg = create();
        dlg.open();
    }

    public void openFrance()
    {
        setWindowTitle("France Security Wizard");

        securityPage = new FrenchSecurityPage();
        addPage(securityPage);
        
        WizardDialog dlg = create();
        dlg.open();
    }

    public void openIndices()
    {
        setWindowTitle("Indices Wizard");

        securityPage = new IndicesPage();
        addPage(securityPage);
        
        WizardDialog dlg = create();
        dlg.open();
    }
    
    public WizardDialog create()
    {
        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    public boolean canFinish()
    {
        return securityPage.isPageComplete();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        for (Iterator iter = securityPage.getSelectedSecurities().iterator(); iter.hasNext(); )
        {
            Security security = (Security) iter.next();
            CorePlugin.getRepository().save(security);
        }

        return true;
    }
}
