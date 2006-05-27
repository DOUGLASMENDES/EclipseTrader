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

package net.sourceforge.eclipsetrader.trading.wizards.systems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystemGroup;
import net.sourceforge.eclipsetrader.trading.internal.wizards.IDynamicWizard;
import net.sourceforge.eclipsetrader.trading.wizards.CommonWizardPage;
import net.sourceforge.eclipsetrader.trading.wizards.PluginParametersPage;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class TradingSystemWizard extends Wizard implements IDynamicWizard
{
    private TradingSystemGroup group;
    private SystemSelectionPage systemSelection = new SystemSelectionPage();
    private BaseParametersPage baseParameters = new BaseParametersPage();
    private List additionalPages = new ArrayList();

    public TradingSystemWizard()
    {
        setWindowTitle("New Trading System Wizard");
    }

    public void open()
    {
        WizardDialog dlg = create();
        dlg.open();
    }

    public WizardDialog create()
    {
        setWindowTitle("New Watchlist Wizard");
        
        addPage(systemSelection);
        addPage(new CommonWizardPage(baseParameters) {
            public IWizardPage getNextPage()
            {
                if (additionalPages.size() != 0)
                {
                    IWizardPage page = (IWizardPage)additionalPages.get(0);
                    page.setWizard(getWizard());
                    return page;
                }
                return null;
            }
        });

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }

    public TradingSystemGroup getGroup()
    {
        return group;
    }

    public void setGroup(TradingSystemGroup group)
    {
        this.group = group;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        TradingSystem system = new TradingSystem();
        system.setPluginId(systemSelection.getPluginId());
        
        system.setAccount(baseParameters.getAccount());
        system.setSecurity(baseParameters.getSecurity());
        system.setMaxExposure(baseParameters.getMaxExposure());
        system.setMaxAmount(baseParameters.getMaxAmount());
        system.setMinAmount(baseParameters.getMinAmount());
        system.setGroup(group);

        for (Iterator iter = additionalPages.iterator(); iter.hasNext(); )
        {
            PluginParametersPage page = (PluginParametersPage) ((CommonWizardPage)iter.next()).getPreferencePage();
            page.performFinish();
            system.getParameters().putAll(page.getParameters());
        }
        
        CorePlugin.getRepository().save(system);
        
        return true;
    }
    
    public List getAdditionalPages()
    {
        return additionalPages;
    }
    
    Security getSecurity()
    {
        return baseParameters.getSecurity();
    }
}
