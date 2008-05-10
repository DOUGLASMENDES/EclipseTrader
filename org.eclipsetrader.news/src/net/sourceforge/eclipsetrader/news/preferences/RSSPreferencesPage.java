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

package net.sourceforge.eclipsetrader.news.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.news.NewsPlugin;
import net.sourceforge.eclipsetrader.news.dialogs.RSSFeedDialog;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RSSPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Text interval;
    private Table table;
    private Button editButton;
    private Button removeButton;

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
        
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        Label label = new Label(group, SWT.NONE);
        label.setText(Messages.RSSPreferencesPage_AutoUpdate);
        interval = new Text(group, SWT.BORDER);
        interval.setLayoutData(new GridData(60, SWT.DEFAULT));
        label = new Label(group, SWT.NONE);
        label.setText(Messages.RSSPreferencesPage_Minutes);
        
        table = new Table(content, SWT.FULL_SELECTION|SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 250;
        gridData.widthHint = 250;
        table.setLayoutData(gridData);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.RSSPreferencesPage_Source);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.RSSPreferencesPage_URL);
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                editButton.setEnabled(table.getSelectionCount() == 1);
                removeButton.setEnabled(table.getSelectionCount() != 0);
            }
        });

        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        Button button = new Button(group, SWT.PUSH);
        button.setText(Messages.RSSPreferencesPage_Add);
        button.setLayoutData(new GridData(80, SWT.DEFAULT));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                RSSFeedDialog dlg = new RSSFeedDialog(getShell());
                if (dlg.open() == RSSFeedDialog.OK)
                {
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(0, dlg.getSource());
                    tableItem.setText(1, dlg.getUrl());
                    for (int i = 0; i < table.getColumnCount(); i++)
                        table.getColumn(i).pack();
                }
            }
        });
        editButton = new Button(group, SWT.PUSH);
        editButton.setText(Messages.RSSPreferencesPage_Edit);
        editButton.setEnabled(false);
        editButton.setLayoutData(new GridData(80, SWT.DEFAULT));
        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (table.getSelectionCount() == 1)
                {
                    TableItem item = table.getSelection()[0];
                    RSSFeedDialog dlg = new RSSFeedDialog(getShell());
                    dlg.setSource(item.getText(0));
                    dlg.setUrl(item.getText(1));
                    if (dlg.open() == RSSFeedDialog.OK)
                    {
                        item.setText(0, dlg.getSource());
                        item.setText(1, dlg.getUrl());
                        for (int i = 0; i < table.getColumnCount(); i++)
                            table.getColumn(i).pack();
                    }
                }
            }
        });
        removeButton = new Button(group, SWT.PUSH);
        removeButton.setText(Messages.RSSPreferencesPage_Remove);
        removeButton.setEnabled(false);
        removeButton.setLayoutData(new GridData(80, SWT.DEFAULT));
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                TableItem[] items = table.getSelection();
                for (int i = 0; i < items.length; i++)
                    items[i].dispose();
                for (int i = 0; i < table.getColumnCount(); i++)
                    table.getColumn(i).pack();
            }
        });

        IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();
        interval.setText(store.getString(NewsPlugin.PREFS_UPDATE_INTERVAL));

        File file = new File(Platform.getLocation().toFile(), "rss.xml"); //$NON-NLS-1$
        if (file.exists() == true)
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
    
                Node firstNode = document.getFirstChild();
    
                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    String nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("source")) //$NON-NLS-1$
                    {
                        TableItem tableItem = new TableItem(table, SWT.NONE);
                        tableItem.setText(0, item.getAttributes().getNamedItem("name").getNodeValue()); //$NON-NLS-1$
                        tableItem.setText(1, item.getFirstChild().getNodeValue());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();
        store.setValue(NewsPlugin.PREFS_UPDATE_INTERVAL, interval.getText());

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, "data", null); //$NON-NLS-1$

            Element root = document.getDocumentElement();

            TableItem[] items = table.getItems();
            for (int i = 0; i < items.length; i++)
            {
                Element element = document.createElement("source"); //$NON-NLS-1$
                element.setAttribute("name", items[i].getText(0)); //$NON-NLS-1$
                element.appendChild(document.createTextNode(items[i].getText(1)));
                root.appendChild(element);
            }
            
            TransformerFactory factory = TransformerFactory.newInstance();
            try {
                factory.setAttribute("indent-number", new Integer(4)); //$NON-NLS-1$
            } catch(Exception e) {}
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
            DOMSource source = new DOMSource(document);
            
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), "rss.xml"))); //$NON-NLS-1$
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return super.performOk();
    }
}
