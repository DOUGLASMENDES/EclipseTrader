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
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IRealtimeChartListener;
import net.sourceforge.eclipsetrader.IRealtimeChartProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.internal.ChartData;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
  private IBasicData data;
  private IChartData[] chartData;
  private Vector chart = new Vector();
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("HH:mm");
  
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
    
    ChartCanvas canvas = new ChartCanvas(form);
    canvas.addMouseListener(this);
    chart.addElement(canvas);
    canvas.addPainter(new PriceChart());
    canvas.addPainter(new AverageChart(7, new Color(null, 0, 255, 0)));
    canvas.addPainter(new AverageChart(21, new Color(null, 255, 0, 0)));

    canvas = new ChartCanvas(form);
    chart.addElement(canvas);
    canvas.addPainter(new RSIChart(10));
    
    canvas = new ChartCanvas(form);
    chart.addElement(canvas);
    canvas.addPainter(new VolumeChart());
    
    // Drag and drop support
    DropTarget target = new DropTarget(parent, DND.DROP_COPY);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
    target.setTransfer(types);
    target.addDropListener(this);
    
    setMargin(1);
    setWidth(3);
    
    int[] weights = { 70, 15, 15 };
    form.setWeights(weights);
    reloadData();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    if (data != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(data, this);
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
  
  private void reloadData()
  {
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("rtchart." + id);

    if (data != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(data, this);
    }
    
    data = null;

    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(symbol) == true)
      {
        data = _tpData[i];
        setPartName(data.getTicker() + " - RTChart");
        load();
        if (TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
        {
          IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
          IChartData[] _d = rtp.getHistoryData(data);
          if (_d != null)
          {
            chartData = _d;
            store();
          }
        }
        if (chartData != null && chartData.length > 0 && chartData[0].getMaxPrice() >= 10)
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
        title.setText(data.getDescription());
        updateLabels();
        for (int m = 0; m < chart.size(); m++)
          ((ChartCanvas)chart.elementAt(m)).setData(chartData);
        break;
      }
    }

    if (data != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.addRealtimeChartListener(data, this);
    }
  }
  
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
    
    File file = new File(folder, data.getSymbol().toLowerCase() + ".xml");
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
      
    chartData = new IChartData[_data.size()];
    _data.toArray(chartData);
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

      for (int i = 0; i < chartData.length; i++)
      {
        Element element = document.createElement("data");
        document.getDocumentElement().appendChild(element);

        Node node = document.createElement("date");
        element.appendChild(node);
        node.appendChild(document.createTextNode(df.format(chartData[i].getDate())));
        node = document.createElement("open");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(chartData[i].getOpenPrice())));
        node = document.createElement("close");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(chartData[i].getClosePrice())));
        node = document.createElement("max");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(chartData[i].getMaxPrice())));
        node = document.createElement("min");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(chartData[i].getMinPrice())));
        node = document.createElement("volume");
        element.appendChild(node);
        node.appendChild(document.createTextNode("" + chartData[i].getVolume()));
      }

      File folder = new File(Platform.getLocation().toFile(), "rtcharts");
      folder.mkdirs();

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(folder, data.getSymbol().toLowerCase() + ".xml")));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) {};
  }
  
  public void setData(final IBasicData data)
  {
  }

  public void updateLabels()
  {
    updateLabels(chartData.length - 1);
  }
  
  public void updateLabels(int index)
  {
    if (chartData.length > 0)
    {
      date.setText(df.format(chartData[index].getDate()));
      closePrice.setText(pf.format(chartData[index].getClosePrice()));
      maxPrice.setText(pf.format(chartData[index].getMaxPrice()));
      minPrice.setText(pf.format(chartData[index].getMinPrice()));
      if (index >= 1)
      {
        double pc = chartData[index].getClosePrice() - chartData[index - 1].getClosePrice();
        pc = pc / chartData[index - 1].getClosePrice();
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
        double pc = chartData[index].getClosePrice() - chartData[index].getOpenPrice();
        pc = pc / chartData[index].getOpenPrice();
        variance.setText(pcf.format(pc * 100) + "%");
        if (pc > 0)
          variance.setForeground(positiveColor);
        else if (pc < 0)
          variance.setForeground(negativeColor);
        else
          variance.setForeground(textColor);
      }
      volume.setText(nf.format(chartData[index].getVolume()));
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
        chartData = provider.getHistoryData(data);
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
        title.setText(data.getDescription());
        updateLabels();
        for (int m = 0; m < chart.size(); m++)
          ((ChartCanvas)chart.elementAt(m)).setData(chartData);
      }
    });
  }
  
  private void setWidth(int width)
  {
    this.width = width;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setWidth(width);
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

      if (data != null)
      {
        int lastValue = -1;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        
        gc.setClipping(0, 0, bottombar.getClientArea().width - scaleWidth, bottombar.getClientArea().height);

        int x = margin + width / 2;
        if (container.getHorizontalBar().isVisible() == true)
          x -= container.getHorizontalBar().getSelection();
        for (int i = 0; i < chartData.length; i++, x += width)
        {
          c.setTime(chartData[i].getDate());
          if (c.get(Calendar.HOUR_OF_DAY) != lastValue || c.get(Calendar.MINUTE) == 30)
          {
            String s = df.format(chartData[i].getDate());
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
    if (data != null)
    {
      if (sb != null && (chartData.length * width + margin * 2) > container.getClientArea().width - scaleWidth)
      {
        sb.setMaximum(chartData.length * width + margin * 2);
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
      if (index >= 0 && index < chartData.length)
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
    ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + id, item[1]);
    reloadData();
  }
}
