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

import java.util.HashMap;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.TradingSystemPluginPreferencePage;
import net.sourceforge.eclipsetrader.trading.internal.wizards.IDynamicWizard;
import net.sourceforge.eclipsetrader.trading.wizards.CommonWizardPage;
import net.sourceforge.eclipsetrader.trading.wizards.PluginParametersPage;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class SystemSelectionPage extends WizardPage
{
    private List list;

    public SystemSelectionPage()
    {
        super("");
        setTitle("Trading System");
        setDescription("Select the trading system to use");
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
                setPages();
                setPageComplete(list.getSelectionIndex() != -1);
            }
        });
        
        for (Iterator iter = TradingPlugin.getTradingSystemPlugins().iterator(); iter.hasNext(); )
        {
            IConfigurationElement element = (IConfigurationElement)iter.next();
            list.add(element.getAttribute("name"));
            list.setData(element.getAttribute("name"), element.getAttribute("id"));
        }
    }

    private void setPages()
    {
        java.util.List pages = ((TradingSystemWizard)getWizard()).getAdditionalPages();
        for (Iterator iter = pages.iterator(); iter.hasNext(); )
            ((IWizardPage)iter.next()).dispose();
        pages.clear();
        
        IConfigurationElement[] members = TradingPlugin.getTradingSystemPluginPreferencePages(getPluginId());
        try {
            for (int i = 0; i < members.length; i++)
            {
                TradingSystemPluginPreferencePage preferencePage = (TradingSystemPluginPreferencePage)members[i].createExecutableExtension("class");
                preferencePage.init(((TradingSystemWizard)getWizard()).getSecurity(), new HashMap());
                CommonWizardPage page = new CommonWizardPage(new PluginParametersPage(preferencePage)) {
                    public IWizardPage getNextPage()
                    {
                        java.util.List pages = ((IDynamicWizard)getWizard()).getAdditionalPages();
                        int index = pages.indexOf(this);
                        if (index < (pages.size() - 1))
                        {
                            IWizardPage page = (IWizardPage)pages.get(index + 1);
                            page.setWizard(getWizard());
                            return page;
                        }
                        return null;
                    }
                };
                if (members[i].getAttribute("name") != null)
                    page.setTitle(members[i].getAttribute("name"));
                if (members[i].getAttribute("description") != null)
                    page.setDescription(members[i].getAttribute("description"));
                ((TradingSystemWizard)getWizard()).getAdditionalPages().add(page);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getPluginId()
    {
        return (String)list.getData(list.getItem(list.getSelectionIndex()));
    }
    
    public String getPluginName()
    {
        return list.getItem(list.getSelectionIndex());
    }
}
