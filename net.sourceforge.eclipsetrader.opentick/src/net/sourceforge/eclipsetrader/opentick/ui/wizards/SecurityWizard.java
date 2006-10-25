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

package net.sourceforge.eclipsetrader.opentick.ui.wizards;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.preferences.IntradayDataOptions;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SecurityWizard extends Wizard
{
    private SecurityPage securityPage;
    private IntradayDataOptions options = new IntradayDataOptions();

    public SecurityWizard()
    {
    }

    public void open()
    {
        setWindowTitle("OpenTick Security Wizard");

        securityPage = new SecurityPage();
        addPage(securityPage);
        addIntradayOptionsPage();
        
        WizardDialog dlg = create();
        dlg.open();
    }
    
    private void addIntradayOptionsPage()
    {
        WizardPage page = new WizardPage("") {
            public void createControl(Composite parent)
            {
                setControl(options.createControls(parent, null));
            }
        };
        page.setTitle("Intraday Charts");
        page.setDescription("Set the options to automatically build intraday charts");
        addPage(page);
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
            options.saveSettings(security);
            CorePlugin.getRepository().save(security);
        }

        return true;
    }
}
