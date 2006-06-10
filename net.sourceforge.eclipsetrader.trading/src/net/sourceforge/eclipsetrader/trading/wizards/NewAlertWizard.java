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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.db.Alert;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.trading.internal.wizards.IDynamicWizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class NewAlertWizard extends Wizard implements IDynamicWizard
{
    private WatchlistItem watchlistItem;
    private AlertSelectionPage alertSelectionPage = new AlertSelectionPage();
    private AlertActionsPage alertActionsPage = new AlertActionsPage();
    private List additionalPages = new ArrayList();
    private Alert alert;

    public NewAlertWizard()
    {
    }
    
    public Alert open(WatchlistItem watchlistItem)
    {
        WizardDialog dlg = create(watchlistItem);
        return dlg.open() == WizardDialog.OK ? alert : null;
    }

    public WizardDialog create(WatchlistItem watchlistItem)
    {
        this.watchlistItem = watchlistItem;
        setWindowTitle("New Alert Wizard");

        setForcePreviousAndNextButtons(true);
        addPage(alertSelectionPage);
        addPage(alertActionsPage);

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        alert = new Alert();
        alert.setPluginId(alertSelectionPage.getIndicator());
        
        alert.setPopup(alertActionsPage.getPopupSelection());
        alert.setHilight(alertActionsPage.getHilightSelection());
        
        for (Iterator iter = additionalPages.iterator(); iter.hasNext(); )
        {
            PluginParametersPage page = (PluginParametersPage) ((CommonWizardPage) iter.next()).getPreferencePage();
            page.performFinish();
            alert.getParameters().putAll(page.getParameters());
        }
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage page)
    {
        IWizardPage nextPage = null;
        
        if (page == alertSelectionPage)
        {
            if (additionalPages.size() != 0)
                nextPage = (IWizardPage) additionalPages.get(0);
        }
        else
        {
            int index = additionalPages.indexOf(page); 
            if (index != -1)
            {
                index++;
                if (index < additionalPages.size())
                    nextPage = (IWizardPage) additionalPages.get(index);
                else
                    nextPage = alertActionsPage;
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

    public Security getSecurity()
    {
        return watchlistItem.getSecurity();
    }
}
