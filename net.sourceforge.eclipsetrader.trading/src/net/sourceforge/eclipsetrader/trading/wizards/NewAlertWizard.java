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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Alert;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.trading.internal.wizards.IDynamicWizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class NewAlertWizard extends Wizard implements IDynamicWizard
{
    private WatchlistItem watchlistItem;
    private AlertSelectionPage alertSelectionPage = new AlertSelectionPage();
    private List additionalPages = new ArrayList();

    public NewAlertWizard()
    {
    }
    
    public void open(WatchlistItem watchlistItem)
    {
        WizardDialog dlg = create(watchlistItem);
        if (dlg.open() == WizardDialog.OK)
            CorePlugin.getRepository().save(watchlistItem.getParent());
    }

    public WizardDialog create(WatchlistItem watchlistItem)
    {
        this.watchlistItem = watchlistItem;
        setWindowTitle("New Alert Wizard");

        setForcePreviousAndNextButtons(true);
        addPage(alertSelectionPage);

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        Alert alert = new Alert();
        alert.setPluginId(alertSelectionPage.getIndicator());
        
        for (Iterator iter = additionalPages.iterator(); iter.hasNext(); )
        {
            PluginParametersPage page = (PluginParametersPage) iter.next();
            page.performFinish();
            alert.getParameters().putAll(page.getParameters());
        }
        
        watchlistItem.getAlerts().add(alert);
        
        return true;
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
