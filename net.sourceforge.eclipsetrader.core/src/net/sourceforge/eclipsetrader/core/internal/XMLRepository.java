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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.Repository;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.columns.Column;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class XMLRepository extends Repository
{
    private Integer securitiesNextId = new Integer(1);
    private Map securitiesMap = new HashMap();
    private Map chartsMap = new HashMap();
    private Integer watchlistsNextId = new Integer(1);
    private Map watchlistsMap = new HashMap();
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$

    public XMLRepository()
    {
        File file = new File(Platform.getLocation().toFile(), "securities.xml"); //$NON-NLS-1$
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);

                Node firstNode = document.getFirstChild();
                securitiesNextId = new Integer(firstNode.getAttributes().getNamedItem("nextId").getTextContent()); //$NON-NLS-1$

                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    String nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("security")) //$NON-NLS-1$
                    {
                        Security obj = loadSecurity(item.getChildNodes());
                        obj.setRepository(this);
                        securitiesMap.put(obj.getId(), obj);
                        allSecurities().add(obj);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        file = new File(Platform.getLocation().toFile(), "watchlists.xml"); //$NON-NLS-1$
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);

                Node firstNode = document.getFirstChild();
                watchlistsNextId = new Integer(firstNode.getAttributes().getNamedItem("nextId").getTextContent()); //$NON-NLS-1$

                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    String nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("watchlist")) //$NON-NLS-1$
                    {
                        Watchlist obj = loadWatchlist(item.getChildNodes());
                        obj.setRepository(this);
                        watchlistsMap.put(obj.getId(), obj);
                        allWatchlists().add(obj);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        file = new File(Platform.getLocation().toFile(), "news.xml"); //$NON-NLS-1$
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                
                Calendar today = Calendar.getInstance();
                today.add(Calendar.DATE, -2);

                Node firstNode = document.getFirstChild();

                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    String nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("news")) //$NON-NLS-1$
                    {
                        NewsItem obj = loadNews(item.getChildNodes());
                        if (obj.getDate().before(today.getTime()))
                            continue;
                        
                        obj.setRepository(this);
                        allNews().add(obj);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.Repository#dispose()
     */
    public void dispose()
    {
        saveSecurities();
        saveWatchlists();
        saveNews();
        super.dispose();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.Repository#load(java.lang.Class, java.lang.Integer)
     */
    public PersistentObject load(Class clazz, Integer id)
    {
        PersistentObject obj = null;
        
        if (clazz.equals(Security.class))
            obj = (PersistentObject)securitiesMap.get(id);
        else if (clazz.equals(Chart.class))
            obj = (PersistentObject)chartsMap.get(id);
        else if (clazz.equals(Watchlist.class))
            obj = (PersistentObject)watchlistsMap.get(id);
        
        if (obj == null)
        {
            if (clazz.equals(Chart.class))
            {
                obj = loadChart(id);
                if (obj != null)
                    chartsMap.put(id, obj);
            }

            if (obj != null)
                obj.setRepository(this);
        }
        
        if (obj != null && !obj.getClass().equals(clazz))
            return null;
        
        return obj;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.Repository#save(net.sourceforge.eclipsetrader.core.db.PersistentObject)
     */
    public void save(PersistentObject obj)
    {
        if (obj instanceof Security)
        {
            if (obj.getId() == null)
            {
                obj.setId(securitiesNextId);
                securitiesNextId = getNextId(securitiesNextId);
            }
            securitiesMap.put(obj.getId(), obj);
            saveSecurities();
        }
        
        if (obj instanceof Chart)
        {
            chartsMap.put(obj.getId(), obj);
            saveChart((Chart)obj);
        }
        
        if (obj instanceof Watchlist)
        {
            if (obj.getId() == null)
            {
                obj.setId(watchlistsNextId);
                watchlistsNextId = getNextId(watchlistsNextId);
            }
            watchlistsMap.put(obj.getId(), obj);
            saveWatchlists();
        }
        
        super.save(obj);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.Repository#delete(net.sourceforge.eclipsetrader.core.db.PersistentObject)
     */
    public void delete(PersistentObject obj)
    {
        super.delete(obj);

        if (obj instanceof Security)
        {
            securitiesMap.remove(obj.getId());
            File file = new File(Platform.getLocation().toFile(), "charts/" + String.valueOf(obj.getId()) + ".xml"); //$NON-NLS-1$  $NON-NLS-2$
            if (file.exists())
                file.delete();
            file = new File(Platform.getLocation().toFile(), "history/" + String.valueOf(obj.getId()) + ".xml"); //$NON-NLS-1$  $NON-NLS-2$
            if (file.exists())
                file.delete();
            file = new File(Platform.getLocation().toFile(), "intraday/" + String.valueOf(obj.getId()) + ".xml"); //$NON-NLS-1$  $NON-NLS-2$
            if (file.exists())
                file.delete();
            saveSecurities();
            saveWatchlists();
        }
        if (obj instanceof Watchlist)
        {
            watchlistsMap.remove(obj.getId());
            saveWatchlists();
        }
    }

    private Integer getNextId(Integer id)
    {
        return new Integer(id.intValue() + 1);
    }
    
    public List loadHistory(Integer id)
    {
        List barData = new ArrayList();
        
        File file = new File(Platform.getLocation().toFile(), "history/" + String.valueOf(id) + ".xml"); //$NON-NLS-1$  $NON-NLS-2$
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                barData = decodeBarData(document.getFirstChild().getChildNodes());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        return barData;
    }
    
    public List loadIntradayHistory(Integer id)
    {
        List barData = new ArrayList();
        
        File file = new File(Platform.getLocation().toFile(), "intraday/" + String.valueOf(id) + ".xml");
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                barData = decodeBarData(document.getFirstChild().getChildNodes());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        return barData;
    }
    
    private List decodeBarData(NodeList node)
    {
        Integer id = new Integer(0);
        List barData = new ArrayList();
        
        for (int i = 0; i < node.getLength(); i++)
        {
            Node dataNode = node.item(i);
            if (dataNode.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
            {
                id = new Integer(id.intValue() + 1);
                Bar bar = new Bar(id);
                NodeList valuesNode = dataNode.getChildNodes();
                for (int ii = 0; ii < valuesNode.getLength(); ii++)
                {
                    Node item = valuesNode.item(ii);
                    Node value = item.getFirstChild();
                    if (value != null)
                    {
                        String nodeName = item.getNodeName();
                        if (nodeName.equalsIgnoreCase("open") == true)
                            bar.setOpen(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("high") == true)
                            bar.setHigh(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("low") == true)
                            bar.setLow(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("close") == true)
                            bar.setClose(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("volume") == true)
                            bar.setVolume(Long.parseLong(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("date") == true)
                        {
                            try {
                                bar.setDate(dateTimeFormat.parse(value.getNodeValue()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                barData.add(bar);
            }
        }
        
        return barData;
    }
    
    public void saveHistory(Integer id, List list)
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, null, null);

            Element root = document.createElement("history");
            document.appendChild(root);
            
            encodeBarData(list, root, document);

            saveDocument(document, "history", String.valueOf(id) + ".xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveIntradayHistory(Integer id, List list)
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, null, null);

            Element root = document.createElement("history");
            document.appendChild(root);
            
            encodeBarData(list, root, document);

            saveDocument(document, "intraday", String.valueOf(id) + ".xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void encodeBarData(List list, Element root, Document document)
    {
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Bar bar = (Bar)iter.next(); 

            Element element = document.createElement("data");
            root.appendChild(element);
            
            Element node = document.createElement("open");
            node.appendChild(document.createTextNode(String.valueOf(bar.getOpen())));
            element.appendChild(node);
            node = document.createElement("high");
            node.appendChild(document.createTextNode(String.valueOf(bar.getHigh())));
            element.appendChild(node);
            node = document.createElement("low");
            node.appendChild(document.createTextNode(String.valueOf(bar.getLow())));
            element.appendChild(node);
            node = document.createElement("close");
            node.appendChild(document.createTextNode(String.valueOf(bar.getClose())));
            element.appendChild(node);
            node = document.createElement("volume");
            node.appendChild(document.createTextNode(String.valueOf(bar.getVolume())));
            element.appendChild(node);
            if (bar.getDate() != null)
            {
                node = document.createElement("date");
                node.appendChild(document.createTextNode(dateTimeFormat.format(bar.getDate())));
                element.appendChild(node);
            }
        }
    }
    
    private void saveChart(Chart chart)
    {
        chart.setRepository(this);

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, null, null);

            Element root = document.createElement("chart");
            document.appendChild(root);

            if (chart.getId() != null)
            {
                Element node = document.createElement("title");
                node.appendChild(document.createTextNode(chart.getTitle()));
                root.appendChild(node);
            }

            for (int r = 0; r < chart.getRows().size(); r++)
            {
                ChartRow row = (ChartRow)chart.getRows().get(r);
                row.setId(new Integer(r));
                row.setParent(chart);
                row.setRepository(this);

                Element rowNode = document.createElement("row");
                root.appendChild(rowNode);

                for (int t = 0; t < row.getTabs().size(); t++)
                {
                    ChartTab tab = (ChartTab)row.getTabs().get(t);
                    tab.setId(new Integer(t));
                    tab.setParent(row);
                    tab.setRepository(this);

                    Element tabNode = document.createElement("tab");
                    tabNode.setAttribute("label", tab.getLabel());
                    rowNode.appendChild(tabNode);

                    for (int i = 0; i < tab.getIndicators().size(); i++)
                    {
                        ChartIndicator indicator = (ChartIndicator)tab.getIndicators().get(i);
                        indicator.setId(new Integer(i));
                        indicator.setParent(tab);
                        indicator.setRepository(this);

                        Element indicatorNode = document.createElement("indicator");
                        indicatorNode.setAttribute("pluginId", indicator.getPluginId());
                        tabNode.appendChild(indicatorNode);

                        for (Iterator iter = indicator.getParameters().keySet().iterator(); iter.hasNext(); )
                        {
                            String key = (String)iter.next();

                            Element node = document.createElement("param");
                            node.setAttribute("key", key);
                            node.setAttribute("value", (String)indicator.getParameters().get(key));
                            indicatorNode.appendChild(node);
                        }
                    }

                    for (int i = 0; i < tab.getObjects().size(); i++)
                    {
                        ChartObject object = (ChartObject)tab.getObjects().get(i);
                        object.setId(new Integer(i));
                        object.setParent(tab);
                        object.setRepository(this);

                        Element indicatorNode = document.createElement("object");
                        indicatorNode.setAttribute("pluginId", object.getPluginId());
                        tabNode.appendChild(indicatorNode);

                        for (Iterator iter = object.getParameters().keySet().iterator(); iter.hasNext(); )
                        {
                            String key = (String)iter.next();

                            Element node = document.createElement("param");
                            node.setAttribute("key", key);
                            node.setAttribute("value", (String)object.getParameters().get(key));
                            indicatorNode.appendChild(node);
                        }
                    }
                }
            }

            if (chart.getId() != null)
                saveDocument(document, "charts", String.valueOf(chart.getId()) + ".xml");
            else
                saveDocument(document, "charts", "default.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Chart loadChart(Integer id)
    {
        Chart chart = new Chart(id);
        chart.setRepository(this);
        chart.setSecurity((Security)load(Security.class, id));
        
        File file = new File(Platform.getLocation().toFile(), "charts/" + String.valueOf(id) + ".xml");
        if (file.exists() == false)
            file = new File(Platform.getLocation().toFile(), "charts/default.xml");
        if (file.exists() == true)
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);

                NodeList firstNode = document.getFirstChild().getChildNodes();
                for (int r = 0; r < firstNode.getLength(); r++)
                {
                    Node item = firstNode.item(r);
                    Node valueNode = item.getFirstChild();
                    String nodeName = item.getNodeName();
                    
                    if (valueNode != null)
                    {
                    }
                    if (nodeName.equalsIgnoreCase("row")) //$NON-NLS-1$
                    {
                        ChartRow row = new ChartRow(new Integer(r));
                        row.setRepository(this);
                        row.setParent(chart);
                        
                        NodeList tabList = item.getChildNodes();
                        for (int t = 0; t < tabList.getLength(); t++)
                        {
                            item = tabList.item(t);
                            nodeName = item.getNodeName();
                            if (nodeName.equalsIgnoreCase("tab")) //$NON-NLS-1$
                            {
                                ChartTab tab = new ChartTab(new Integer(t));
                                tab.setRepository(this);
                                tab.setParent(row);
                                tab.setLabel(((Node)item).getAttributes().getNamedItem("label").getTextContent());

                                NodeList indicatorList = item.getChildNodes();
                                for (int i = 0; i < indicatorList.getLength(); i++)
                                {
                                    item = indicatorList.item(i);
                                    nodeName = item.getNodeName();
                                    if (nodeName.equalsIgnoreCase("indicator")) //$NON-NLS-1$
                                    {
                                        ChartIndicator indicator = new ChartIndicator(new Integer(i));
                                        indicator.setRepository(this);
                                        indicator.setParent(tab);
                                        indicator.setPluginId(((Node)item).getAttributes().getNamedItem("pluginId").getTextContent());

                                        NodeList parametersList = item.getChildNodes();
                                        for (int p = 0; p < parametersList.getLength(); p++)
                                        {
                                            item = parametersList.item(p);
                                            nodeName = item.getNodeName();
                                            if (nodeName.equalsIgnoreCase("param")) //$NON-NLS-1$
                                            {
                                                String key = ((Node)item).getAttributes().getNamedItem("key").getTextContent(); 
                                                String value = ((Node)item).getAttributes().getNamedItem("value").getTextContent();
                                                indicator.getParameters().put(key, value);
                                            }
                                        }
                                        
                                        tab.getIndicators().add(indicator);
                                    }
                                    else if (nodeName.equalsIgnoreCase("object")) //$NON-NLS-1$
                                    {
                                        ChartObject object = new ChartObject(new Integer(i));
                                        object.setRepository(this);
                                        object.setParent(tab);
                                        object.setPluginId(((Node)item).getAttributes().getNamedItem("pluginId").getTextContent());

                                        NodeList parametersList = item.getChildNodes();
                                        for (int p = 0; p < parametersList.getLength(); p++)
                                        {
                                            item = parametersList.item(p);
                                            nodeName = item.getNodeName();
                                            if (nodeName.equalsIgnoreCase("param")) //$NON-NLS-1$
                                            {
                                                String key = ((Node)item).getAttributes().getNamedItem("key").getTextContent(); 
                                                String value = ((Node)item).getAttributes().getNamedItem("value").getTextContent();
                                                object.getParameters().put(key, value);
                                            }
                                        }
                                        
                                        tab.getObjects().add(object);
                                    }
                                }

                                row.getTabs().add(tab);
                            }
                        }
                        
                        chart.getRows().add(row);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        if (chart.getTitle().length() == 0)
            chart.setTitle(chart.getSecurity().getDescription());
        chart.clearChanged();
        
        return chart;
    }

    private Security loadSecurity(NodeList node)
    {
        Security security = new Security(new Integer(Integer.parseInt(((Node)node).getAttributes().getNamedItem("id").getTextContent())));
        
        for (int i = 0; i < node.getLength(); i++)
        {
            Node item = node.item(i);
            String nodeName = item.getNodeName();
            Node value = item.getFirstChild();
            if (value != null)
            {
                if (nodeName.equals("code")) //$NON-NLS-1$
                    security.setCode(value.getNodeValue());
                else if (nodeName.equals("description")) //$NON-NLS-1$
                    security.setDescription(value.getNodeValue());
                else if (nodeName.equals("currency")) //$NON-NLS-1$
                    security.setCurrency(Currency.getInstance(value.getNodeValue()));
            }
            if (nodeName.equalsIgnoreCase("feeds")) //$NON-NLS-1$
            {
                NodeList nodeList = item.getChildNodes();
                for (int q = 0; q < nodeList.getLength(); q++)
                {
                    item = nodeList.item(q);
                    nodeName = item.getNodeName();
                    value = item.getFirstChild();
                    if (nodeName.equals("quote")) //$NON-NLS-1$
                    {
                        Security.Feed feed = security.new Feed();
                        feed.setId(item.getAttributes().getNamedItem("id").getTextContent()); //$NON-NLS-1$
                        Node attribute = item.getAttributes().getNamedItem("exchange"); //$NON-NLS-1$
                        if (attribute != null)
                            feed.setExchange(attribute.getTextContent());
                        if (value != null)
                            feed.setSymbol(value.getNodeValue());
                        security.setQuoteFeed(feed);
                    }
                    else if (nodeName.equals("level2")) //$NON-NLS-1$
                    {
                        Security.Feed feed = security.new Feed();
                        feed.setId(item.getAttributes().getNamedItem("id").getTextContent()); //$NON-NLS-1$
                        Node attribute = item.getAttributes().getNamedItem("exchange"); //$NON-NLS-1$
                        if (attribute != null)
                            feed.setExchange(attribute.getTextContent());
                        if (value != null)
                            feed.setSymbol(value.getNodeValue());
                        security.setLevel2Feed(feed);
                    }
                    else if (nodeName.equals("history")) //$NON-NLS-1$
                    {
                        Security.Feed feed = security.new Feed();
                        feed.setId(item.getAttributes().getNamedItem("id").getTextContent()); //$NON-NLS-1$
                        Node attribute = item.getAttributes().getNamedItem("exchange"); //$NON-NLS-1$
                        if (attribute != null)
                            feed.setExchange(attribute.getTextContent());
                        if (value != null)
                            feed.setSymbol(value.getNodeValue());
                        security.setHistoryFeed(feed);
                    }
                }
            }
            else if (nodeName.equalsIgnoreCase("quote")) //$NON-NLS-1$
            {
                Quote quote = new Quote();
                NodeList quoteList = item.getChildNodes();
                for (int q = 0; q < quoteList.getLength(); q++)
                {
                    item = quoteList.item(q);
                    nodeName = item.getNodeName();
                    value = item.getFirstChild();
                    if (value != null)
                    {
                        if (nodeName.equalsIgnoreCase("date")) //$NON-NLS-1$
                        {
                            try {
                                quote.setDate(dateTimeFormat.parse(value.getNodeValue()));
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if (nodeName.equalsIgnoreCase("last")) //$NON-NLS-1$
                            quote.setLast(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("bid")) //$NON-NLS-1$
                            quote.setBid(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("ask")) //$NON-NLS-1$
                            quote.setAsk(Double.parseDouble(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("bidSize")) //$NON-NLS-1$
                            quote.setBidSize(Integer.parseInt(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("askSize")) //$NON-NLS-1$
                            quote.setAskSize(Integer.parseInt(value.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("volume")) //$NON-NLS-1$
                            quote.setVolume(Integer.parseInt(value.getNodeValue()));
                    }
                }
                security.setQuote(quote);
            }
            else if (nodeName.equalsIgnoreCase("data")) //$NON-NLS-1$
            {
                NodeList dataList = item.getChildNodes();
                for (int q = 0; q < dataList.getLength(); q++)
                {
                    item = dataList.item(q);
                    nodeName = item.getNodeName();
                    value = item.getFirstChild();
                    if (value != null)
                    {
                        if (nodeName.equalsIgnoreCase("open")) //$NON-NLS-1$
                            security.setOpen(new Double(Double.parseDouble(value.getNodeValue())));
                        else if (nodeName.equalsIgnoreCase("high")) //$NON-NLS-1$
                            security.setHigh(new Double(Double.parseDouble(value.getNodeValue())));
                        else if (nodeName.equalsIgnoreCase("low")) //$NON-NLS-1$
                            security.setLow(new Double(Double.parseDouble(value.getNodeValue())));
                        else if (nodeName.equalsIgnoreCase("close")) //$NON-NLS-1$
                            security.setClose(new Double(Double.parseDouble(value.getNodeValue())));
                    }
                }
            }
        }
        
        security.clearChanged();
        security.getQuoteMonitor().clearChanged();
        security.getLevel2Monitor().clearChanged();
        
        return security;
    }
    
    private void saveSecurities()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, null, null);

            Element root = document.createElement("data");
            root.setAttribute("nextId", String.valueOf(securitiesNextId));
            document.appendChild(root);
            
            for (Iterator iter = securitiesMap.values().iterator(); iter.hasNext(); )
            {
                Security security = (Security)iter.next(); 

                Element element = document.createElement("security");
                element.setAttribute("id", String.valueOf(security.getId()));
                root.appendChild(element);
                
                Element node = document.createElement("code");
                node.appendChild(document.createTextNode(security.getCode()));
                element.appendChild(node);
                node = document.createElement("description");
                node.appendChild(document.createTextNode(security.getDescription()));
                element.appendChild(node);
                if (security.getCurrency() != null)
                {
                    node = document.createElement("currency");
                    node.appendChild(document.createTextNode(security.getCurrency().getCurrencyCode()));
                    element.appendChild(node);
                }
                
                if (security.getQuoteFeed() != null || security.getHistoryFeed() != null)
                {
                    Node feedsNode = document.createElement("feeds");
                    element.appendChild(feedsNode);
                    if (security.getQuoteFeed() != null)
                    {
                        node = document.createElement("quote");
                        node.setAttribute("id", security.getQuoteFeed().getId());
                        node.setAttribute("exchange", security.getQuoteFeed().getExchange());
                        node.appendChild(document.createTextNode(security.getQuoteFeed().getSymbol()));
                        feedsNode.appendChild(node);
                    }
                    if (security.getLevel2Feed() != null)
                    {
                        node = document.createElement("level2");
                        node.setAttribute("id", security.getLevel2Feed().getId());
                        node.setAttribute("exchange", security.getLevel2Feed().getExchange());
                        node.appendChild(document.createTextNode(security.getLevel2Feed().getSymbol()));
                        feedsNode.appendChild(node);
                    }
                    if (security.getHistoryFeed() != null)
                    {
                        node = document.createElement("history");
                        node.setAttribute("id", security.getHistoryFeed().getId());
                        node.setAttribute("exchange", security.getHistoryFeed().getExchange());
                        node.appendChild(document.createTextNode(security.getHistoryFeed().getSymbol()));
                        feedsNode.appendChild(node);
                    }
                }
                
                if (security.getQuote() != null)
                {
                    Quote quote = security.getQuote();
                    Node quoteNode = document.createElement("quote");

                    if (quote.getDate() != null)
                    {
                        node = document.createElement("date");
                        node.appendChild(document.createTextNode(dateTimeFormat.format(quote.getDate())));
                        quoteNode.appendChild(node);
                    }
                    node = document.createElement("last");
                    node.appendChild(document.createTextNode(String.valueOf(quote.getLast())));
                    quoteNode.appendChild(node);
                    node = document.createElement("bid");
                    node.appendChild(document.createTextNode(String.valueOf(quote.getBid())));
                    quoteNode.appendChild(node);
                    node = document.createElement("ask");
                    node.appendChild(document.createTextNode(String.valueOf(quote.getAsk())));
                    quoteNode.appendChild(node);
                    node = document.createElement("bidSize");
                    node.appendChild(document.createTextNode(String.valueOf(quote.getBidSize())));
                    quoteNode.appendChild(node);
                    node = document.createElement("askSize");
                    node.appendChild(document.createTextNode(String.valueOf(quote.getAskSize())));
                    quoteNode.appendChild(node);
                    node = document.createElement("volume");
                    node.appendChild(document.createTextNode(String.valueOf(quote.getVolume())));
                    quoteNode.appendChild(node);
                    
                    element.appendChild(quoteNode);
                }

                Node dataNode = document.createElement("data");
                element.appendChild(dataNode);
                
                if (security.getOpen() != null)
                {
                    node = document.createElement("open");
                    node.appendChild(document.createTextNode(String.valueOf(security.getOpen())));
                    dataNode.appendChild(node);
                }
                if (security.getHigh() != null)
                {
                    node = document.createElement("high");
                    node.appendChild(document.createTextNode(String.valueOf(security.getHigh())));
                    dataNode.appendChild(node);
                }
                if (security.getLow() != null)
                {
                    node = document.createElement("low");
                    node.appendChild(document.createTextNode(String.valueOf(security.getLow())));
                    dataNode.appendChild(node);
                }
                if (security.getClose() != null)
                {
                    node = document.createElement("close");
                    node.appendChild(document.createTextNode(String.valueOf(security.getClose())));
                    dataNode.appendChild(node);
                }
            }

            saveDocument(document, "", "securities.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Watchlist loadWatchlist(NodeList node)
    {
        int itemIndex = 1;
        Watchlist watchlist = new Watchlist(new Integer(Integer.parseInt(((Node)node).getAttributes().getNamedItem("id").getTextContent())));
        
        for (int i = 0; i < node.getLength(); i++)
        {
            Node item = node.item(i);
            String nodeName = item.getNodeName();
            Node value = item.getFirstChild();
            if (value != null)
            {
                if (nodeName.equalsIgnoreCase("title")) //$NON-NLS-1$
                    watchlist.setDescription(value.getNodeValue());
                else if (nodeName.equalsIgnoreCase("style")) //$NON-NLS-1$
                    watchlist.setStyle(Integer.parseInt(value.getNodeValue()));
                else if (nodeName.equalsIgnoreCase("feed")) //$NON-NLS-1$
                    watchlist.setDefaultFeed(value.getNodeValue());
            }
            if (nodeName.equalsIgnoreCase("columns")) //$NON-NLS-1$
            {
                NodeList columnList = item.getChildNodes();
                for (int c = 0; c < columnList.getLength(); c++)
                {
                    item = columnList.item(c);
                    nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("column")) //$NON-NLS-1$
                    {
                        String clazz = ((Node)item).getAttributes().getNamedItem("class").getTextContent();
                        try {
                            Column column = (Column)Class.forName(clazz).newInstance();

                            NodeList quoteList = item.getChildNodes();
                            for (int q = 0; q < quoteList.getLength(); q++)
                            {
                                item = quoteList.item(q);
                                nodeName = item.getNodeName();
                                value = item.getFirstChild();
                                if (value != null)
                                {
                                    if (nodeName.equalsIgnoreCase("label")) //$NON-NLS-1$
                                        column.setLabel(value.getNodeValue());
                                    else if (nodeName.equalsIgnoreCase("width")) //$NON-NLS-1$
                                        column.setWidth(Integer.parseInt(value.getNodeValue()));
                                }
                            }

                            watchlist.getColumns().add(column);
                        } catch(Exception e) {
                            CorePlugin.logException(e);
                        }
                    }
                }
            }
            else if (nodeName.equalsIgnoreCase("items")) //$NON-NLS-1$
            {
                NodeList itemList = item.getChildNodes();
                for (int c = 0; c < itemList.getLength(); c++)
                {
                    item = itemList.item(c);
                    nodeName = item.getNodeName();
                    if (nodeName.equalsIgnoreCase("security")) //$NON-NLS-1$
                    {
                        WatchlistItem watchlistItem = new WatchlistItem(new Integer(itemIndex++));
                        watchlistItem.setParent(watchlist);

                        String id = ((Node)item).getAttributes().getNamedItem("id").getTextContent();
                        watchlistItem.setSecurity((Security)load(Security.class, new Integer(id)));
                        if (watchlistItem.getSecurity() == null)
                            System.err.println("Unable to load security id " + id);
                        
                        NodeList quoteList = item.getChildNodes();
                        for (int q = 0; q < quoteList.getLength(); q++)
                        {
                            item = quoteList.item(q);
                            nodeName = item.getNodeName();
                            value = item.getFirstChild();
                            if (value != null)
                            {
                                if (nodeName.equalsIgnoreCase("position")) //$NON-NLS-1$
                                    watchlistItem.setPosition(Integer.parseInt(value.getNodeValue()));
                                else if (nodeName.equalsIgnoreCase("paid")) //$NON-NLS-1$
                                    watchlistItem.setPaidPrice(Double.parseDouble(value.getNodeValue()));
                            }
                        }

                        watchlist.getItems().add(watchlistItem);
                    }
                }
            }
        }
        
        watchlist.clearChanged();
        
        return watchlist;
    }
    
    private void saveWatchlists()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, null, null);

            Element root = document.createElement("data");
            root.setAttribute("nextId", String.valueOf(watchlistsNextId));
            document.appendChild(root);
            
            for (Iterator iter = watchlistsMap.values().iterator(); iter.hasNext(); )
            {
                Watchlist watchlist = (Watchlist)iter.next(); 

                Element element = document.createElement("watchlist");
                element.setAttribute("id", String.valueOf(watchlist.getId()));
                root.appendChild(element);

                Element node = document.createElement("title");
                node.appendChild(document.createTextNode(watchlist.getDescription()));
                element.appendChild(node);
                node = document.createElement("style");
                node.appendChild(document.createTextNode(String.valueOf(watchlist.getStyle())));
                element.appendChild(node);
                if (watchlist.getDefaultFeed() != null)
                {
                    node = document.createElement("feed");
                    node.appendChild(document.createTextNode(watchlist.getDefaultFeed()));
                    element.appendChild(node);
                }

                Element columnsNode = document.createElement("columns");
                element.appendChild(columnsNode);

                for (Iterator iter2 = watchlist.getColumns().iterator(); iter2.hasNext(); )
                {
                    Column column = (Column)iter2.next();

                    Element columnNode = document.createElement("column");
                    columnNode.setAttribute("class", column.getClass().getName());
                    columnsNode.appendChild(columnNode);

                    node = document.createElement("label");
                    node.appendChild(document.createTextNode(column.getLabel()));
                    columnNode.appendChild(node);
                    node = document.createElement("width");
                    node.appendChild(document.createTextNode(String.valueOf(column.getWidth())));
                    columnNode.appendChild(node);
                }

                Element itemsNode = document.createElement("items");
                element.appendChild(itemsNode);

                int index = 0;
                for (Iterator iter2 = watchlist.getItems().iterator(); iter2.hasNext(); )
                {
                    WatchlistItem item = (WatchlistItem)iter2.next();
                    item.setId(new Integer(index++));
                    item.setParent(watchlist);
                    item.setRepository(this);

                    Element itemNode = document.createElement("security");
                    itemNode.setAttribute("id", String.valueOf(item.getSecurity().getId()));
                    itemsNode.appendChild(itemNode);

                    if (item.getPosition() != null && item.getPosition().intValue() != 0)
                    {
                        node = document.createElement("position");
                        node.appendChild(document.createTextNode(String.valueOf(item.getPosition())));
                        itemNode.appendChild(node);
                    }
                    if (item.getPaidPrice() != null && item.getPaidPrice().doubleValue() != 0)
                    {
                        node = document.createElement("paid");
                        node.appendChild(document.createTextNode(String.valueOf(item.getPaidPrice())));
                        itemNode.appendChild(node);
                    }
                }
            }
            
            saveDocument(document, "", "watchlists.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDocument(Document document, String path, String name)
    {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            try {
                factory.setAttribute("indent-number", new Integer(4));
            } catch(Exception e) {}
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            
            File file = new File(Platform.getLocation().toFile(), path);
            file.mkdirs();
            file = new File(file, name);
            
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NewsItem loadNews(NodeList node)
    {
        NewsItem news = new NewsItem();
        
        if (((Node)node).getAttributes().getNamedItem("security") != null)
        {
            Security security = getSecurity(((Node)node).getAttributes().getNamedItem("security").getTextContent());
            news.setSecurity(security);
        }
        if (((Node)node).getAttributes().getNamedItem("readed") != null)
            news.setReaded(Boolean.parseBoolean(((Node)node).getAttributes().getNamedItem("readed").getTextContent()));
        
        for (int i = 0; i < node.getLength(); i++)
        {
            Node item = node.item(i);
            String nodeName = item.getNodeName();
            Node value = item.getFirstChild();
            if (value != null)
            {
                if (nodeName.equalsIgnoreCase("date") == true)
                {
                    try {
                        news.setDate(dateTimeFormat.parse(value.getNodeValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (nodeName.equals("description")) //$NON-NLS-1$
                    news.setTitle(value.getNodeValue());
                else if (nodeName.equals("source")) //$NON-NLS-1$
                    news.setSource(value.getNodeValue());
                else if (nodeName.equals("url")) //$NON-NLS-1$
                    news.setUrl(value.getNodeValue());
            }
        }
        
        news.clearChanged();
        
        return news;
    }
    
    private void saveNews()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, null, null);

            Element root = document.createElement("data");
            document.appendChild(root);
            
            for (Iterator iter = allNews().iterator(); iter.hasNext(); )
            {
                NewsItem news = (NewsItem)iter.next(); 

                Element element = document.createElement("news");
                if (news.getSecurity() != null)
                    element.setAttribute("security", String.valueOf(news.getSecurity().getId()));
                element.setAttribute("readed", String.valueOf(news.isReaded()));
                root.appendChild(element);

                Element node = document.createElement("date");
                node.appendChild(document.createTextNode(dateTimeFormat.format(news.getDate())));
                element.appendChild(node);
                node = document.createElement("description");
                node.appendChild(document.createTextNode(news.getTitle()));
                element.appendChild(node);
                node = document.createElement("source");
                node.appendChild(document.createTextNode(news.getSource()));
                element.appendChild(node);
                node = document.createElement("url");
                node.appendChild(document.createTextNode(news.getUrl()));
                element.appendChild(node);
            }
            
            saveDocument(document, "", "news.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
