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

package net.sourceforge.eclipsetrader.charts.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.charts.wizards.IndicatorPage;
import net.sourceforge.eclipsetrader.charts.wizards.PluginParametersPage;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class AddIndicatorWizard extends Wizard
{
    private IndicatorPage indicatorPage;
    private List additionalPages = new ArrayList();
    ChartIndicator indicator;

    public AddIndicatorWizard()
    {
    }

    public ChartIndicator open()
    {
        WizardDialog dlg = create();
        return (dlg.open() == WizardDialog.OK) ? indicator : null;
    }
    
    protected WizardDialog create()
    {
        setWindowTitle("Add Indicator");
        setForcePreviousAndNextButtons(true);
        
        indicatorPage = new IndicatorPage() {
            protected List getAdditionalPages()
            {
                return additionalPages;
            }
        };
        addPage(indicatorPage);

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        indicator = new ChartIndicator();
        indicator.setPluginId(indicatorPage.getIndicator());
        for (Iterator iter = additionalPages.iterator(); iter.hasNext(); )
        {
            PluginParametersPage page = (PluginParametersPage)iter.next();
            if (page.getControl() != null)
            {
                page.performFinish();
                indicator.getParameters().putAll(page.getSettings().getMap());
            }
        }

        return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage currentPage)
    {
        IWizardPage nextPage = super.getNextPage(currentPage);
        
        if (nextPage == null)
        {
            int index = additionalPages.indexOf(currentPage);
            if (index < (additionalPages.size() - 1))
            {
                nextPage = (IWizardPage)additionalPages.get(index + 1);
                nextPage.setWizard(this);
            }
        }

        return nextPage;
    }
}
