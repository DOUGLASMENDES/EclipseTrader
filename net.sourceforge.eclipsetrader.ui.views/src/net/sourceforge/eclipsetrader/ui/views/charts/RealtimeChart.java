/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IRealtimeChartListener;
import net.sourceforge.eclipsetrader.IRealtimeChartProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.internal.ChartData;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RealtimeChart extends ViewPart implements IRealtimeChartListener, ControlListener, DropTargetListener, MouseListener, MouseMoveListener, PaintListener, SelectionListener
{
  private Color background = new Color(null, 255, 255, 255);
  private Color lineColor = new Color(null, 0, 0, 255);
  private Color textColor = new Color(null, 0, 0, 0);
  private Color positiveColor = new Color(null, 0, 192, 0);
  private Color negativeColor = new Color(null, 192, 0, 0);
  private int width = 5;
  private int margin = 2;
  private int scaleWidth = 60;
  private Composite container;
  private Composite composite;
  private SashForm form;
  private Composite bottombar;
  private Label title;
  private Label date;
  private Label closePrice;
  private Label maxPrice;
  private Label minPrice;
  private Label variance;
  private Label volume;
  private IBasicData basicData;
  private IChartData[] data = new IChartData[0];
  private Vector chart = new Vector();
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("HH:mm");
  private int selectedZone = 0;
  
  public RealtimeChart()
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
    
    composite = new Composite(container, SWT.NONE);
    composite.setBackground(background);
    gridLayout = new GridLayout(20, false);
    gridLayout.marginWidth = 3;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 5;
    gridLayout.verticalSpacing = 0;
    composite.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_FILL);
    gridData.heightHint = 16;
    composite.setLayoutData(gridData);
    composite.addPaintListener(this);
    title = new Label(composite, SWT.NONE);
    title.setBackground(title.getParent().getBackground());
    title.setLayoutData(new GridData());
    Label label = new Label(composite, SWT.NONE);
    label.setText("Ora:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    date = new Label(composite, SWT.NONE);
    date.setForeground(lineColor);
    date.setBackground(title.getParent().getBackground());
    date.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Val.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    closePrice = new Label(composite, SWT.NONE);
    closePrice.setForeground(lineColor);
    closePrice.setBackground(title.getParent().getBackground());
    closePrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Max.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    maxPrice = new Label(composite, SWT.NONE);
    maxPrice.setForeground(lineColor);
    maxPrice.setBackground(title.getParent().getBackground());
    maxPrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Min.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    minPrice = new Label(composite, SWT.NONE);
    minPrice.setForeground(lineColor);
    minPrice.setBackground(title.getParent().getBackground());
    minPrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Var.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    variance = new Label(composite, SWT.NONE);
    variance.setForeground(lineColor);
    variance.setBackground(title.getParent().getBackground());
    variance.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Vol.:");
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
    gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_FILL);
    gridData.heightHint = 20;
    bottombar.setLayoutData(gridData);
    bottombar.addPaintListener(this);
    
    // Drag and drop support
    DropTarget target = new DropTarget(parent, DND.DROP_COPY);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
    target.setTransfer(types);
    target.addDropListener(this);

/*
    ChartCanvas canvas = new ChartCanvas(form);
    canvas.addMouseListener(this);
    chart.addElement(canvas);
    canvas.addPainter(new PriceChart());
    IChartPlotter chartPlotter = new AverageChart();
    chartPlotter.setParameter("period", "7");
    chartPlotter.setParameter("color", "0,255,0");
    canvas.addPainter(chartPlotter);
    chartPlotter = new AverageChart();
    chartPlotter.setParameter("period", "21");
    chartPlotter.setParameter("color", "255,0,0");
    canvas.addPainter(chartPlotter);

    canvas = new ChartCanvas(form);
    chart.addElement(canvas);
    canvas.addPainter(new RSIChart());
    
    canvas = new ChartCanvas(form);
    chart.addElement(canvas);
    canvas.addPainter(new VolumeChart());
    
    setMargin(1);
    setWidth(3);
    
    int[] weights = { 70, 15, 15 };
    form.setWeights(weights);
    reloadData();
*/
    
    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("rtchart." + id);
    if (!symbol.equals(""))
    {
      IBasicData bd = TraderPlugin.getData(symbol);
      if (bd == null)
      {
        bd = new BasicData();
        bd.setSymbol(symbol);
        bd.setTicker(symbol);
        bd.setDescription(symbol);
      }
      setData(bd);
    }
  }
  
  public void reloadPreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "rtcharts");
    Vector sectionHeights = new Vector();
    
    // Remove all charts
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      canvas.removeMouseListener(this);
      canvas.dispose();
    }
    chart.removeAllElements();

    // Read the preferences files for the new chart
    File f = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs");
    if (f.exists() == true)
    {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("section"))
          {
            ChartCanvas canvas = new ChartCanvas(form);
            canvas.createContextMenu(this);
            canvas.addMouseListener(this);
            chart.addElement(canvas);

            // Standard attributes
            Node attr = n.getAttributes().getNamedItem("price");
            if (attr != null && attr.getNodeValue().equalsIgnoreCase("true") == true)
              canvas.addPainter(new PriceChart());
            attr = n.getAttributes().getNamedItem("volume");
            if (attr != null && attr.getNodeValue().equalsIgnoreCase("true") == true)
              canvas.addPainter(new VolumeChart());
            String height = n.getAttributes().getNamedItem("height").getNodeValue();
            sectionHeights.addElement(new Integer(height));

            // Charts
            NodeList parent = n.getChildNodes();
            for (int ii = 0; ii < parent.getLength(); ii++)
            {
              Node item = parent.item(ii);
              if (item.getNodeName().equalsIgnoreCase("chart") == true)
              {
                Object obj = null;
                String id = item.getAttributes().getNamedItem("id").getNodeValue();
                if (id.equalsIgnoreCase("price") == true)
                  obj = new PriceChart();
                else
                {
                  IExtensionRegistry registry = Platform.getExtensionRegistry();
                  IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.chartPlotter");
                  if (extensionPoint != null)
                  {
                    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
                    for (int m = 0; m < members.length; m++)
                    {
                      IConfigurationElement member = members[m];
                      if (id.equalsIgnoreCase(member.getAttribute("id")))
                        try {
                          obj = member.createExecutableExtension("class");
                        } catch(Exception x) { x.printStackTrace(); };
                    }
                  }
                }
                if (obj != null && obj instanceof IChartPlotter)
                {
                  setPlotterParameters((NodeList)item, (IChartPlotter)obj);
                  if (item.getAttributes().getNamedItem("name") != null)
                    ((IChartPlotter)obj).setName(item.getAttributes().getNamedItem("name").getNodeValue());
                  canvas.addPainter((IChartPlotter)obj);
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
        weights[i] = ((Integer)sectionHeights.elementAt(i)).intValue();
      form.setWeights(weights);
    }
    else
    {
      ChartCanvas canvas = new ChartCanvas(form);
      canvas.createContextMenu(this);
      canvas.addMouseListener(this);
      chart.addElement(canvas);
      canvas.addPainter(new PriceChart());
      
      canvas = new ChartCanvas(form);
      canvas.createContextMenu(this);
      chart.addElement(canvas);
      canvas.addPainter(new VolumeChart());

      int[] weights = { 80, 20 };
      form.setWeights(weights);
    }
  }
  
  private void setPlotterParameters(NodeList parent, IChartPlotter obj)
  {
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node node = parent.item(i);
      if (node.getNodeName().equalsIgnoreCase("params") == true)
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
  public void savePreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "rtcharts");

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "preferences", null);

      int[] weights = form.getWeights();
      for (int i = 0; i < weights.length; i++)
      {
        Element element = document.createElement("section");
        element.setAttribute("height", String.valueOf(weights[i]));
        ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
        for (int ii = 0; ii < canvas.getPainterCount(); ii++)
        {
//          if (canvas.getPainter(ii) instanceof PriceChart)
//            element.setAttribute("price", "true");
          if (canvas.getPainter(ii) instanceof VolumeChart)
            element.setAttribute("volume", "true");
        }
        document.getDocumentElement().appendChild(element);

        // Get all plotters for the given canvas
        for (int ii = 0; ii < canvas.getPainterCount(); ii++)
        {
          IChartPlotter painter = canvas.getPainter(ii);
          if (painter instanceof VolumeChart)
            continue;

          // Set the standard attributes
          Element node = document.createElement("chart");
          node.setAttribute("id", painter.getId());
          if (painter.getName() != null)
            node.setAttribute("name", painter.getName());
          
          // Append the parameters map
          HashMap params = painter.getParameters();
          Iterator keys = params.keySet().iterator();
          while (keys.hasNext())
          {
            String key = (String)keys.next();
            Element p = document.createElement("params");
            p.setAttribute(key, (String)params.get(key));
            node.appendChild(p);
          }
          
          element.appendChild(node);
        }
      }

      // XML transform
      File f = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs");
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) { ex.printStackTrace(); };
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(basicData, this);
    }
    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    container.setFocus();
  }
  
  public void setData(final IBasicData d)
  {
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(basicData, this);
    }
    basicData = d;
    reloadPreferences();
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.addRealtimeChartListener(basicData, this);
    }

    String id = getViewSite().getSecondaryId();
    ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + id, basicData.getSymbol());
    setPartName(basicData.getTicker() + " - RTChart");

    new Thread(new Runnable() {
      public void run()
      {
        load();
        if (TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
        {
          IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
          IChartData[] _d = rtp.getHistoryData(basicData);
          if (_d != null)
          {
            data = _d;
            store();
          }
        }
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
          for (int i = 0; i < chart.size(); i++)
            ((ChartCanvas)chart.elementAt(i)).setData(data);
        }
      }
    }).start();
  }
  
  public void updateView()
  {
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setData(data);
  }
  
  public void addOscillator(String id)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.chartPlotter");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int m = 0; m < members.length; m++)
      {
        IConfigurationElement member = members[m];
        if (id.equalsIgnoreCase(member.getAttribute("id")))
          try {
            Object obj = member.createExecutableExtension("class");
            if (obj instanceof IChartPlotter)
            {
              IChartPlotter chartPlotter = (IChartPlotter)obj;
              ChartParametersDialog pdlg = new ChartParametersDialog((IChartConfigurer)chartPlotter);
              if (pdlg.open() == ChartParametersDialog.OK)
              {
                if (pdlg.getPosition() == ChartParametersDialog.SELECTED_ZONE)
                  ((ChartCanvas)chart.elementAt(selectedZone)).addPainter(chartPlotter);
                else
                {
                  int[] w = form.getWeights();
                  ChartCanvas canvas = new ChartCanvas(form);
                  Control[] children = form.getChildren();
                  children[children.length - 1].moveBelow(children[0]);
                  canvas.createContextMenu(this);
                  chart.insertElementAt(canvas, 1);
                  canvas.addPainter(chartPlotter);
                  int[] weights = new int[chart.size()];
                  for (int i = 0; i < w.length; i++)
                    weights[i] = w[i];
                  weights[0] -= 150;
                  weights[weights.length - 1] = 150;
                  form.layout();
                }
                updateView();
                savePreferences();
              }
            }
          } catch(Exception x) { x.printStackTrace(); };
      }
    }
  }
  
  public void editOscillator()
  {
    ChartDialog dlg = new ChartDialog();
    dlg.setChart(chart);
    if (dlg.open() == ChartDialog.OK)
    {
      IChartPlotter chartPlotter = dlg.getObject();
      if (chartPlotter != null)
      {
        ChartParametersDialog pdlg = new ChartParametersDialog((IChartConfigurer)chartPlotter);
        if (pdlg.openEdit() == ChartParametersDialog.OK)
        {
          updateView();
          savePreferences();
        }
      }
    }
  }
  
  public void removeOscillator()
  {
    ChartDialog dlg = new ChartDialog();
    dlg.setChart(chart);
    if (dlg.open() == ChartDialog.OK)
    {
      IChartPlotter obj = dlg.getObject();
      if (obj != null)
      {
        for (int i = chart.size() - 1; i >= 0; i--)
        {
          ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
          for (int ii = canvas.getPainterCount() - 1; ii >= 0; ii--)
          {
            if (canvas.getPainter(ii) == obj)
            {
              canvas.removePainter(obj);
              if (canvas.getPainterCount() == 0)
              {
                canvas.removeMouseListener(this);
                canvas.dispose();
                chart.removeElementAt(i);
                form.layout();
              }
              break;
            }
          }
        }
        updateView();
        savePreferences();
      }
    }
  }
  
  public void setChartType(int type)
  {
    for (int i = chart.size() - 1; i >= 0; i--)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      for (int ii = canvas.getPainterCount() - 1; ii >= 0; ii--)
      {
        if (canvas.getPainter(ii) instanceof PriceChart)
        {
          canvas.getPainter(ii).setParameter("type", String.valueOf(type));
          updateView();
          savePreferences();
          break;
        }
      }
    }
  }

/*
  private void reloadData()
  {
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("rtchart." + id);

    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(basicData, this);
    }
    
    basicData = null;

    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(symbol) == true)
      {
        basicData = _tpData[i];
        setPartName(basicData.getTicker() + " - RTChart");
        load();
        if (TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
        {
          IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
          IChartData[] _d = rtp.getHistoryData(basicData);
          if (_d != null)
          {
            data = _d;
            store();
          }
        }
        if (data != null && data.length > 0 && data[0].getMaxPrice() >= 10)
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
        controlResized(null);
        bottombar.redraw();
        title.setText(basicData.getDescription());
        updateLabels();
        for (int m = 0; m < chart.size(); m++)
          ((ChartCanvas)chart.elementAt(m)).setData(data);
        break;
      }
    }

    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.addRealtimeChartListener(basicData, this);
    }
  }
*/

  public void load()
  {
    Vector _data = new Vector();
    File folder = new File(Platform.getLocation().toFile(), "rtcharts");
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    NumberFormat nf = NumberFormat.getInstance();
    
    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    nf.setMinimumIntegerDigits(1);
    
    File file = new File(folder, basicData.getSymbol().toLowerCase() + ".xml");
    if (file.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int c = 0; c < firstChild.getLength(); c++)
        {
          if (firstChild.item(c).getNodeName().equalsIgnoreCase("data"))
          {
            NodeList parent = firstChild.item(c).getChildNodes();
            IChartData cd = new ChartData();
            for (int i = 0; i < parent.getLength(); i++)
            {
              Node n = parent.item(i);
              Node value = n.getFirstChild();
              if (value != null)
              {
                if (n.getNodeName().equalsIgnoreCase("open") == true)
                  cd.setOpenPrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("max") == true)
                  cd.setMaxPrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("min") == true)
                  cd.setMinPrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("close") == true)
                  cd.setClosePrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("volume") == true)
                  cd.setVolume(Integer.parseInt(value.getNodeValue()));
                else if (n.getNodeName().equalsIgnoreCase("date") == true)
                {
                  try {
                    cd.setDate(df.parse(value.getNodeValue()));
                  } catch(Exception e) {};
                }
              }
            }
            _data.addElement(cd);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    data = new IChartData[_data.size()];
    _data.toArray(data);
  }

  /**
   * Method to store the chart data.<br>
   */
  public void store()
  {
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    NumberFormat nf = NumberFormat.getInstance();
    
    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    nf.setMinimumIntegerDigits(1);

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "chart", null);

      for (int i = 0; i < data.length; i++)
      {
        Element element = document.createElement("data");
        document.getDocumentElement().appendChild(element);

        Node node = document.createElement("date");
        element.appendChild(node);
        node.appendChild(document.createTextNode(df.format(data[i].getDate())));
        node = document.createElement("open");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getOpenPrice())));
        node = document.createElement("close");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getClosePrice())));
        node = document.createElement("max");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getMaxPrice())));
        node = document.createElement("min");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getMinPrice())));
        node = document.createElement("volume");
        element.appendChild(node);
        node.appendChild(document.createTextNode("" + data[i].getVolume()));
      }

      File folder = new File(Platform.getLocation().toFile(), "rtcharts");
      folder.mkdirs();

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(folder, basicData.getSymbol().toLowerCase() + ".xml")));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) {};
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
        variance.setText(pcf.format(pc * 100) + "%");
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
        variance.setText(pcf.format(pc * 100) + "%");
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
      date.setText("");
      closePrice.setText("");
      maxPrice.setText("");
      minPrice.setText("");
      variance.setText("");
      volume.setText("");
    }
    composite.layout();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartListener#realtimeChartUpdated(net.sourceforge.eclipsetrader.IRealtimeChartProvider)
   */
  public void realtimeChartUpdated(final IRealtimeChartProvider provider)
  {
    new Thread(new Runnable() {
      public void run() 
      {
        data = provider.getHistoryData(basicData);
        store();
        updateChart();
      }
    }).start();
  }
  
  public void updateChart()
  {
    container.getDisplay().asyncExec(new Runnable() {
      public void run() {
        controlResized(null);
        bottombar.redraw();
        title.setText(basicData.getDescription());
        updateLabels();
        for (int m = 0; m < chart.size(); m++)
          ((ChartCanvas)chart.elementAt(m)).setData(data);
      }
    });
  }
  
  private void setWidth(int width)
  {
    this.width = width;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setColumnWidth(width);
  }
  
  private void setMargin(int margin)
  {
    this.margin = margin;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setMargin(margin);
  }
  
  private void setScaleWidth(int scaleWidth)
  {
    this.scaleWidth = scaleWidth;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setScaleWidth(scaleWidth);
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

      if (basicData != null)
      {
        int lastValue = -1;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        
        gc.setClipping(0, 0, bottombar.getClientArea().width - scaleWidth, bottombar.getClientArea().height);

        int x = margin + width / 2;
        if (container.getHorizontalBar().isVisible() == true)
          x -= container.getHorizontalBar().getSelection();
        for (int i = 0; i < data.length; i++, x += width)
        {
          c.setTime(data[i].getDate());
          if (c.get(Calendar.HOUR_OF_DAY) != lastValue || c.get(Calendar.MINUTE) == 30)
          {
            String s = df.format(data[i].getDate());
            int x1 = x - gc.stringExtent(s).x / 2;
            gc.drawLine(x, 0, x, 5);
            gc.drawString(s, x1, 5);
            lastValue = c.get(Calendar.HOUR_OF_DAY);
          }
        }
      }
    }
    else
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
    if (container.isDisposed() == true)
      return;
    ScrollBar sb = container.getHorizontalBar();
    if (basicData != null)
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
          ((ChartCanvas)chart.elementAt(i)).setChartOrigin(0 - sb.getSelection());
      }
      else
      {
        sb.setVisible(false);
        for (int i = 0; i < chart.size(); i++)
          ((ChartCanvas)chart.elementAt(i)).setChartOrigin(0);
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
        ((ChartCanvas)chart.elementAt(i)).setChartOrigin(0 - sb.getSelection());
      bottombar.redraw();
    }
  }

  private boolean mouseDown = false;
  private int mousePreviousX = -1;
  private GC mouseGC;

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
    if (e.button == 1)
    {
      mouseDown = true;
      mouseGC = new GC((Canvas)e.getSource());
      mouseGC.setXORMode(true);
      mouseGC.setForeground(background);
      mouseMove(e);
    }
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      if (e.getSource() == canvas.getChart())
        selectedZone = i;
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
      updateLabels();
      if (mousePreviousX != -1)
      {
        mouseGC.drawLine(mousePreviousX, 0, mousePreviousX, ((Canvas)e.getSource()).getClientArea().height);
        mousePreviousX = -1;
        mouseGC.dispose();
      }
    }
  }
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove(MouseEvent e)
  {
    if (mouseDown == true)
    {
      int index = (e.x - margin) / width;
      if (index >= 0 && index < data.length)
      {
        updateLabels(index);
        if (mousePreviousX != -1)
          mouseGC.drawLine(mousePreviousX, 0, mousePreviousX, ((Canvas)e.getSource()).getClientArea().height);
        mouseGC.drawLine(e.x, 0, e.x, ((Canvas)e.getSource()).getClientArea().height);
        mousePreviousX = e.x;
      }
      else
      {
        updateLabels();
        mouseGC.drawLine(mousePreviousX, 0, mousePreviousX, ((Canvas)e.getSource()).getClientArea().height);
        mousePreviousX = -1;
      }
    }
  }

  public void dragEnter(DropTargetEvent event) 
  {
    event.detail = DND.DROP_COPY;
  }
  public void dragOver(DropTargetEvent event) 
  {
    event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
  }
  public void dragOperationChanged(DropTargetEvent event) 
  {
  }
  public void dragLeave(DropTargetEvent event) 
  {
  }
  public void dropAccept(DropTargetEvent event) 
  {
  }
  public void drop(DropTargetEvent event) 
  {
    String[] item = ((String)event.data).split(";");
    String id = getViewSite().getSecondaryId();
    String symbol = item[1];
    ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + id, item[1]);
    if (!symbol.equals(""))
    {
      IBasicData bd = TraderPlugin.getData(symbol);
      if (bd == null)
      {
        bd = new BasicData();
        bd.setSymbol(symbol);
        bd.setTicker(symbol);
        bd.setDescription(symbol);
      }
      setData(bd);
    }
  }
}
