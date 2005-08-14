/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;
import net.sourceforge.eclipsetrader.ui.views.charts.indicators.MA;
import net.sourceforge.eclipsetrader.ui.views.charts.indicators.VOL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ChartView extends ViewPart implements ControlListener, MouseListener, MouseMoveListener, PaintListener, SelectionListener
{
  protected Color background = new Color(null, 255, 255, 255);
  protected Color lineColor = new Color(null, 0, 0, 255);
  protected Color textColor = new Color(null, 0, 0, 0);
  protected Color positiveColor = new Color(null, 0, 192, 0);
  protected Color negativeColor = new Color(null, 192, 0, 0);
  protected Color yearColor = new Color(null, 192, 0, 0);
  protected int width = 5;
  protected int margin = 2;
  protected int scaleWidth = 60;
  protected int limitPeriod = 12;
  protected Composite container;
  protected Composite composite;
  protected SashForm form;
  protected Composite bottombar;
  protected Label title;
  protected Label date;
  protected Label dateLabel;
  protected Label closePrice;
  protected Label maxPrice;
  protected Label minPrice;
  protected Label variance;
  protected Label volume;
  protected IChartDataProvider dataProvider;
  protected IBasicData basicData;
  protected IChartData[] data = new IChartData[0];
  protected List chart = new ArrayList();
  protected NumberFormat nf = NumberFormat.getInstance();
  protected NumberFormat pf = NumberFormat.getInstance();
  protected NumberFormat pcf = NumberFormat.getInstance();
  protected SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
  private BarData barData = new BarData();
  
  public ChartView()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);

    pcf.setMinimumIntegerDigits(1);
    pcf.setMinimumFractionDigits(2);
    pcf.setMaximumFractionDigits(2);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    container = new Composite(parent, SWT.H_SCROLL);
    container.getHorizontalBar().addSelectionListener(this);
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    container.setLayout(gridLayout);
    container.addControlListener(this);
    container.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e)
      {
      }
      public void focusLost(FocusEvent e)
      {
      }
    });
    
    composite = new Composite(container, SWT.NONE);
    composite.setBackground(background);
    gridLayout = new GridLayout(20, false);
    gridLayout.marginWidth = 3;
    gridLayout.marginHeight = 1;
    gridLayout.horizontalSpacing = 5;
    gridLayout.verticalSpacing = 0;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_FILL));
    composite.addPaintListener(this);
    title = new Label(composite, SWT.NONE);
    title.setBackground(title.getParent().getBackground());
    title.setLayoutData(new GridData());
    dateLabel = new Label(composite, SWT.NONE);
    dateLabel.setText(Messages.getString("ChartView.Date")); //$NON-NLS-1$
    dateLabel.setBackground(title.getParent().getBackground());
    dateLabel.setLayoutData(new GridData());
    date = new Label(composite, SWT.NONE);
    date.setForeground(lineColor);
    date.setBackground(title.getParent().getBackground());
    date.setLayoutData(new GridData());
    Label label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartView.Value")); //$NON-NLS-1$
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    closePrice = new Label(composite, SWT.NONE);
    closePrice.setForeground(lineColor);
    closePrice.setBackground(title.getParent().getBackground());
    closePrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartView.Max")); //$NON-NLS-1$
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    maxPrice = new Label(composite, SWT.NONE);
    maxPrice.setForeground(lineColor);
    maxPrice.setBackground(title.getParent().getBackground());
    maxPrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartView.Min")); //$NON-NLS-1$
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    minPrice = new Label(composite, SWT.NONE);
    minPrice.setForeground(lineColor);
    minPrice.setBackground(title.getParent().getBackground());
    minPrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartView.Change")); //$NON-NLS-1$
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    variance = new Label(composite, SWT.NONE);
    variance.setForeground(lineColor);
    variance.setBackground(title.getParent().getBackground());
    variance.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartView.Volume")); //$NON-NLS-1$
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    volume = new Label(composite, SWT.NONE);
    volume.setForeground(lineColor);
    volume.setBackground(title.getParent().getBackground());
    volume.setLayoutData(new GridData());

    form = new SashForm(container, SWT.VERTICAL);
    form.setLayoutData(new GridData(GridData.FILL_BOTH));

    bottombar = new Canvas(container, SWT.NONE);
    bottombar.setBackground(background);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_FILL);
    gridData.heightHint = 20;
    bottombar.setLayoutData(gridData);
    bottombar.addPaintListener(this);
    
    // Set the selection provider for this site
    getSite().setSelectionProvider(new ChartSelectionProvider());
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    container.forceFocus();
  }

  public abstract void reloadPreferences();
  
  public void reloadPreferences(File folder)
  {
    List sectionHeights = new ArrayList();

    // Remove all charts
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.get(i);
      canvas.getChart().removeMouseListener(this);
      canvas.getChart().removeMouseMoveListener(this);
      canvas.select(null);
      canvas.dispose();
    }
    chart.clear();
    limitPeriod = 12;

    // Read the preferences files for the new chart
    File f = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs"); //$NON-NLS-1$
    if (f.exists() == true)
    {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("settings")) //$NON-NLS-1$
          {
            Node attr = n.getAttributes().getNamedItem("limit"); //$NON-NLS-1$
            if (attr != null)
              limitPeriod = Integer.parseInt(attr.getNodeValue());
          }
          else if (n.getNodeName().equalsIgnoreCase("section")) //$NON-NLS-1$
          {
            ChartCanvas canvas = new ChartCanvas(form, SWT.NONE);
            canvas.registerContextMenu(getSite());
            canvas.getChart().addMouseListener(this);
            canvas.getChart().addMouseMoveListener(this);
            if (chart.size() == 0)
              canvas.setHilight(true);
            chart.add(canvas);

            // Standard attributes
            Node attr = n.getAttributes().getNamedItem("main"); //$NON-NLS-1$
            if (attr != null && attr.getNodeValue().equalsIgnoreCase("true") == true) //$NON-NLS-1$
            {
              canvas.setMainChart(true);
              attr = n.getAttributes().getNamedItem("style"); //$NON-NLS-1$
              canvas.setStyle(Integer.parseInt(attr.getNodeValue()));
            }
            String height = n.getAttributes().getNamedItem("height").getNodeValue(); //$NON-NLS-1$
            sectionHeights.add(new Integer(height));

            // Indicators
            NodeList parent = n.getChildNodes();
            for (int ii = 0; ii < parent.getLength(); ii++)
            {
              Node item = parent.item(ii);
              if (item.getNodeName().equalsIgnoreCase("indicator") == true) //$NON-NLS-1$
              {
                String clazz = item.getAttributes().getNamedItem("class").getNodeValue(); //$NON-NLS-1$
                try {
                  Object obj = Class.forName(clazz).newInstance();
                  if (obj instanceof IndicatorPlugin)
                  {
                    IndicatorPlugin indicator = (IndicatorPlugin)obj;
                    canvas.getIndicators().add(indicator);
                    setObjectParameters((NodeList)item, indicator);
                  }
                  else
                    TraderPlugin.log("Class " + obj.getClass().getName() + " is not an instanceof " + IndicatorPlugin.class.getName());
                } catch(Exception e) {
                  e.printStackTrace();
                }
              }
              else if (item.getNodeName().equalsIgnoreCase("tool") == true) //$NON-NLS-1$
              {
                String clazz = item.getAttributes().getNamedItem("class").getNodeValue(); //$NON-NLS-1$
                try {
                  Object obj = Class.forName(clazz).newInstance();
                  if (obj instanceof ToolPlugin)
                  {
                    ToolPlugin tool = (ToolPlugin)obj;
                    canvas.getTools().add(tool);
                    setObjectParameters((NodeList)item, tool);
                  }
                  else
                    TraderPlugin.log("Class " + obj.getClass().getName() + " is not an instanceof " + ToolPlugin.class.getName());
                } catch(Exception e) {
                  e.printStackTrace();
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      int[] weights = new int[sectionHeights.size()];
      for (int i = 0; i < weights.length; i++)
        weights[i] = ((Integer)sectionHeights.get(i)).intValue();
      form.setWeights(weights);
    }
    else
    {
      ChartCanvas canvas = new ChartCanvas(form, SWT.NONE);
      canvas.registerContextMenu(getSite());
      canvas.getChart().addMouseListener(this);
      canvas.getChart().addMouseMoveListener(this);
      canvas.setHilight(true);
      canvas.setMainChart(true);
      chart.add(canvas);
      IndicatorPlugin plugin = new MA();
      plugin.setParameter("period", "7");
      plugin.setParameter("color", "0,208,0");
      canvas.getIndicators().add(plugin);
      plugin = new MA();
      plugin.setParameter("period", "21");
      plugin.setParameter("color", "255,0,0");
      canvas.getIndicators().add(plugin);
      
      canvas = new ChartCanvas(form, SWT.NONE);
      canvas.registerContextMenu(getSite());
      chart.add(canvas);
      canvas.getChart().addMouseListener(this);
      canvas.getChart().addMouseMoveListener(this);
      canvas.getIndicators().add(new VOL());

      int[] weights = { 85, 15 };
      form.setWeights(weights);
    }

    updateLabels();
    getSite().getSelectionProvider().setSelection(new ChartSelection(getSelectedZone()));
  }
  
  private void setObjectParameters(NodeList parent, ChartObject obj)
  {
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node node = parent.item(i);
      if (node.getNodeName().equalsIgnoreCase("params") == true) //$NON-NLS-1$
      {
        NamedNodeMap map = node.getAttributes();
        for (int ii = 0; ii < map.getLength(); ii++)
        {
          String name = map.item(ii).getNodeName();
          String value = map.item(ii).getNodeValue();
          if (name != null && value != null)
            obj.setParameter(name, value);
        }
      }
    }
  }

  /**
   * Save the chart's preference to an XML file.
   * <p></p>
   */
  public abstract void savePreferences();
  
  public void savePreferences(File folder)
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "preferences", null); //$NON-NLS-1$ //$NON-NLS-2$

      Element element = document.createElement("settings"); //$NON-NLS-1$
      element.setAttribute("limit", String.valueOf(limitPeriod)); //$NON-NLS-1$
      document.getDocumentElement().appendChild(element);

      int[] weights = form.getWeights();
      for (int i = 0; i < weights.length; i++)
      {
        element = document.createElement("section"); //$NON-NLS-1$
        element.setAttribute("height", String.valueOf(weights[i])); //$NON-NLS-1$
        ChartCanvas canvas = (ChartCanvas)chart.get(i);
        if (canvas.isMainChart())
        {
          element.setAttribute("main", "true"); //$NON-NLS-1$ //$NON-NLS-2$
          element.setAttribute("style", String.valueOf(canvas.getStyle())); //$NON-NLS-1$
        }
        document.getDocumentElement().appendChild(element);

        // Save the indicators configuration
        for (int ii = 0; ii < canvas.getIndicators().size(); ii++)
        {
          IndicatorPlugin indicator = (IndicatorPlugin)canvas.getIndicators().get(ii);

          // Set the standard attributes
          Element node = document.createElement("indicator"); //$NON-NLS-1$
          node.setAttribute("class", indicator.getClass().getName()); //$NON-NLS-1$
//          if (indicator.getName() != null)
//            node.setAttribute("name", indicator.getName()); //$NON-NLS-1$
          
          // Append the parameters map
          Map params = indicator.getParameters();
          Iterator keys = params.keySet().iterator();
          while (keys.hasNext())
          {
            String key = (String)keys.next();
            if (params.get(key) != null)
            {
              Element p = document.createElement("params"); //$NON-NLS-1$
              p.setAttribute(key, (String)params.get(key));
              node.appendChild(p);
            }
          }
          
          element.appendChild(node);
        }
        
        // Save the tools configuration
        for (int ii = 0; ii < canvas.getTools().size(); ii++)
        {
          ToolPlugin tool = canvas.getTools().get(ii);
          
          Element node = document.createElement("tool"); //$NON-NLS-1$
          node.setAttribute("class", tool.getClass().getName()); //$NON-NLS-1$

          Map params = tool.getParameters();
          Iterator keys = params.keySet().iterator();
          while (keys.hasNext())
          {
            String key = (String)keys.next();
            if (params.get(key) != null)
            {
              Element p = document.createElement("params"); //$NON-NLS-1$
              p.setAttribute(key, (String)params.get(key));
              node.appendChild(p);
            }
          }
          
          element.appendChild(node);
        }
      }

      // XML transform
      File f = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs"); //$NON-NLS-1$
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //$NON-NLS-1$
      transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) { ex.printStackTrace(); };
  }

  public abstract void setData(final IBasicData d);
  public abstract IChartData[] getChartData(IBasicData data);

  public void setData(final IBasicData d, String stitle, String prefId)
  {
    basicData = d;
    reloadPreferences();

    String id = getViewSite().getSecondaryId();
    ViewsPlugin.getDefault().getPreferenceStore().setValue(prefId + id, basicData.getSymbol());
    setPartName(stitle);

    data = getChartData(basicData);
    if (data != null)
    {
      container.getDisplay().asyncExec(new Runnable() {
        public void run() {
          if (data != null && data.length > 0)
          {
            if (data[0].getMaxPrice() >= 10)
            {
              pf.setMinimumFractionDigits(2);
              pf.setMaximumFractionDigits(2);
              setScaleWidth(42);
            }
            else
            {
              pf.setMinimumFractionDigits(4);
              pf.setMaximumFractionDigits(4);
              setScaleWidth(50);
            }
          }
          else
            setScaleWidth(50);
          controlResized(null);
          bottombar.redraw();
          title.setText(basicData.getDescription());
          updateLabels();
        }
      });

      barData.clear();
      barData.addAll(data);
      for (int i = 0; i < chart.size(); i++)
        ((ChartCanvas)chart.get(i)).setBarData(barData);
    }
  }
  
  public boolean isIndex(IBasicData d)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
                return true;
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              return true;
          }
        }
      }
    }
    
    return false;
  }
  
  public IIndexDataProvider getIndexProvider(IBasicData d)
  {
    String EXTENSION_POINT_ID = "net.sourceforge.eclipsetrader.indexProvider"; //$NON-NLS-1$

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              {
                IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, members[i].getAttribute("id")); //$NON-NLS-1$
                return ip;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
            {
              IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, members[i].getAttribute("id")); //$NON-NLS-1$
              return ip;
            }
          }
        }
      }
    }
    
    return null;
  }

  public void updateIndexData(IBasicData d)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              String ticker = items[iii].getAttribute("ticker"); //$NON-NLS-1$
              String label = items[iii].getAttribute("label"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              {
                if (ticker.length() != 0)
                  d.setTicker(ticker);
                d.setDescription(label);
                return;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            String ticker = children[ii].getAttribute("ticker"); //$NON-NLS-1$
            String label = children[ii].getAttribute("label"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
            {
              if (ticker.length() != 0)
                d.setTicker(ticker);
              d.setDescription(label);
              return;
            }
          }
        }
      }
    }
  }

  /**
   * Adds a new zone to the view relative to the given reference zone.
   * 
   * @param reference - the refrence zone
   * @param position - 0=below the reference zone, 1=above the reference zone
   * @return
   */
  public ChartCanvas addZone(ChartCanvas reference, int position)
  {
    int[] w = form.getWeights();

    ChartCanvas canvas = new ChartCanvas(form, SWT.NONE);
    canvas.registerContextMenu(getSite());
    canvas.getChart().addMouseListener(this);
    canvas.getChart().addMouseMoveListener(this);
    canvas.setBarData(barData);

    int[] weights = new int[chart.size()];
    for (int i = 0; i < w.length; i++)
      weights[i] = w[i];

    if (container.getHorizontalBar() != null)
      canvas.setChartOrigin(0 - container.getHorizontalBar().getSelection());

    if (position == 0)
    {
      canvas.moveBelow(reference);
      chart.add(chart.indexOf(reference) + 1, canvas);
    }
    else
    {
      canvas.moveAbove(reference);
      chart.add(chart.indexOf(reference), canvas);
    }
    weights[chart.indexOf(canvas)] = 120;
    form.layout();

    ChartCanvas selectedZone = getSelectedZone();
    selectedZone.deselectAll();
    selectedZone.setHilight(false);
    selectedZone = canvas;
    selectedZone.setHilight(true);
    getSite().getSelectionProvider().setSelection(new ChartSelection(selectedZone));
    
    return canvas;
  }
  
  public void removeZone(ChartCanvas canvas)
  {
    canvas.removeMouseListener(this);
    canvas.removeMouseMoveListener(this);
    canvas.dispose();
    chart.remove(chart.indexOf(canvas));
    form.layout();
  }
  
  public void updateView()
  {
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.get(i)).setBarData(barData);
  }
  
  public ToolsCollection getTools()
  {
    return ((ChartCanvas)chart.get(0)).getTools();
  }
  
  public int getChartType()
  {
/*    for (int i = chart.size() - 1; i >= 0; i--)
    {
      ChartCanvas canvas = (ChartCanvas)chart.get(i);
      for (int ii = canvas.getPainterCount() - 1; ii >= 0; ii--)
      {
        if (canvas.getPainter(ii) instanceof PriceChart)
        {
          String param = (String)canvas.getPainter(ii).getParameters().get("type"); //$NON-NLS-1$
          if (param != null)
            return Integer.parseInt(param);
        }
      }
    }*/
    return PriceChart.LINE;
  }
  
  public void setChartType(int type)
  {
/*    for (int i = chart.size() - 1; i >= 0; i--)
    {
      ChartCanvas canvas = (ChartCanvas)chart.get(i);
      for (int ii = canvas.getPainterCount() - 1; ii >= 0; ii--)
      {
        if (canvas.getPainter(ii) instanceof PriceChart)
        {
          canvas.getPainter(ii).setParameter("type", String.valueOf(type)); //$NON-NLS-1$
          updateView();
          savePreferences();
          break;
        }
      }
    }*/
  }

  public void updateLabels()
  {
    updateLabels(data.length - 1);
  }
  
  public void updateLabels(int index)
  {
    if (data.length > 0)
    {
      date.setText(df.format(data[index].getDate()));
      closePrice.setText(pf.format(data[index].getClosePrice()));
      maxPrice.setText(pf.format(data[index].getMaxPrice()));
      minPrice.setText(pf.format(data[index].getMinPrice()));
      if (index >= 1)
      {
        double pc = data[index].getClosePrice() - data[index - 1].getClosePrice();
        pc = pc / data[index - 1].getClosePrice();
        variance.setText(pcf.format(pc * 100) + "%"); //$NON-NLS-1$
        if (pc > 0)
          variance.setForeground(positiveColor);
        else if (pc < 0)
          variance.setForeground(negativeColor);
        else
          variance.setForeground(textColor);
      }
      else
      {
        double pc = data[index].getClosePrice() - data[index].getOpenPrice();
        pc = pc / data[index].getOpenPrice();
        variance.setText(pcf.format(pc * 100) + "%"); //$NON-NLS-1$
        if (pc > 0)
          variance.setForeground(positiveColor);
        else if (pc < 0)
          variance.setForeground(negativeColor);
        else
          variance.setForeground(textColor);
      }
      volume.setText(nf.format(data[index].getVolume()));
    }
    else
    {
      date.setText(""); //$NON-NLS-1$
      closePrice.setText(""); //$NON-NLS-1$
      maxPrice.setText(""); //$NON-NLS-1$
      minPrice.setText(""); //$NON-NLS-1$
      variance.setText(""); //$NON-NLS-1$
      volume.setText(""); //$NON-NLS-1$
    }
    composite.layout();
    
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.get(i)).updateLabels(index);
  }
  
  protected void setWidth(int width)
  {
    this.width = width;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.get(i)).setColumnWidth(width);
  }
  
  protected void setMargin(int margin)
  {
    this.margin = margin;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.get(i)).setMargin(margin);
  }
  
  public void setScaleWidth(int scaleWidth)
  {
    this.scaleWidth = scaleWidth;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.get(i)).setScaleWidth(scaleWidth);
  }

  /**
   * Method to return the limitPeriod field.<br>
   * @return Returns the limitPeriod.
   */
  public int getLimitPeriod()
  {
    return limitPeriod;
  }
  /**
   * Method to set the limitPeriod field.<br>
   * @param limitPeriod The limitPeriod to set.
   */
  public void setLimitPeriod(int limitPeriod)
  {
    this.limitPeriod = limitPeriod;
    savePreferences();
    setData(basicData);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    GC gc = e.gc;
    
    if (e.getSource() == bottombar)
    {
      gc.setForeground(textColor);
      gc.drawLine(0, 0, bottombar.getClientArea().width, 0); 

      if (data != null)
      {
        int month = -1;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM"); //$NON-NLS-1$
        
        gc.setClipping(0, 0, bottombar.getClientArea().width - scaleWidth, bottombar.getClientArea().height);

        int x = margin + width / 2;
        if (container.getHorizontalBar().isVisible() == true)
          x -= container.getHorizontalBar().getSelection();
        for (int i = 0; i < data.length; i++, x += width)
        {
          c.setTime(data[i].getDate());
          if (c.get(Calendar.MONTH) != month)
          {
            String s = df.format(data[i].getDate());
            int x1 = x - gc.stringExtent(s).x / 2;
            gc.setForeground(textColor);
            if (c.get(Calendar.MONTH) == Calendar.JANUARY)
            {
              s += " " + c.get(Calendar.YEAR); //$NON-NLS-1$
              gc.setForeground(yearColor);
            }
            gc.drawLine(x, 0, x, 5);
            gc.drawString(s, x1, 5);
            month = c.get(Calendar.MONTH);
          }
        }
      }
    }
    else if (e.getSource() instanceof Composite)
    {
      gc.setForeground(textColor);
      Composite c = (Composite)e.getSource();
      gc.drawLine(0, c.getClientArea().height - 1, c.getClientArea().width, c.getClientArea().height - 1); 
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
   */
  public void controlMoved(ControlEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
   */
  public void controlResized(ControlEvent e)
  {
    ScrollBar sb = container.getHorizontalBar();
    if (data != null)
    {
      if (sb != null && (data.length * width + margin * 2) > container.getClientArea().width - scaleWidth)
      {
        sb.setMaximum(data.length * width + margin * 2);
        sb.setMinimum(0);
        sb.setIncrement(6);
        sb.setPageIncrement(container.getClientArea().width - scaleWidth);
        sb.setThumb(container.getClientArea().width - scaleWidth);
        sb.setSelection(sb.getMaximum() - sb.getThumb());
        sb.setVisible(true);
        sb.setEnabled(true);
        for (int i = 0; i < chart.size(); i++)
          ((ChartCanvas)chart.get(i)).setChartOrigin(0 - sb.getSelection());
      }
      else
      {
        sb.setVisible(false);
        for (int i = 0; i < chart.size(); i++)
          ((ChartCanvas)chart.get(i)).setChartOrigin(0);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetSelected(SelectionEvent e)
  {
    if (e.getSource() instanceof ScrollBar)
    {
      ScrollBar sb = (ScrollBar)e.getSource();
      for (int i = 0; i < chart.size(); i++)
        ((ChartCanvas)chart.get(i)).setChartOrigin(0 - sb.getSelection());
      bottombar.redraw();
    }
  }
  
  public ChartCanvas getSelectedZone()
  {
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.get(i);
      if (canvas.isHilight())
        return canvas;
    }
    
    return null;
  }
  
  public ChartCanvas getMainChart()
  {
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.get(i);
      if (canvas.isMainChart())
        return canvas;
    }
    
    return null;
  }

  private boolean mouseDown = false;
  private int mousePreviousX = -1;
  private int mousePreviousY = -1;
  private GC[] mouseGC;
  private Cursor crossCursor = new Cursor(null, SWT.CURSOR_CROSS);
  private Cursor handCursor = new Cursor(null, SWT.CURSOR_HAND);
  private Cursor arrowCursor = new Cursor(null, SWT.CURSOR_ARROW);
  private Cursor lastSetCursor = arrowCursor;
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown(MouseEvent e)
  {
    ChartCanvas selectedZone = getSelectedZone();

    // Check if the mouse was clicked on a zone different than the currently selected one
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.get(i);
      if (e.getSource() == canvas.getChart())
      {
        if (selectedZone != canvas)
        {
          selectedZone.deselectAll();
          selectedZone.setHilight(false);
          selectedZone = canvas;
          selectedZone.setHilight(true);
          getSite().getSelectionProvider().setSelection(new ChartSelection(selectedZone));
          if (e.button == 1)
            return;
        }
        break;
      }
    }

    // Get the chart object at the mouse position
    Object selectedItem = selectedZone.getSelectedItem();
    Object newSelection = selectedZone.getItemAt(e.x, e.y);
    if (newSelection != selectedItem)
    {
      if (e.button == 3 && newSelection == null)
        return;
      selectedZone.select(newSelection);
      getSite().getSelectionProvider().setSelection(new ChartSelection(selectedZone));
      if (e.button == 1 && !(newSelection instanceof ToolPlugin))
        return;
      selectedItem = newSelection;
    }
    
    if (e.button == 1)
    {
      mouseDown = true;
      mouseGC = new GC[chart.size()];
      for (int i = 0; i < mouseGC.length; i++)
      {
        mouseGC[i] = new GC(((ChartCanvas)chart.get(i)).getChart());
        mouseGC[i].setForeground(new Color(null, 0, 0, 0));
      }
      mousePreviousX = -1;
      mousePreviousY = -1;
      mouseMove(e);

      if (selectedItem instanceof ToolPlugin)
        ((ToolPlugin)selectedItem).mousePressed(e);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp(MouseEvent e)
  {
    if (e.button == 1)
    {
      mouseDown = false;
      if (mousePreviousX != -1)
      {
        Object obj = getSelectedZone().getSelectedItem();
        if (obj instanceof ToolPlugin)
        {
          ((ToolPlugin)obj).mouseReleased(e);
          savePreferences();
        }

        for (int i = 0; i < mouseGC.length; i++)
        {
          ChartCanvas chartCanvas = (ChartCanvas)chart.get(i); 
          Canvas canvas = chartCanvas.getChart();
          if (mousePreviousX != -1)
            canvas.redraw(mousePreviousX, 0, 1, canvas.getClientArea().height, true);
          if (chartCanvas.isHilight() && mousePreviousY != -1)
          {
            canvas.redraw(0, mousePreviousY, canvas.getClientArea().width, 1, true);
            chartCanvas.updateScaleLabel(-1);
          }
        }
        mousePreviousX = -1;
        mousePreviousY = -1;
        for (int i = 0; i < mouseGC.length; i++)
          mouseGC[i].dispose();
      }
      updateLabels();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove(MouseEvent e)
  {
    ChartCanvas selectedZone = getSelectedZone();
    Object obj = selectedZone.getSelectedItem();

    if (mouseDown == true)
    {
      int index = (e.x - margin) / width;

      if (index >= 0 && index < data.length)
        updateLabels(index);
      else
        updateLabels();
      
      if (obj instanceof ToolPlugin)
        ((ToolPlugin)obj).mouseDragged(e);
      
      for (int i = 0; i < mouseGC.length; i++)
      {
        ChartCanvas chartCanvas = (ChartCanvas)chart.get(i); 
        Canvas cc = chartCanvas.getChart();
        if (mousePreviousX != -1 && mousePreviousX != e.x)
          cc.redraw(mousePreviousX, 0, 1, cc.getClientArea().height, true);
        if (chartCanvas.isHilight() && mousePreviousY != -1 && mousePreviousY != e.y)
          cc.redraw(0, mousePreviousY, cc.getClientArea().width, 1, true);

        cc.update();
        
        mouseGC[i].drawLine(e.x, 0, e.x, cc.getClientArea().height);
        if (chartCanvas.isHilight())
          mouseGC[i].drawLine(0, e.y, cc.getClientArea().width, e.y);
      }

      selectedZone.updateScaleLabel(e.y);

      mousePreviousX = e.x;
      mousePreviousY = e.y;
    }
    else
    {
      if (obj == null)
        obj = selectedZone.getItemAt(e.x, e.y);
      if (obj == null)
      {
        if (lastSetCursor != arrowCursor)
        {
          selectedZone.getChart().setCursor(arrowCursor);
          lastSetCursor = arrowCursor;
        }
      }
      if (obj instanceof ToolPlugin)
      {
        if (((ToolPlugin)obj).isOnHandle(e.x, e.y))
        {
          if (lastSetCursor != crossCursor)
          {
            selectedZone.getChart().setCursor(crossCursor);
            lastSetCursor = crossCursor;
          }
        }
        else if (((ToolPlugin)obj).containsPoint(e.x, e.y))
        {
          if (lastSetCursor != handCursor)
          {
            selectedZone.getChart().setCursor(handCursor);
            lastSetCursor = handCursor;
          }
        }
        else if (lastSetCursor != arrowCursor)
        {
          selectedZone.getChart().setCursor(arrowCursor);
          lastSetCursor = arrowCursor;
        }
      }
    }
  }
}
