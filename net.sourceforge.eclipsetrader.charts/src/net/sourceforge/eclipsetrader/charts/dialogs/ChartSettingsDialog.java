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

package net.sourceforge.eclipsetrader.charts.dialogs;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class ChartSettingsDialog extends PreferenceDialog
{
    private Chart chart;
    private GeneralPage generalPage;

    public ChartSettingsDialog(Chart chart, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.chart = chart;
        
        generalPage = new GeneralPage(chart);
        PreferenceNode generalNode = new PreferenceNode("general", generalPage);
        getPreferenceManager().addToRoot(generalNode);
        
        for (int r = 0; r < chart.getRows().size(); r++)
        {
            ChartRow row = (ChartRow)chart.getRows().get(r);

            for (int t = 0; t < row.getTabs().size(); t++)
            {
                ChartTab tab = (ChartTab)row.getTabs().get(t);

                PreferenceNode tabNode = new PreferenceNode("tab" + t, new TabsPage(tab));
                getPreferenceManager().addToRoot(tabNode);
            
                for (int i = 0; i < tab.getIndicators().size(); i++)
                {
                    ChartIndicator indicator = (ChartIndicator)tab.getIndicators().get(i);
                    Settings settings = new Settings();
                    for (Iterator iter = indicator.getParameters().keySet().iterator(); iter.hasNext(); )
                    {
                        String key = (String)iter.next();
                        settings.set(key, (String)indicator.getParameters().get(key));
                    }
                    
                    IConfigurationElement plugin = getIndicatorPlugin(indicator.getPluginId());
                    IConfigurationElement[] members = plugin.getChildren("preferencePage");
                    if (members.length != 0)
                    {
                        PreferenceNode indicatorNode = null;

                        IConfigurationElement item = members[0];
                        try {
                            Object obj = item.createExecutableExtension("class");
                            if (obj instanceof IndicatorPluginPreferencePage)
                            {
                                ((IndicatorPluginPreferencePage)obj).setSettings(settings);
                                IndicatorPage page = new IndicatorPage(indicator, (IndicatorPluginPreferencePage)obj);
                                if (item.getAttribute("title") != null)
                                    page.setTitle(item.getAttribute("title"));

                                indicatorNode = new PreferenceNode("indicator", page);
                                tabNode.add(indicatorNode);
                            }

                            for (int p = 1; p < members.length; p++)
                            {
                                obj = item.createExecutableExtension("class");
                                if (obj instanceof IndicatorPluginPreferencePage)
                                {
                                    ((IndicatorPluginPreferencePage)obj).setSettings(settings);
                                    IndicatorPage page = new IndicatorPage(indicator, (IndicatorPluginPreferencePage)obj);
                                    if (item.getAttribute("title") != null)
                                        page.setTitle(item.getAttribute("title"));

                                    PreferenceNode node = new PreferenceNode("prefs" + p, page);
                                    indicatorNode.add(node);
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Chart Settings");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#okPressed()
     */
    protected void okPressed()
    {
        super.okPressed();

        if (generalPage.getClearData())
        {
            chart.getSecurity().getHistory().clear();
            chart.setChanged();
            CorePlugin.getRepository().saveHistory(chart.getSecurity().getId(), chart.getSecurity().getHistory());
        }
        
        if (generalPage.getSaveAsDefault())
            ChartsPlugin.saveDefaultChart(chart);

        CorePlugin.getRepository().save(chart);
        chart.notifyObservers();
    }

    private static IConfigurationElement getIndicatorPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.INDICATORS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item;
            }
        }
        
        return null;
    }
}
