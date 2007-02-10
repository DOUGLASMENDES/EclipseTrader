/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.opentick.ui.wizards;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.opentick.OpenTickPlugin;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opentick.OTExchange;

/**
 */
public class SecurityPage extends WizardPage
{
    Combo exchange;
    Table table;
    Text search;
    NodeList childNodes;
    
    Runnable runnable = new Runnable() {
        public void run()
        {
            table.setRedraw(false);
            table.removeAll();
            
            String id = (String)exchange.getData(exchange.getText());
            if (id != null)
            {
                try
                {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(FileLocator.openStream(OpenTickPlugin.getDefault().getBundle(), new Path("data/securities." + id.toLowerCase() + ".xml"), false));

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
                            if (item.getFirstChild() != null)
                                tableItem.setText(1, item.getFirstChild().getNodeValue());
                            tableItem.setData(item);
                        }
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            table.setRedraw(true);
            for (int i = 0; i < table.getColumnCount(); i++)
                table.getColumn(i).pack();

            if (search.getText().length() != 0)
                searchJob.run();
        }
    };
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
                    String description = item.getFirstChild().getNodeValue();
                    if (CorePlugin.getRepository().getSecurity(code) != null)
                        continue;
                    if (pattern.length() == 0 || code.toLowerCase().indexOf(pattern) != -1 || description.toLowerCase().indexOf(pattern) != -1)
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

        Composite group = new Composite(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        Label label = new Label(group, SWT.NONE);
        label.setText("Exchange");
        exchange = new Combo(group, SWT.READ_ONLY);
        exchange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        exchange.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                exchange.getDisplay().timerExec(100, runnable);
            }
        });

        table = new Table(composite, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = 250;
        table.setLayoutData(gridData);
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText("Code");
        tableColumn.setWidth(50);
        tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText("Description");
        tableColumn.setWidth(150);
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setPageComplete(table.getSelectionCount() != 0);
            }
        });
        
        label = new Label(composite, SWT.NONE);
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

        List exchanges = new ArrayList();
        exchanges.add(new OTExchange(-1, "A", "American Stock Exchange", "", true, ""));
//        exchanges.add(new OTExchange(-1, "AO", "American Stock Exchange (options)", "", true, ""));
        exchanges.add(new OTExchange(-1, "B", "Boston Stock Exchange", "", true, ""));
//        exchanges.add(new OTExchange(-1, "BO", "Boston Options Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "BT", "Chicago Board of Trade", "", true, ""));
//        exchanges.add(new OTExchange(-1, "CO", "Chicago Board Options Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "EC", "Chicago Board of Trade (E-Mini)", "", true, ""));
        exchanges.add(new OTExchange(-1, "MT", "Chicago Mercantile Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "EM", "Chicago Mercantile Exchange (E-Mini)", "", true, ""));
        exchanges.add(new OTExchange(-1, "HT", "Chicago Mercantile Exchange (GLOBEX)", "", true, ""));
        exchanges.add(new OTExchange(-1, "M", "Chicago (Midwest) Stock Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "C", "Cincinatti Stock Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "DT", "Dow Jones", "", true, ""));
//        exchanges.add(new OTExchange(-1, "IO", "International Securities Exchange (options)", "", true, ""));
        exchanges.add(new OTExchange(-1, "MX", "Montreal Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "D", "Nasdaq ADF", "", true, ""));
        exchanges.add(new OTExchange(-1, "U", "Nasdaq Bulletin Board", "", true, ""));
//        exchanges.add(new OTExchange(-1, "V", "Nasdaq Bulletin Board (pink)", "", true, ""));
        exchanges.add(new OTExchange(-1, "T", "Nasdaq Listed Stocks", "", true, ""));
        exchanges.add(new OTExchange(-1, "Q", "Nasdaq NMS", "", true, ""));
        exchanges.add(new OTExchange(-1, "S", "Nasdaq Small Cap", "", true, ""));
        exchanges.add(new OTExchange(-1, "N", "New York Stock Exchange", "", true, ""));
//        exchanges.add(new OTExchange(-1, "NO", "New York Stock Exchange (options)", "", true, ""));
        exchanges.add(new OTExchange(-1, "P", "Pacific Stock Exchange", "", true, ""));
//        exchanges.add(new OTExchange(-1, "PO", "Pacific Stock Exchange (options)", "", true, ""));
        exchanges.add(new OTExchange(-1, "X", "Philadelphia Stock Exchange", "", true, ""));
//        exchanges.add(new OTExchange(-1, "XO", "Philadelphia Stock Exchange (options)", "", true, ""));
        exchanges.add(new OTExchange(-1, "TO", "Toronto Stock Exchange", "", true, ""));
        exchanges.add(new OTExchange(-1, "VN", "Toronto Stock Exchange (Venture)", "", true, ""));
        
        OTExchange[] list = (OTExchange[])exchanges.toArray(new OTExchange[exchanges.size()]);
        for (int i = 0; i < list.length; i++)
        {
            exchange.add(list[i].getTitle());
            exchange.setData(list[i].getTitle(), list[i].getCode());
        }
    }
    
    public List getSelectedSecurities()
    {
        List list = new ArrayList();

        TableItem[] tableItem = table.getSelection();
        for (int i = 0; i < tableItem.length; i++)
        {
            Node item = (Node) table.getSelection()[i].getData();
            Security security = new Security();
            security.setCode(item.getAttributes().getNamedItem("code").getNodeValue());
            security.setDescription(item.getFirstChild().getNodeValue());
            security.setCurrency(Currency.getInstance(Locale.US));
            
            FeedSource feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.opentick");
            feed.setExchange((String)exchange.getData(exchange.getText()));
            security.setQuoteFeed(feed);
            feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.yahoo");
            security.setHistoryFeed(feed);
            feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.opentick");
            feed.setExchange("is");
            security.setLevel2Feed(feed);

            list.add(security);
        }

        return list;
    }
}
