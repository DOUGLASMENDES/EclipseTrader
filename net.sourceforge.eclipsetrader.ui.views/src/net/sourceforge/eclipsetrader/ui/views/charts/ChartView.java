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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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

public class ChartView extends ViewPart implements ControlListener, MouseListener, MouseMoveListener, PaintListener, SelectionListener
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
  private IChartDataProvider dataProvider;
  private IBasicData basicData;
  private IChartData[] data;
  private Vector chart = new Vector();
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
  
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
    label.setText("Data:");
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
    canvas.createContextMenu(this);
    canvas.addMouseListener(this);
    chart.addElement(canvas);
    canvas.addPainter(new PriceChart());
    canvas.addPainter(new AverageChart(7, new Color(null, 0, 255, 0)));
    canvas.addPainter(new AverageChart(21, new Color(null, 255, 0, 0)));

    canvas = new ChartCanvas(form);
    canvas.createContextMenu(this);
    chart.addElement(canvas);
    canvas.addPainter(new RSIChart(10));
    
    canvas = new ChartCanvas(form);
    canvas.createContextMenu(this);
    chart.addElement(canvas);
    canvas.addPainter(new VolumeChart());
    
    int[] weights = { 75, 10, 15 };
    form.setWeights(weights);

    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("chart." + id);
    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(symbol) == true)
      {
        setData(_tpData[i]);
        break;
      }
    }
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
    container.setFocus();
  }
  
  public void setData(final IBasicData d)
  {
    basicData = d;
    
    String id = getViewSite().getSecondaryId();
    ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + id, basicData.getSymbol());
    setPartName(basicData.getTicker() + " - Chart");

    new Thread(new Runnable() {
      public void run()
      {
        dataProvider = TraderPlugin.getChartDataProvider();
        if (dataProvider != null)
        {
          data = dataProvider.getData(basicData);
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
  
  public void updateChart()
  {
    new Thread(new Runnable() {
      public void run()
      {
        dataProvider = TraderPlugin.getChartDataProvider();
        if (dataProvider != null)
        {
          dataProvider.update(basicData);
          data = dataProvider.getData(basicData);
          container.getDisplay().asyncExec(new Runnable() {
            public void run() {
              controlResized(null);
              bottombar.redraw();
              updateLabels();
            }
          });
          for (int i = 0; i < chart.size(); i++)
            ((ChartCanvas)chart.elementAt(i)).setData(data);
        }
      }
    }).start();
  }
  
  public void showNext()
  {
    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(basicData.getSymbol()) == true)
      {
        if (i < _tpData.length - 1)
          setData(_tpData[i + 1]);
        else
          setData(_tpData[0]);
        break;
      }
    }
  }
  
  public void showPrevious()
  {
    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(basicData.getSymbol()) == true)
      {
        if (i > 0)
          setData(_tpData[i - 1]);
        else
          setData(_tpData[_tpData.length - 1]);
        break;
      }
    }
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
        int month = -1;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM");
        
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
            gc.drawLine(x, 0, x, 5);
            gc.drawString(s, x1, 5);
            month = c.get(Calendar.MONTH);
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
}
