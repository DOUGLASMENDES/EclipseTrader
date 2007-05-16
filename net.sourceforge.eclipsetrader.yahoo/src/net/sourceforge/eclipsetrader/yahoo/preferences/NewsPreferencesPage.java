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

package net.sourceforge.eclipsetrader.yahoo.preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NewsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button showSubscribersOnly;
    private Table table;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        showSubscribersOnly = new Button(content, SWT.CHECK);
        showSubscribersOnly.setText(Messages.NewsPreferencesPage_GetSubscribersOnly);
        showSubscribersOnly.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        
        table = new Table(content, SWT.FULL_SELECTION|SWT.SINGLE|SWT.CHECK);
        table.setHeaderVisible(true);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = 250;
        table.setLayoutData(gridData);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.NewsPreferencesPage_ProviderColumnName);

        IPreferenceStore store = YahooPlugin.getDefault().getPreferenceStore();
        showSubscribersOnly.setSelection(store.getBoolean(YahooPlugin.PREFS_SHOW_SUBSCRIBERS_ONLY));

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(FileLocator.openStream(YahooPlugin.getDefault().getBundle(), new Path("categories.xml"), false)); //$NON-NLS-1$

            NodeList childNodes = document.getFirstChild().getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++)
            {
                Node node = childNodes.item(i);
                String nodeName = node.getNodeName();
                if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
                {
                    String id = ((Node)node).getAttributes().getNamedItem("id").getNodeValue(); //$NON-NLS-1$
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    NodeList list = node.getChildNodes();
                    for (int x = 0; x < list.getLength(); x++)
                    {
                        Node item = list.item(x);
                        nodeName = item.getNodeName();
                        Node value = item.getFirstChild();
                        if (value != null)
                        {
                            if (nodeName.equalsIgnoreCase("title")) //$NON-NLS-1$
                                tableItem.setText(value.getNodeValue());
                        }
                    }
                    tableItem.setData(id);
                    tableItem.setChecked(store.getBoolean(id));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        table.getColumn(0).pack();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = YahooPlugin.getDefault().getPreferenceStore();

        store.setValue(YahooPlugin.PREFS_SHOW_SUBSCRIBERS_ONLY, showSubscribersOnly.getSelection());
        
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            String id = (String) items[i].getData();
            store.setValue(id, items[i].getChecked());
        }

        return super.performOk();
    }
}
