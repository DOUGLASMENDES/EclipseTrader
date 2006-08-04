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

package net.sourceforge.eclipsetrader.core.internal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystemGroup;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class TradingSystemRepository
{
    XMLRepository repository;
    private Integer tsGroupNextId = new Integer(1);
    Map tsGroupMap = new HashMap();
    private Integer tsNextId = new Integer(1);
    Map tsMap = new HashMap();
    private Logger logger = Logger.getLogger(getClass());

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$

    public TradingSystemRepository(XMLRepository repository)
    {
        this.repository = repository;

        File file = new File(Platform.getLocation().toFile(), "ts.xml"); //$NON-NLS-1$
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);

                Node firstNode = document.getFirstChild();
                tsNextId = new Integer(firstNode.getAttributes().getNamedItem("nextId").getNodeValue()); //$NON-NLS-1$
                tsGroupNextId = new Integer(firstNode.getAttributes().getNamedItem("nextGroupId").getNodeValue()); //$NON-NLS-1$

                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    String nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("system")) //$NON-NLS-1$
                    {
                        TradingSystem obj = loadSystem(item.getChildNodes());
                        obj.setRepository(this.repository);
                    }
                    else if (nodeName.equalsIgnoreCase("group")) //$NON-NLS-1$
                    {
                        TradingSystemGroup group = (TradingSystemGroup) loadGroup(item.getChildNodes());
                        group.setRepository(this.repository);
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
    }
    
    void clear()
    {
        File file = new File(Platform.getLocation().toFile(), "ts.xml"); //$NON-NLS-1$
        if (file.exists() == true)
            file.delete();
        
        tsGroupNextId = new Integer(1);
        tsGroupMap = new HashMap();
        tsNextId = new Integer(1);
        tsMap = new HashMap();
    }

    public void save(TradingSystemGroup object)
    {
        if (object.getId() == null)
        {
            object.setId(tsGroupNextId);
            tsGroupNextId = getNextId(tsGroupNextId);
        }
        tsGroupMap.put(object.getId(), object);
        
        if (!repository.getTradingSystemGroups().contains(object))
            repository.getTradingSystemGroups().add(object);
        
        if (object.getParent() != null)
        {
            if (!object.getParent().getGroups().contains(object))
                object.getParent().getGroups().add(object);
        }
    }

    public void save(TradingSystem object)
    {
        if (object.getId() == null)
        {
            object.setId(tsNextId);
            tsNextId = getNextId(tsNextId);
        }
        tsMap.put(object.getId(), object);
        
        if (!repository.getTradingSystems().contains(object))
            repository.getTradingSystems().add(object);

        if (object.getGroup() != null)
        {
            if (!object.getGroup().getTradingSystems().contains(object))
                object.getGroup().getTradingSystems().add(object);
        }
    }
    
    void saveTradingSystems()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, "data", null);

            Element root = document.getDocumentElement();
            root.setAttribute("nextId", String.valueOf(tsNextId));
            root.setAttribute("nextGroupId", String.valueOf(tsGroupNextId));
            
            for (Iterator iter = tsGroupMap.values().iterator(); iter.hasNext(); )
            {
                TradingSystemGroup group = (TradingSystemGroup)iter.next();
                if (group.getParent() == null)
                    saveGroup(group, document, root);
            }

            for (Iterator iter = tsMap.values().iterator(); iter.hasNext(); )
            {
                TradingSystem system = (TradingSystem) iter.next();
                if (system.getGroup() == null)
                    saveSystem(system, document, root);
            }

            repository.saveDocument(document, "", "ts.xml");

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    private TradingSystemGroup loadGroup(NodeList node)
    {
        TradingSystemGroup group = new TradingSystemGroup(new Integer(Integer.parseInt(((Node)node).getAttributes().getNamedItem("id").getNodeValue())));
        
        for (int i = 0; i < node.getLength(); i++)
        {
            Node item = node.item(i);
            String nodeName = item.getNodeName();
            Node value = item.getFirstChild();
            if (value != null)
            {
                if (nodeName.equals("description")) //$NON-NLS-1$
                    group.setDescription(value.getNodeValue());
            }
            if (nodeName.equals("system")) //$NON-NLS-1$
            {
                TradingSystem system = loadSystem(item.getChildNodes());
                system.setGroup(group);
                group.getTradingSystems().add(system);
            }
            else if (nodeName.equals("group")) //$NON-NLS-1$
            {
                TradingSystemGroup grp = loadGroup(item.getChildNodes());
                grp.setParent(group);
                group.getGroups().add(grp);
            }
        }
        
        group.clearChanged();
        tsGroupMap.put(group.getId(), group);
        repository.getTradingSystemGroups().add(group);
        
        return group;
    }

    private void saveGroup(TradingSystemGroup group, Document document, Element root)
    {
        Element element = document.createElement("group");
        element.setAttribute("id", String.valueOf(group.getId()));
        root.appendChild(element);

        Element node = document.createElement("description");
        node.appendChild(document.createTextNode(group.getDescription()));
        element.appendChild(node);

        for (Iterator iter = group.getGroups().iterator(); iter.hasNext(); )
        {
            TradingSystemGroup grp = (TradingSystemGroup)iter.next();
            saveGroup(grp, document, element);
        }

        for (Iterator iter = group.getTradingSystems().iterator(); iter.hasNext(); )
        {
            TradingSystem system = (TradingSystem) iter.next();
            saveSystem(system, document, element);
        }
    }
    
    private void saveSystem(TradingSystem system, Document document, Element root)
    {
        Element element = document.createElement("system");
        element.setAttribute("id", String.valueOf(system.getId()));
        element.setAttribute("pluginId", system.getPluginId());
        root.appendChild(element);

        Element node = document.createElement("security");
        node.appendChild(document.createTextNode(String.valueOf(system.getSecurity().getId())));
        element.appendChild(node);
        node = document.createElement("account");
        node.appendChild(document.createTextNode(String.valueOf(system.getAccount().getId())));
        element.appendChild(node);
        node = document.createElement("max_exposure");
        node.appendChild(document.createTextNode(String.valueOf(system.getMaxExposure())));
        element.appendChild(node);
        node = document.createElement("min_amount");
        node.appendChild(document.createTextNode(String.valueOf(system.getMinAmount())));
        element.appendChild(node);
        node = document.createElement("max_amount");
        node.appendChild(document.createTextNode(String.valueOf(system.getMaxAmount())));
        element.appendChild(node);
        if (system.getDate() != null)
        {
            node = document.createElement("date");
            node.appendChild(document.createTextNode(dateTimeFormat.format(system.getDate())));
            element.appendChild(node);
        }
        node = document.createElement("signal");
        node.appendChild(document.createTextNode(String.valueOf(system.getSignal())));
        element.appendChild(node);

        for (Iterator paramIter = system.getParameters().keySet().iterator(); paramIter.hasNext(); )
        {
            String key = (String)paramIter.next();
            
            node = document.createElement("param");
            node.setAttribute("key", key);
            node.appendChild(document.createTextNode((String)system.getParameters().get(key)));
            element.appendChild(node);
        }
    }

    
    private TradingSystem loadSystem(NodeList node)
    {
        TradingSystem system = new TradingSystem(new Integer(Integer.parseInt(((Node)node).getAttributes().getNamedItem("id").getNodeValue())));
        system.setPluginId(((Node)node).getAttributes().getNamedItem("pluginId").getNodeValue());
        
        for (int i = 0; i < node.getLength(); i++)
        {
            Node item = node.item(i);
            String nodeName = item.getNodeName();
            Node value = item.getFirstChild();
            if (value != null)
            {
                if (nodeName.equals("security")) //$NON-NLS-1$
                    system.setSecurity((Security)repository.load(Security.class, new Integer(value.getNodeValue())));
                else if (nodeName.equals("account")) //$NON-NLS-1$
                    system.setAccount((Account)repository.load(Account.class, new Integer(value.getNodeValue())));
                else if (nodeName.equalsIgnoreCase("date")) //$NON-NLS-1$
                {
                    try {
                        system.setDate(dateTimeFormat.parse(value.getNodeValue()));
                    }
                    catch (Exception e) {
                        logger.warn(e.toString(), e);
                    }
                }
                else if (nodeName.equalsIgnoreCase("signal")) //$NON-NLS-1$
                    system.setSignal(Integer.parseInt(value.getNodeValue()));
                else if (nodeName.equalsIgnoreCase("max_exposure")) //$NON-NLS-1$
                    system.setMaxExposure(Double.parseDouble(value.getNodeValue()));
                else if (nodeName.equalsIgnoreCase("min_amount")) //$NON-NLS-1$
                    system.setMinAmount(Double.parseDouble(value.getNodeValue()));
                else if (nodeName.equalsIgnoreCase("max_amount")) //$NON-NLS-1$
                    system.setMaxAmount(Double.parseDouble(value.getNodeValue()));
                else if (nodeName.equalsIgnoreCase("param")) //$NON-NLS-1$
                {
                    String key = ((Node)item).getAttributes().getNamedItem("key").getNodeValue(); 
                    system.getParameters().put(key, value.getNodeValue());
                }
            }
        }
        
        system.clearChanged();
        tsMap.put(system.getId(), system);
        repository.getTradingSystems().add(system);
        
        return system;
    }

    private Integer getNextId(Integer id)
    {
        return new Integer(id.intValue() + 1);
    }
}
