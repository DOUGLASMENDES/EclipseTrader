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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class NewIndicatorWizard extends Wizard
{
    private Chart chart;
    private IndicatorPage indicatorPage;
    private IndicatorLocationPage indicatorLocationPage;
    private List additionalPages = new ArrayList();

    public NewIndicatorWizard()
    {
    }

    public Chart open(Chart chart)
    {
        WizardDialog dlg = create(chart);
        if (dlg.open() == WizardDialog.OK)
            CorePlugin.getRepository().save(chart);

        return this.chart;
    }
    
    public WizardDialog create(Chart chart)
    {
        this.chart = chart;
        setWindowTitle("New Indicator Wizard");
        
        indicatorPage = new IndicatorPage();
        addPage(indicatorPage);

        indicatorLocationPage = new IndicatorLocationPage(chart);
        addPage(indicatorLocationPage);

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
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
    
    List getAdditionalPages()
    {
        return additionalPages;
    }
}
