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

package net.sourceforge.eclipsetrader.yahoo.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class IndicesPage extends WizardPage implements ISecurityPage
{
    private Tree tree;
    private Text search;
    private NodeList childNodes;

    public IndicesPage()
    {
        super("");
        setTitle("Index");
        setDescription("Select one of the available indices");
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

        tree = new Tree(composite, SWT.MULTI|SWT.FULL_SELECTION);
        tree.setHeaderVisible(false);
        tree.setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = 250;
        tree.setLayoutData(gridData);
        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setPageComplete(tree.getSelectionCount() != 0);
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
                String pattern = search.getText().toLowerCase();
                tree.setRedraw(false);
                tree.removeAll();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    String nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
                    {
                        if (hasMatchingItems(item.getChildNodes(), pattern))
                        {
                            TreeItem parentItem = new TreeItem(tree, SWT.NONE);
                            parentItem.setText(item.getAttributes().getNamedItem("name").getNodeValue());
                            addMatchingItems(parentItem, item.getChildNodes(), pattern);
                            if (pattern.length() != 0)
                                parentItem.setExpanded(true);
                        }
                    }
                    else if (nodeName.equalsIgnoreCase("index")) //$NON-NLS-1$
                    {
                        String code = item.getAttributes().getNamedItem("code").getNodeValue().toLowerCase();
                        String description = item.getFirstChild().getNodeValue().toLowerCase();
                        if (pattern.length() == 0 || code.indexOf(pattern) != -1 || description.indexOf(pattern) != -1)
                        {
                            if (CorePlugin.getRepository().getSecurity(code) != null)
                                continue;
                            TreeItem treeItem = new TreeItem(tree, SWT.NONE);
                            treeItem.setText(item.getFirstChild().getNodeValue());
                            treeItem.setData(item);
                        }
                    }
                }
                tree.setRedraw(true);
            }
        });

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(FileLocator.openStream(YahooPlugin.getDefault().getBundle(), new Path("data/indices.xml"), false)); //$NON-NLS-1$

            Node firstNode = document.getFirstChild();

            childNodes = firstNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++)
            {
                Node item = childNodes.item(i);
                String nodeName = item.getNodeName();
                if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
                {
                    TreeItem parentItem = new TreeItem(tree, SWT.NONE);
                    parentItem.setText(item.getAttributes().getNamedItem("name").getNodeValue());
                    addCategory(parentItem, item.getChildNodes());
                }
                else if (nodeName.equalsIgnoreCase("index")) //$NON-NLS-1$
                {
                    String code = item.getAttributes().getNamedItem("code").getNodeValue();
                    if (CorePlugin.getRepository().getSecurity(code) != null)
                        continue;
                    TreeItem treeItem = new TreeItem(tree, SWT.NONE);
                    treeItem.setText(item.getFirstChild().getNodeValue()); //$NON-NLS-1$
                    treeItem.setData(item);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        for (int i = 0; i < tree.getColumnCount(); i++)
            tree.getColumn(i).pack();
    }
    
    private void addCategory(TreeItem parentItem, NodeList list)
    {
        for (int i = 0; i < list.getLength(); i++)
        {
            Node item = list.item(i);
            String nodeName = item.getNodeName();
            if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
            {
                TreeItem parent = new TreeItem(parentItem, SWT.NONE);
                parent.setText(item.getAttributes().getNamedItem("name").getNodeValue());
                addCategory(parent, item.getChildNodes());
            }
            else if (nodeName.equalsIgnoreCase("index")) //$NON-NLS-1$
            {
                String code = item.getAttributes().getNamedItem("code").getNodeValue();
                if (CorePlugin.getRepository().getSecurity(code) != null)
                    continue;
                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(item.getFirstChild().getNodeValue());
                treeItem.setData(item);
            }
        }
    }
    
    private boolean hasMatchingItems(NodeList list, String pattern)
    {
        if (pattern.length() == 0)
            return true;

        for (int i = 0; i < list.getLength(); i++)
        {
            Node item = list.item(i);
            String nodeName = item.getNodeName();
            if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
            {
                if (hasMatchingItems(item.getChildNodes(), pattern))
                    return true;
            }
            else if (nodeName.equalsIgnoreCase("index")) //$NON-NLS-1$
            {
                String code = item.getAttributes().getNamedItem("code").getNodeValue().toLowerCase();
                String description = item.getFirstChild().getNodeValue().toLowerCase();
                if (code.indexOf(pattern) != -1 || description.indexOf(pattern) != -1)
                    return true;
            }
        }
        
        return false;
    }
    
    private void addMatchingItems(TreeItem parentItem, NodeList list, String pattern)
    {
        for (int i = 0; i < list.getLength(); i++)
        {
            Node item = list.item(i);
            String nodeName = item.getNodeName();
            if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
            {
                if (hasMatchingItems(item.getChildNodes(), pattern))
                {
                    TreeItem parent = new TreeItem(parentItem, SWT.NONE);
                    parent.setText(item.getAttributes().getNamedItem("name").getNodeValue());
                    addMatchingItems(parent, item.getChildNodes(), pattern);
                    if (pattern.length() != 0)
                        parent.setExpanded(true);
                }
            }
            else if (nodeName.equalsIgnoreCase("index")) //$NON-NLS-1$
            {
                String code = item.getAttributes().getNamedItem("code").getNodeValue().toLowerCase();
                String description = item.getFirstChild().getNodeValue().toLowerCase();
                if (pattern.length() == 0 || code.indexOf(pattern) != -1 || description.indexOf(pattern) != -1)
                {
                    if (CorePlugin.getRepository().getSecurity(code) != null)
                        continue;
                    TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                    treeItem.setText(item.getFirstChild().getNodeValue());
                    treeItem.setData(item);
                }
            }
        }
    }
    
    public List getSelectedSecurities()
    {
        List list = new ArrayList();

        TreeItem[] tableItem = tree.getSelection();
        for (int i = 0; i < tableItem.length; i++)
        {
            Node item = (Node) tree.getSelection()[i].getData();
            Security security = new Security();
            security.setCode(item.getAttributes().getNamedItem("code").getNodeValue());
            security.setDescription(item.getFirstChild().getNodeValue());
            
            FeedSource feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.yahoo");
            security.setQuoteFeed(feed);
            feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.yahoo");
            security.setHistoryFeed(feed);
            feed = new FeedSource();
            feed.setId("net.sourceforge.eclipsetrader.archipelago");
            security.setLevel2Feed(feed);

            list.add(security);
        }

        return list;
    }
}
