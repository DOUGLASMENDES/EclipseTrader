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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class IndicatorPage extends WizardPage
{
    private List list;

    public IndicatorPage()
    {
        super("");
        setTitle("Indicator");
        setDescription("Select the indicator to add");
        setPageComplete(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);

        list = new List(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = list.getItemHeight() * 15;
        list.setLayoutData(gridData);
        list.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setPageComplete(list.getSelectionIndex() != -1);
                setPages();
            }
        });

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.INDICATORS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            java.util.List plugins = Arrays.asList(members);
            Collections.sort(plugins, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    if ((arg0 instanceof IConfigurationElement) && (arg1 instanceof IConfigurationElement))
                    {
                        String s0 = ((IConfigurationElement) arg0).getAttribute("name"); //$NON-NLS-1$
                        String s1 = ((IConfigurationElement) arg1).getAttribute("name"); //$NON-NLS-1$
                        return s0.compareTo(s1);
                    }
                    return 0;
                }
            });

            for (Iterator iter = plugins.iterator(); iter.hasNext(); )
            {
                IConfigurationElement element = (IConfigurationElement)iter.next();
                list.add(element.getAttribute("name")); //$NON-NLS-1$
                list.setData(String.valueOf(list.getItemCount() - 1), element.getAttribute("id")); //$NON-NLS-1$
            }
        }
    }
    
    public String getIndicator()
    {
        return (String)list.getData(String.valueOf(list.getSelectionIndex()));
    }
    
    public String getIndicatorName()
    {
        return (String)list.getItem(list.getSelectionIndex());
    }
    
    public void setIndicator(String name)
    {
        list.select(list.indexOf(name));
        setPages();
    }
    
    private void setPages()
    {
        java.util.List pages = ((NewIndicatorWizard)getWizard()).getAdditionalPages();
        for (Iterator iter = pages.iterator(); iter.hasNext(); )
            ((IWizardPage)iter.next()).dispose();
        pages.clear();
        
        IConfigurationElement plugin = ChartsPlugin.getIndicatorPlugin(getIndicator());
        IConfigurationElement[] members = plugin.getChildren("preferencePage");
        if (members.length != 0)
        {
            IConfigurationElement item = members[0];
            try {
                for (int p = 0; p < members.length; p++)
                {
                    Object obj = item.createExecutableExtension("class");
                    if (obj instanceof IndicatorPluginPreferencePage)
                    {
                        IndicatorPluginPreferencePage preferencePage = (IndicatorPluginPreferencePage)obj;
                        preferencePage.setTitle(plugin.getAttribute("name"));
                        preferencePage.setDescription(item.getAttribute("description"));
                        PluginParametersPage page = new PluginParametersPage((IndicatorPluginPreferencePage)preferencePage);
                        ((NewIndicatorWizard)getWizard()).getAdditionalPages().add(page);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        getNextPage().setTitle(plugin.getAttribute("name"));
    }
}
