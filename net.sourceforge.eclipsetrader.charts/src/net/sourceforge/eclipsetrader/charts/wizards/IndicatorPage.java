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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class IndicatorPage extends WizardPage
{
    Tree tree;
    Font groupFont;

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

        tree = new Tree(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = tree.getItemHeight() * 15;
        tree.setLayoutData(gridData);
        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                String id = getIndicator();
                if (id != null)
                    setPages();
                setPageComplete(id != null);
            }
        });
        Font font = tree.getFont();
        FontData fontData = font.getFontData()[0];
        groupFont = new Font(font.getDevice(), fontData.getName(), fontData.getHeight(), SWT.BOLD);
        tree.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                groupFont.dispose();
            }
        });
        
        tree.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
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

                    // Creates the group items
                    Map groups = new HashMap();
                    for (Iterator iter = plugins.iterator(); iter.hasNext(); )
                    {
                        IConfigurationElement element = (IConfigurationElement)iter.next();
                        if (element.getName().equals("group"))
                        {
                            TreeItem treeItem = new TreeItem(tree, SWT.NONE);
                            treeItem.setText(element.getAttribute("name")); //$NON-NLS-1$
                            treeItem.setFont(groupFont);
                            groups.put(element.getAttribute("id"), treeItem);
                        }
                    }

                    // Add the plugins under the respective groups, if defined
                    TreeItem treeItem = null;
                    for (Iterator iter = plugins.iterator(); iter.hasNext(); )
                    {
                        IConfigurationElement element = (IConfigurationElement)iter.next();
                        if (element.getName().equals("indicator"))
                        {
                            TreeItem parentItem = (TreeItem)groups.get(element.getAttribute("group"));
                            if (parentItem != null)
                                treeItem = new TreeItem(parentItem, SWT.NONE);
                            else
                            {
                                int index = tree.getItemCount();
                                String name = element.getAttribute("name");
                                TreeItem[] items = tree.getItems();
                                for (int i = 0; i < items.length; i++)
                                {
                                    if (name.compareTo(items[i].getText()) < 0)
                                    {
                                        index = i;
                                        break;
                                    }
                                }
                                treeItem = new TreeItem(tree, SWT.NONE, index);
                            }
                            treeItem.setText(element.getAttribute("name")); //$NON-NLS-1$
                            treeItem.setData(element.getAttribute("id")); //$NON-NLS-1$
                        }
                    }

                    // Removes the groups without childrens
                    for (Iterator iter = groups.values().iterator(); iter.hasNext(); )
                    {
                        treeItem = (TreeItem)iter.next();
                        if (treeItem.getItemCount() == 0)
                            treeItem.dispose();
                    }
                }
            }
        });
    }
    
    public String getIndicator()
    {
        TreeItem[] selection = tree.getSelection();
        if (selection.length == 1)
            return (String)selection[0].getData();
        return null;
    }
    
    public String getIndicatorName()
    {
        TreeItem[] selection = tree.getSelection();
        if (selection.length == 1)
            return (String)selection[0].getText();
        return "";
    }
    
    protected abstract java.util.List getAdditionalPages();
    
    private void setPages()
    {
        java.util.List pages = getAdditionalPages();
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
                        pages.add(page);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        getNextPage().setTitle(plugin.getAttribute("name"));
    }
}
