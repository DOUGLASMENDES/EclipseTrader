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

package net.sourceforge.eclipsetrader.charts.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class NewIndicatorWizard extends Wizard
{
    private Chart chart;
    private int defaultRow = 1;
    private String defaultTab;
    private IndicatorPage indicatorPage;
    private IndicatorLocationPage indicatorLocationPage;
    private List additionalPages = new ArrayList();

    public NewIndicatorWizard()
    {
    }

    public int open(Chart chart)
    {
        WizardDialog dlg = create(chart);
        int result = dlg.open();
        if (result == WizardDialog.OK)
            CorePlugin.getRepository().save(chart);

        return result;
    }
    
    public WizardDialog create(Chart chart)
    {
        this.chart = chart;
        setWindowTitle(Messages.NewIndicatorWizard_Title);
        
        indicatorPage = new IndicatorPage() {
            protected List getAdditionalPages()
            {
                return additionalPages;
            }
        };
        addPage(indicatorPage);

        indicatorLocationPage = new IndicatorLocationPage(chart);
        addPage(indicatorLocationPage);

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        ChartIndicator indicator = new ChartIndicator();
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

        if (indicatorLocationPage.getNewRow())
        {
            ChartRow row = new ChartRow();
            chart.getRows().add(row);
            ChartTab tab = new ChartTab();
            row.getTabs().add(tab);
            
            tab.setLabel(indicatorLocationPage.getTabLabel());
            tab.getIndicators().add(indicator);

            chart.setChanged();
        }
        else if (indicatorLocationPage.getNewTab())
        {
            ChartRow row = (ChartRow)chart.getRows().get(indicatorLocationPage.getRowNumber() - 1);
            ChartTab tab = new ChartTab();
            row.getTabs().add(tab);
            
            tab.setLabel(indicatorLocationPage.getTabLabel());
            tab.getIndicators().add(indicator);

            chart.setChanged();
        }
        else if (indicatorLocationPage.getStacked())
        {
            ChartTab tab = indicatorLocationPage.getStackedTab();
            tab.getIndicators().add(indicator);

            chart.setChanged();
        }
        
        return true;
    }
    
    IndicatorPage getIndicatorPage()
    {
        return indicatorPage;
    }

    public int getDefaultRow()
    {
        return defaultRow;
    }

    public void setDefaultRow(int defaultRow)
    {
        this.defaultRow = defaultRow;
    }

    public String getDefaultTab()
    {
        return defaultTab;
    }

    public void setDefaultTab(String defaultTab)
    {
        this.defaultTab = defaultTab;
    }
}
