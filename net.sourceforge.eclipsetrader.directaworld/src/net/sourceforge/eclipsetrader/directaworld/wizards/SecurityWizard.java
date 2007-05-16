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

package net.sourceforge.eclipsetrader.directaworld.wizards;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.preferences.IntradayDataOptions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SecurityWizard extends Wizard implements INewWizard
{
    public static String WINDOW_TITLE = Messages.SecurityWizard_Title;
    private SecurityPage securityPage;
    private IntradayDataOptions options = new IntradayDataOptions();

    public SecurityWizard()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        setWindowTitle(WINDOW_TITLE);
        
        securityPage = new SecurityPage();
        addPage(securityPage);
    }

    public void open()
    {
        setWindowTitle(WINDOW_TITLE);
        WizardDialog dlg = create();
        dlg.open();
    }
    
    public WizardDialog create()
    {
        securityPage = new SecurityPage();
        addPage(securityPage);

        WizardPage page = new WizardPage("") { //$NON-NLS-1$
            public void createControl(Composite parent)
            {
                setControl(options.createControls(parent, null));
            }
        };
        page.setTitle(Messages.SecurityWizard_IntradayChartsTitle);
        page.setDescription(Messages.SecurityWizard_IntradayChartsDescription);
        addPage(page);

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
