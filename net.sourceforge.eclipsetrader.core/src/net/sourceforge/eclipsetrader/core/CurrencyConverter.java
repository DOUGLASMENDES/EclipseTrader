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

package net.sourceforge.eclipsetrader.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
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
    private List currencies = new ArrayList();
    private Map map = new HashMap();
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    private CurrencyConverter()
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
                    Node valueNode = item.getFirstChild();
                    if (valueNode != null)
                    {
                        if (nodeName.equalsIgnoreCase("currency")) //$NON-NLS-1$
                            currencies.add(valueNode.getNodeValue());
                        else if (nodeName.equalsIgnoreCase("conversion")) //$NON-NLS-1$
                        {
                            String symbol = ((Node)item).getAttributes().getNamedItem("symbol").getNodeValue();
                            Double value = new Double(Double.parseDouble(valueNode.getNodeValue()));
                            map.put(symbol, value);
                        }
                    }
                }
            } catch (Exception e) {
                CorePlugin.logException(e);
            }
        }
    }

    public static CurrencyConverter getInstance()
    {
        return instance;
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
        return convert(amount, from.getCurrencyCode(), to.getCurrencyCode());
    }

    public double convert(Double amount, Currency from, Currency to)
    {
        if (from == null || to == null || from.equals(to))
            return amount.doubleValue();
        return convert(amount.doubleValue(), from.getCurrencyCode(), to.getCurrencyCode());
    }

    public double convert(double amount, String from, String to)
    {
        double result = amount;

        if (from == null || to == null || from.equals(to))
            return result;
        
        Double ratio = (Double)map.get(from + to);
        if (ratio == null)
        {
            ratio = (Double)map.get(to + from);
            if (ratio != null)
                result = amount / ratio.doubleValue();
        }
        else
            result = amount * ratio.doubleValue();
        
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
    
    private void save()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, "data", null);

            Element root = document.createElement("data");
            
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
                node.appendChild(document.createTextNode(String.valueOf((Double)map.get(symbol))));
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
            CorePlugin.logException(e);
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
                
                // 2 = Date
                // 3 = Time
                // 1 = Last price or N/A
                if (item[1].equalsIgnoreCase("N/A") == false)
                    result = new Double(numberFormat.parse(item[1]).doubleValue());
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
            CorePlugin.logException(e);
        }

        return result;
    }
}
