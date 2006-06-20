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

package net.sourceforge.eclipsetrader.trading.wizards.accounts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ui.WizardPageAdapter;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class PluginSelectionPage extends WizardPage
{
    List list;

    public PluginSelectionPage()
    {
        super("");
        setTitle("Account Type");
        setDescription("Select account type to create");
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

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.ACCOUNT_PROVIDERS_EXTENSION_POINT);
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
    
    public String getPluginId()
    {
        return (String)list.getData(String.valueOf(list.getSelectionIndex()));
    }

    private void setPages()
    {
        NewAccountWizard wizard = (NewAccountWizard)getWizard();
        String pluginId = getPluginId();
        
        java.util.List pages = wizard.getAdditionalPages();
        for (Iterator iter = pages.iterator(); iter.hasNext(); )
            ((IWizardPage)iter.next()).dispose();
        pages.clear();
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.ACCOUNT_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                if (members[i].getAttribute("id").equals(pluginId)) //$NON-NLS-1$
                {
                    members = members[i].getChildren("preferencePage");
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        try
                        {
                            PreferencePage preferencePage = (PreferencePage)members[ii].createExecutableExtension("class");
                            WizardPageAdapter page = new WizardPageAdapter(preferencePage);
                            if (members[ii].getAttribute("name") != null)
                                page.setTitle(members[ii].getAttribute("name"));
                            if (members[ii].getAttribute("description") != null)
                                page.setDescription(members[ii].getAttribute("description"));
                            preferencePage.setPreferenceStore(wizard.getPreferenceStore());
                            wizard.getAdditionalPages().add(page);
                        }
                        catch (Exception e)
                        {
                            CorePlugin.logException(e);
                        }
                    }
                    break;
                }
            }
        }
    }
}
