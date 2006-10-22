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

package net.sourceforge.eclipsetrader.directaworld.wizards;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.directaworld.DirectaWorldPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class SecurityPage extends WizardPage
{
    Table table;
    Text search;
    NodeList childNodes;
    Runnable searchJob = new Runnable() {
        public void run()
        {
            String pattern = search.getText().toLowerCase();

            table.setRedraw(false);
            table.removeAll();
            for (int i = 0; i < childNodes.getLength(); i++)
            {
                Node item = childNodes.item(i);
                String nodeName = item.getNodeName();
                if (nodeName.equalsIgnoreCase("security")) //$NON-NLS-1$
                {
                    String code = item.getAttributes().getNamedItem("code").getNodeValue();
                    String isin = item.getAttributes().getNamedItem("isin").getNodeValue().toLowerCase();
                    String description = item.getFirstChild().getNodeValue().toLowerCase();
                    if (CorePlugin.getRepository().getSecurity(code) != null)
                        continue;
                    if (pattern.length() == 0 || code.toLowerCase().indexOf(pattern) != -1 || isin.indexOf(pattern) != -1 || description.indexOf(pattern) != -1)
                    {
                        TableItem tableItem = new TableItem(table, SWT.NONE);
                        tableItem.setText(0, code);
                        tableItem.setText(1, item.getFirstChild().getNodeValue());
                        tableItem.setData(item);
                    }
                }
            }
            table.setRedraw(true);
        }
    };

    public SecurityPage()
    {
        super("");
        setTitle("Security");
        setDescription("Select one of the available securities");
        setPageComplete(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        setControl(composite);

        table = new Table(composite, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = 250;
        table.setLayoutData(gridData);
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText("Code");
        tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText("Description");
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setPageComplete(table.getSelectionCount() != 0);
            }
        });
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("Search");
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        search = new Text(composite, SWT.BORDER);
        search.setLayoutData(new GridData(120, SWT.DEFAULT));
        search.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                search.getDisplay().timerExec(500, searchJob);
            }
        });

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(FileLocator.openStream(DirectaWorldPlugin.getDefault().getBundle(), new Path("data/securities.xml"), false));

            Node firstNode = document.getFirstChild();

            childNodes = firstNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++)
            {
                Node item = childNodes.item(i);
                String nodeName = item.getNodeName();
                if (nodeName.equalsIgnoreCase("security")) //$NON-NLS-1$
                {
                    String code = item.getAttributes().getNamedItem("code").getNodeValue();
                    if (CorePlugin.getRepository().getSecurity(code) != null)
                        continue;
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(0, code);
                    tableItem.setText(1, item.getFirstChild().getNodeValue());
                    tableItem.setData(item);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();
        
        search.setFocus();
    }
    
    public List getSelectedSecurities()
    {
        List list = new ArrayList();

        TableItem[] tableItem = table.getSelection();
        for (int i = 0; i < tableItem.length; i++)
        {
            Node item = (Node) table.getSelection()[i].getData();
            Security security = new Security();
            security.setCode(item.getAttributes().getNamedItem("code").getNodeValue() + ".MI");
            security.setDescription(item.getFirstChild().getNodeValue());
            security.setCurrency(Currency.getInstance(Locale.ITALY));

            FeedSource feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.directaworld");
            feed.setSymbol(item.getAttributes().getNamedItem("code").getNodeValue());
            security.setQuoteFeed(feed);
            
            feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.borsaitalia");
            feed.setSymbol(item.getAttributes().getNamedItem("isin").getNodeValue());
            security.setHistoryFeed(feed);
            
            list.add(security);
        }

        return list;
    }
}
