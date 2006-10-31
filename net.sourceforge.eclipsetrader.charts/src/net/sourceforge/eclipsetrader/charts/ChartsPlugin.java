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

package net.sourceforge.eclipsetrader.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The main plugin class to be used in the desktop.
 */
public class ChartsPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts";
    public static final String INDICATORS_EXTENSION_POINT = PLUGIN_ID + ".indicators";
    public static final String OBJECTS_EXTENSION_POINT = PLUGIN_ID + ".objects";
    public static final String PREFS_HIDE_TABS = "HIDE_TABS";
    public static final String PREFS_EXTEND_SCALE = "EXTEND_SCALE";
    public static final String PREFS_EXTEND_PERIOD = "EXTEND_PERIOD";
    private static ChartsPlugin plugin;
    private static NumberFormat numberFormat;
    private static NumberFormat percentageFormat;
    private static NumberFormat priceFormat;

    /**
     * The constructor.
     */
    public ChartsPlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        copyFileToStateLocation("defaultChart.xml");
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static ChartsPlugin getDefault()
    {
        return plugin;
    }

    public static NumberFormat getNumberFormat()
    {
        if (numberFormat == null)
        {
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
            numberFormat.setMinimumIntegerDigits(1);
            numberFormat.setMinimumFractionDigits(0);
            numberFormat.setMaximumFractionDigits(0);
        }
        return numberFormat;
    }

    public static NumberFormat getPercentageFormat()
    {
        if (percentageFormat == null)
        {
            percentageFormat = NumberFormat.getInstance();
            percentageFormat.setGroupingUsed(false);
            percentageFormat.setMinimumIntegerDigits(1);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setMaximumFractionDigits(2);
        }
        return percentageFormat;
    }

    public static NumberFormat getPriceFormat()
    {
        if (priceFormat == null)
        {
            priceFormat = NumberFormat.getInstance();
            priceFormat.setGroupingUsed(true);
            priceFormat.setMinimumIntegerDigits(1);
            priceFormat.setMinimumFractionDigits(4);
            priceFormat.setMaximumFractionDigits(4);
        }
        return priceFormat;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    public static IIndicatorPlugin createIndicatorPlugin(String id)
    {
        IConfigurationElement item = getIndicatorPlugin(id);
        if (item != null)
        {
            try {
                Object obj = item.createExecutableExtension("class");
                if (obj instanceof IIndicatorPlugin)
                    return (IIndicatorPlugin)obj;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    public static IConfigurationElement getIndicatorPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.INDICATORS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item;
            }
        }
        
        return null;
    }
    
    public static ObjectPlugin createObjectPlugin(String id)
    {
        IConfigurationElement item = getObjectPlugin(id);
        if (item != null)
        {
            try {
                Object obj = item.createExecutableExtension("class");
                if (obj instanceof ObjectPlugin)
                    return (ObjectPlugin)obj;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    public static IConfigurationElement getObjectPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.OBJECTS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id))
                    return item;
            }
        }
        
        return null;
    }
    
    public static Chart createDefaultChart()
    {
        Logger logger = Logger.getLogger(ChartsPlugin.class);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
        Chart chart = new Chart();

        File file = ChartsPlugin.getDefault().getStateLocation().append("defaultChart.xml").toFile();
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
                        if (nodeName.equalsIgnoreCase("compression") == true)
                            chart.setCompression(Integer.parseInt(valueNode.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("period") == true)
                            chart.setPeriod(Integer.parseInt(valueNode.getNodeValue()));
                        else if (nodeName.equalsIgnoreCase("autoScale") == true)
                            chart.setAutoScale(new Boolean(valueNode.getNodeValue()).booleanValue());
                        else if (nodeName.equalsIgnoreCase("begin") == true)
                        {
                            try {
                                chart.setBeginDate(dateTimeFormat.parse(valueNode.getNodeValue()));
                            } catch (Exception e) {
                                logger.warn(e.toString());
                            }
                        }
                        else if (nodeName.equalsIgnoreCase("end") == true)
                        {
                            try {
                                chart.setEndDate(dateTimeFormat.parse(valueNode.getNodeValue()));
                            } catch (Exception e) {
                                logger.warn(e.toString());
                            }
                        }
                    }
                    if (nodeName.equalsIgnoreCase("row")) //$NON-NLS-1$
                    {
                        ChartRow row = new ChartRow(new Integer(r));
                        row.setParent(chart);
                        
                        NodeList tabList = item.getChildNodes();
                        for (int t = 0; t < tabList.getLength(); t++)
                        {
                            item = tabList.item(t);
                            nodeName = item.getNodeName();
                            if (nodeName.equalsIgnoreCase("tab")) //$NON-NLS-1$
                            {
                                ChartTab tab = new ChartTab(new Integer(t));
                                tab.setParent(row);
                                tab.setLabel(((Node)item).getAttributes().getNamedItem("label").getNodeValue());

                                NodeList indicatorList = item.getChildNodes();
                                for (int i = 0; i < indicatorList.getLength(); i++)
                                {
                                    item = indicatorList.item(i);
                                    nodeName = item.getNodeName();
                                    if (nodeName.equalsIgnoreCase("indicator")) //$NON-NLS-1$
                                    {
                                        ChartIndicator indicator = new ChartIndicator(new Integer(i));
                                        indicator.setParent(tab);
                                        indicator.setPluginId(((Node)item).getAttributes().getNamedItem("pluginId").getNodeValue());

                                        NodeList parametersList = item.getChildNodes();
                                        for (int p = 0; p < parametersList.getLength(); p++)
                                        {
                                            item = parametersList.item(p);
                                            nodeName = item.getNodeName();
                                            if (nodeName.equalsIgnoreCase("param")) //$NON-NLS-1$
                                            {
                                                String key = ((Node)item).getAttributes().getNamedItem("key").getNodeValue(); 
                                                String value = ((Node)item).getAttributes().getNamedItem("value").getNodeValue();
                                                indicator.getParameters().put(key, value);
                                            }
                                        }
                                        
                                        tab.getIndicators().add(indicator);
                                    }
                                    else if (nodeName.equalsIgnoreCase("object")) //$NON-NLS-1$
                                    {
                                        ChartObject object = new ChartObject(new Integer(i));
                                        object.setParent(tab);
                                        object.setPluginId(((Node)item).getAttributes().getNamedItem("pluginId").getNodeValue());

                                        NodeList parametersList = item.getChildNodes();
                                        for (int p = 0; p < parametersList.getLength(); p++)
                                        {
                                            item = parametersList.item(p);
                                            nodeName = item.getNodeName();
                                            if (nodeName.equalsIgnoreCase("param")) //$NON-NLS-1$
                                            {
                                                String key = ((Node)item).getAttributes().getNamedItem("key").getNodeValue(); 
                                                String value = ((Node)item).getAttributes().getNamedItem("value").getNodeValue();
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
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
        
        chart.clearChanged();

        return chart;
    }
    
    public static void saveDefaultChart(Chart chart)
    {
        Logger logger = Logger.getLogger(ChartsPlugin.class);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.getDOMImplementation().createDocument(null, "chart", null);

            Element root = document.getDocumentElement();
            
            Element node = document.createElement("title");
            node.appendChild(document.createTextNode(chart.getTitle()));
            root.appendChild(node);
            node = document.createElement("compression");
            node.appendChild(document.createTextNode(String.valueOf(chart.getCompression())));
            root.appendChild(node);
            node = document.createElement("period");
            node.appendChild(document.createTextNode(String.valueOf(chart.getPeriod())));
            root.appendChild(node);
            node = document.createElement("autoScale");
            node.appendChild(document.createTextNode(String.valueOf(chart.isAutoScale())));
            root.appendChild(node);
            if (chart.getBeginDate() != null)
            {
                node = document.createElement("begin");
                node.appendChild(document.createTextNode(dateTimeFormat.format(chart.getBeginDate())));
                root.appendChild(node);
            }
            if (chart.getEndDate() != null)
            {
                node = document.createElement("end");
                node.appendChild(document.createTextNode(dateTimeFormat.format(chart.getEndDate())));
                root.appendChild(node);
            }
            for (int r = 0; r < chart.getRows().size(); r++)
            {
                ChartRow row = (ChartRow)chart.getRows().get(r);
                row.setId(new Integer(r));

                Element rowNode = document.createElement("row");
                root.appendChild(rowNode);

                for (int t = 0; t < row.getTabs().size(); t++)
                {
                    ChartTab tab = (ChartTab)row.getTabs().get(t);
                    tab.setId(new Integer(t));

                    Element tabNode = document.createElement("tab");
                    tabNode.setAttribute("label", tab.getLabel());
                    rowNode.appendChild(tabNode);

                    for (int i = 0; i < tab.getIndicators().size(); i++)
                    {
                        ChartIndicator indicator = (ChartIndicator)tab.getIndicators().get(i);
                        indicator.setId(new Integer(i));

                        Element indicatorNode = document.createElement("indicator");
                        indicatorNode.setAttribute("pluginId", indicator.getPluginId());
                        tabNode.appendChild(indicatorNode);

                        for (Iterator iter = indicator.getParameters().keySet().iterator(); iter.hasNext(); )
                        {
                            String key = (String)iter.next();

                            node = document.createElement("param");
                            node.setAttribute("key", key);
                            node.setAttribute("value", (String)indicator.getParameters().get(key));
                            indicatorNode.appendChild(node);
                        }
                    }

                    for (int i = 0; i < tab.getObjects().size(); i++)
                    {
                        ChartObject object = (ChartObject)tab.getObjects().get(i);
                        object.setId(new Integer(i));

                        Element indicatorNode = document.createElement("object");
                        indicatorNode.setAttribute("pluginId", object.getPluginId());
                        tabNode.appendChild(indicatorNode);

                        for (Iterator iter = object.getParameters().keySet().iterator(); iter.hasNext(); )
                        {
                            String key = (String)iter.next();

                            node = document.createElement("param");
                            node.setAttribute("key", key);
                            node.setAttribute("value", (String)object.getParameters().get(key));
                            indicatorNode.appendChild(node);
                        }
                    }
                }
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
            
            File file = ChartsPlugin.getDefault().getStateLocation().append("defaultChart.xml").toFile();
            
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.flush();
            out.close();

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    private void copyFileToStateLocation(String file)
    {
        File f = ChartsPlugin.getDefault().getStateLocation().append(file).toFile();
        if (!f.exists())
        {
            try
            {
                byte[] buffer = new byte[10240];
                OutputStream os = new FileOutputStream(f);
                InputStream is = FileLocator.openStream(getBundle(), new Path("data/" + file), false);
                int readed = 0;
                do
                {
                    readed = is.read(buffer);
                    os.write(buffer, 0, readed);
                } while (readed == buffer.length);
                os.close();
                is.close();
            }
            catch (Exception e) {
                CorePlugin.logException(e);
            }
        }
    }
}
