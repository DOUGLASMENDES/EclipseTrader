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

package net.sourceforge.eclipsetrader.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CurrencyConverter extends Observable
{
    private static CurrencyConverter instance = new CurrencyConverter();
    List currencies = new ArrayList();
    Map map = new HashMap();
    Map historyMap = new HashMap();
    NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    Log logger = LogFactory.getLog(getClass());
    
    private class History
    {
        Date date;
        Double ratio;
        
        public History()
        {
        }

        public History(Date date, Double ratio)
        {
            this.date = date;
            this.ratio = ratio;
        }

        public boolean equals(Object obj)
        {
            if (obj instanceof Date)
                return date.equals((Date)obj);
            if (obj instanceof History)
                return date.equals(((History)obj).date);
            return false;
        }
    }

    CurrencyConverter()
    {
        File file = new File(Platform.getLocation().toFile(), "currencies.xml"); //$NON-NLS-1$
        if (file.exists() == true)
        {
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

                    if (nodeName.equalsIgnoreCase("currency")) //$NON-NLS-1$
                    {
                        Node valueNode = item.getFirstChild();
                        if (valueNode != null)
                            currencies.add(valueNode.getNodeValue());
                    }
                    else if (nodeName.equalsIgnoreCase("conversion")) //$NON-NLS-1$
                    {
                        String symbol = ((Node)item).getAttributes().getNamedItem("symbol").getNodeValue(); //$NON-NLS-1$
                        
                        Node valueNode = null;
                        if (((Node)item).getAttributes().getNamedItem("ratio") != null) //$NON-NLS-1$
                            valueNode = ((Node)item).getAttributes().getNamedItem("ratio"); //$NON-NLS-1$
                        if (valueNode == null)
                            valueNode = item.getFirstChild();
                        if (valueNode != null)
                        {
                            Double value = new Double(Double.parseDouble(valueNode.getNodeValue()));
                            map.put(symbol, value);
                        }
                        readHistory(symbol, item.getChildNodes());
                    }
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
    private void readHistory(String symbol, NodeList nodeList)
    {
        List list = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node item = nodeList.item(i);
            String nodeName = item.getNodeName();
            if (nodeName.equalsIgnoreCase("history")) //$NON-NLS-1$
            {
                History history = new History();
                try {
                    history.date = dateFormat.parse(item.getAttributes().getNamedItem("date").getNodeValue());
                    history.ratio = new Double(item.getAttributes().getNamedItem("ratio").getNodeValue());
                } catch (Exception e) {
                    logger.warn(e);
                }
                if (history.date != null && history.ratio != null && !list.contains(history))
                    list.add(history);
            }
        }

        historyMap.put(symbol, list);
    }

    public static CurrencyConverter getInstance()
    {
        return instance;
    }
    
    public void clear()
    {
        File file = new File(Platform.getLocation().toFile(), "currencies.xml"); //$NON-NLS-1$
        if (file.exists() == true)
            file.delete();
        currencies.clear();
        map.clear();
        historyMap.clear();
    }
    
    public void dispose()
    {
        save();
    }

    public List getCurrencies()
    {
        return currencies;
    }

    public void setCurrencies(List currencies)
    {
        this.currencies = currencies;
    }

    public double convert(double amount, Currency from, Currency to)
    {
        if (from == null || to == null || from.equals(to))
            return amount;
        return convert(null, amount, from.getCurrencyCode(), to.getCurrencyCode());
    }

    public double convert(Date date, double amount, Currency from, Currency to)
    {
        if (from == null || to == null || from.equals(to))
            return amount;
        return convert(date, amount, from.getCurrencyCode(), to.getCurrencyCode());
    }

    public double convert(Double amount, Currency from, Currency to)
    {
        if (from == null || to == null || from.equals(to))
            return amount.doubleValue();
        return convert(null, amount.doubleValue(), from.getCurrencyCode(), to.getCurrencyCode());
    }

    public double convert(Date date, Double amount, Currency from, Currency to)
    {
        if (from == null || to == null || from.equals(to))
            return amount.doubleValue();
        return convert(date, amount.doubleValue(), from.getCurrencyCode(), to.getCurrencyCode());
    }

    public double convert(double amount, String from, String to)
    {
        return convert(null, amount, from, to);
    }

    public double convert(Date date, double amount, String from, String to)
    {
        double result = amount;

        if (from == null || to == null || from.equals(to))
            return result;
        
        Double ratio = null;
        if (date != null)
        {
            ratio = getExchangeRatio(date, from, to);
            if (ratio == null)
            {
                Double r = getExchangeRatio(date, to, from);
                if (r != null)
                    ratio = new Double(1 / r.doubleValue());
            }
        }
        if (ratio == null)
            ratio = getExchangeRatio(from, to);

        if (ratio != null)
            result = amount * ratio.doubleValue();
        
        if (date != null && ratio != null)
            setExchangeRatio(date, from, to, ratio.doubleValue());

        return result;
    }
    
    public IStatus updateExchanges(IProgressMonitor monitor)
    {
        if (monitor != null)
            monitor.beginTask("Updating currencies", currencies.size() + 1);
        map.clear();
        
        Object[] symbols = currencies.toArray();
        for (int x = 0; x < symbols.length; x++)
        {
            for (int y = 0; y < symbols.length; y++)
            {
                if (x != y)
                {
                    String s = (String)symbols[x] + (String)symbols[y];
                    Double old = (Double)map.get(s);
                    Double quote = downloadQuote(s);
                    if (quote != null)
                        map.put(s, quote);
                    else
                        map.remove(s);
                    if (old == null && quote != null || old != null && !old.equals(quote))
                        setChanged();
                    if (monitor != null && monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                }
            }
            if (monitor != null)
                monitor.worked(1);
        }
        notifyObservers();

        save();
        if (monitor != null)
            monitor.worked(1);

        if (monitor != null)
            monitor.done();

        return Status.OK_STATUS;
    }
    
    public void setExchangeRatio(Currency from, Currency to, double ratio)
    {
        if (from != null && to != null && !from.equals(to))
            setExchangeRatio(from.getCurrencyCode(), to.getCurrencyCode(), ratio);
    }
    
    public void setExchangeRatio(String from, String to, double ratio)
    {
        Double old = (Double)map.get(from + to);
        Double quote = new Double(ratio);
        map.put(from + to, quote);
        if (old == null && quote != null || old != null && !old.equals(quote))
        {
            setChanged();
            notifyObservers();
        }
    }
    
    public Double getExchangeRatio(String from, String to)
    {
        Double ratio = (Double)map.get(from + to);
        if (ratio == null)
        {
            Double r = (Double)map.get(to + from);
            if (r != null)
                ratio = new Double(1 / r.doubleValue());
        }
        return ratio;
    }
    
    public void setExchangeRatio(Date date, String from, String to, double ratio)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        
        List list = (ArrayList)historyMap.get(from + to);
        if (list == null)
            list = new ArrayList();
        History history = new History(date, new Double(ratio));
        int index = list.indexOf(history);
        if (index != -1)
        {
            History old = (History)list.get(index);
            if (!old.ratio.equals(history.ratio))
            {
                list.set(index, history);
                setChanged();
            }
        }
        else
        {
            list.add(history);
            setChanged();
        }
        historyMap.put(from + to, list);
        notifyObservers();
    }
    
    public Double getExchangeRatio(Date date, String from, String to)
    {
        Double ratio = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();

        List list = (ArrayList)historyMap.get(from + to);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                History history = (History)list.get(i);
                if (history.date.equals(date))
                {
                    ratio = history.ratio;
                    break;
                }
            }
        }
        
        return ratio;
    }
    
    private void save()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, "data", null);

            Element root = document.getDocumentElement();
            
            for (Iterator iter = currencies.iterator(); iter.hasNext(); )
            {
                Element node = document.createElement("currency");
                node.appendChild(document.createTextNode((String)iter.next()));
                root.appendChild(node);
            }
            
            for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )
            {
                String symbol = (String)iter.next();
                Element node = document.createElement("conversion");
                node.setAttribute("symbol", symbol);
                node.setAttribute("ratio", String.valueOf((Double)map.get(symbol)));
                saveHistory(node, symbol);
                root.appendChild(node);
            }

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
            
            File file = new File(Platform.getLocation().toFile(), "currencies.xml");
            
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
    
    private void saveHistory(Node root, String symbol)
    {
        Document document = root.getOwnerDocument();
        List list = (List)historyMap.get(symbol);
        if (list != null)
        {
            java.util.Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2)
                {
                    History d1 = (History) o1;
                    History d2 = (History) o2;
                    if (d1.date.after(d2.date) == true)
                        return 1;
                    else if (d1.date.before(d2.date) == true)
                        return -1;
                    return 0;
                }
            });

            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                History history = (History)iter.next();
                Element node = document.createElement("history");
                node.setAttribute("date", dateFormat.format(history.date));
                node.setAttribute("ratio", String.valueOf(history.ratio));
                root.appendChild(node);
            }
        }
    }

    private Double downloadQuote(String symbol)
    {
        Double result = null;
        
        StringBuffer url = new StringBuffer("http://quote.yahoo.com/download/javasoft.beans?symbols=");
        url = url.append(symbol + "=X");
        url.append("&format=sl1d1t1c1ohgvbap");
        
        String line = "";
        try
        {
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            
            IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
            if (store.getBoolean(CorePlugin.PREFS_ENABLE_HTTP_PROXY))
            {
                client.getHostConfiguration().setProxy(store.getString(CorePlugin.PREFS_PROXY_HOST_ADDRESS), store.getInt(CorePlugin.PREFS_PROXY_PORT_ADDRESS));
                if (store.getBoolean(CorePlugin.PREFS_ENABLE_PROXY_AUTHENTICATION))
                    client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(store.getString(CorePlugin.PREFS_PROXY_USER), store.getString(CorePlugin.PREFS_PROXY_PASSWORD)));
            }

            HttpMethod method = new GetMethod(url.toString());
            method.setFollowRedirects(true);
            client.executeMethod(method);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            while ((line = in.readLine()) != null)
            {
                String[] item = line.split(",");
                if (line.indexOf(";") != -1)
                    item = line.split(";");
                
                // 1 = Last price or N/A
                if (item[1].equalsIgnoreCase("N/A") == false)
                    result = new Double(numberFormat.parse(item[1]).doubleValue());
                // 2 = Date
                // 3 = Time
                // 4 = Change
                // 5 = Open
                // 6 = Maximum
                // 7 = Minimum
                // 8 = Volume
                // 9 = Bid Price
                // 10 = Ask Price
                // 11 = Close Price
                
                // 0 = Code
            }
            in.close();
        }
        catch (Exception e) {
            logger.error(e, e);
        }

        return result;
    }

    public Double downloadQuote(String symbol, Date date)
    {
        return null;
    }
}
