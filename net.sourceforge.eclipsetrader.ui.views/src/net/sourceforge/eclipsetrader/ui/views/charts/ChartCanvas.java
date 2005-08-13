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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.ICollectionObserver;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Define a canvas over which a IChartPlotter object can draw.
 */
public class ChartCanvas extends Composite implements ControlListener, Observer, PaintListener
{
  public static final int LINE = 0;
  public static final int BARS = 1;
  public static final int CANDLES = 2;
  private boolean hilight = false;
  private boolean mainChart = false;
  private int style = LINE;
  private Composite container;
  private Composite chartContainer;
  private Composite labels;
  private Composite scaleContainer;
  private Canvas chart;
  private Canvas scale;
  private Image chartImage;
  private Image scaleImage;
  private Label scaleLabel;
  private Color chartBackground = new Color(null, 255, 255, 224);
  private Color scaleBackground = new Color(null, 255, 255, 255);
  private Color separatorColor = new Color(null, 0, 0, 0);
  private Color hilightColor = new Color(null, 255, 0, 0);
  private Color scaleLabelColor = new Color(null, 255, 255, 0);
  private Color gridColor = new Color(null, 192, 192, 192);
  private Color lineColor = new Color(null, 0, 0, 255);
  private int columnWidth = 5;
  private int margin = 2;
  private int scaleWidth = 50;
  private BarData barData = new BarData();
  private boolean logScale = false;
  private Scaler scaler = new Scaler();
  private IndicatorsCollection indicators = new IndicatorsCollection();
  private ToolsCollection tools = new ToolsCollection();
  private NumberFormat nf = NumberFormat.getInstance();
  private Cursor crossCursor = new Cursor(null, SWT.CURSOR_CROSS);
  private Cursor arrowCursor = new Cursor(null, SWT.CURSOR_ARROW);
  private ToolPlugin selectedTool = null;

  /**
   * Standard SWT widget constructor.
   * <p>The style refers to the same styles available for the Composite widget.</p>
   */
  public ChartCanvas(Composite parent, int style)
  {
    super(parent, style);
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);

    // Main container with a GridLayout of 2 columns to accomodate for the chart
    // and the scale
    container = this; // new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    container.setLayout(gridLayout);

    // Container of the chart, needed for the horizontal scroll
    chartContainer = new Composite(container, SWT.NONE);
    chartContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
    chartContainer.addControlListener(this);
    
    labels = new Composite(chartContainer, SWT.NONE);
    RowLayout rowLayout = new RowLayout();
    rowLayout.type = SWT.HORIZONTAL;
    rowLayout.pack = true;
    rowLayout.wrap = true;
    labels.setLayout(rowLayout);
    labels.setBackground(chartBackground);
    labels.setBounds(0, 0, 0, 0);
    
    // Chart canvas
    chart = new Canvas(chartContainer, SWT.NONE);
    chart.setBackground(chartBackground);
    chart.addPaintListener(this);

    // Container for the scale canvas
    scaleContainer = new Composite(container, SWT.NONE);
    GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);
    gridData.widthHint = scaleWidth;
    scaleContainer.setLayoutData(gridData);
    scaleContainer.addControlListener(this);
    
    // Label that overlays the scale
    scaleLabel = new Label(scaleContainer, SWT.NONE);
    scaleLabel.setBackground(scaleLabelColor);
    scaleLabel.setBounds(0, 0, 0, 0);

    // Scale canvas
    scale = new Canvas(scaleContainer, SWT.NONE);
    scale.setBackground(scaleBackground);
    scale.addPaintListener(this);
    
    // Add the indicators collection observer
    indicators.addObserver(new ICollectionObserver() {
      public void itemAdded(Object obj)
      {
        if (obj instanceof IndicatorPlugin)
        {
          ((IndicatorPlugin)obj).setCanvas(ChartCanvas.this.getChart());
          if (barData != null)
          {
            ((IndicatorPlugin)obj).setBarData(barData);
            redraw();
            updateLabels();
          }
        }
        
        if (obj instanceof Observable)
          ((Observable)obj).addObserver(ChartCanvas.this);
      }
      public void itemRemoved(Object obj)
      {
        redraw();
        updateLabels();
        if (obj instanceof Observable)
          ((Observable)obj).deleteObserver(ChartCanvas.this);
      }
    });
    
    // Add the tools collection observer
    tools.addObserver(new ICollectionObserver() {
      public void itemAdded(Object obj)
      {
        ((ToolPlugin)obj).setCanvas(chart);
        ((ToolPlugin)obj).setScaler(scaler);
        if (barData != null)
        {
          getChart().redraw();
          updateLabels();
        }
        
        if (obj instanceof Observable)
          ((Observable)obj).addObserver(ChartCanvas.this);
      }
      public void itemRemoved(Object obj)
      {
        getChart().redraw();
        updateLabels();
        if (obj instanceof Observable)
          ((Observable)obj).deleteObserver(ChartCanvas.this);
      }
    });
    
    updateLabels();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  public void dispose()
  {
    scale.removePaintListener(this);
    chartContainer.removeControlListener(this);
    chart.removePaintListener(this);
    
    indicators.clear();
    tools.clear();

    scale.dispose();
    chart.dispose();
    chartContainer.dispose();
    if (chartImage != null)
      chartImage.dispose();
    
    super.dispose();
  }
  
  public void registerContextMenu(IWorkbenchPartSite site) 
  {
    // Create menu manager.
    MenuManager menuMgr = new MenuManager("#popupMenu", "contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager mgr) {
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });
    
    // Create menu.
    Menu menu = menuMgr.createContextMenu(chart);
    chart.setMenu(menu);
    
    // Register menu for extension.
    site.registerContextMenu(menuMgr, site.getSelectionProvider());
  }

  /**
   * Get the canvas used to draw the chart.
   * 
   * @return the chart canvas
   */
  public Canvas getChart()
  {
    return chart;
  }
  
  /**
   * Get the canvas used to draw the chart scale.
   * 
   * @return the scale canvas
   */
  public Canvas getScale()
  {
    return scale;
  }
  
  /**
   * Get the hilight status of the receiver.
   *
   * @return true if hilighted, false otherwise
   */
  public boolean isHilight()
  {
    return hilight;
  }

  /**
   * Set the hilight status of the receiver.
   * 
   * @param hilight - the hilight status
   */
  public void setHilight(boolean hilight)
  {
    this.hilight = hilight;
    getScale().redraw();
  }
  
  public Scaler getScaler()
  {
    return scaler;
  }
  
  public boolean isLogScale()
  {
    return logScale;
  }
  
  public void setLogScale(boolean value)
  {
    this.logScale = value;
    redraw();
  }

  public boolean isMainChart()
  {
    return mainChart;
  }

  public void setMainChart(boolean mainChart)
  {
    this.mainChart = mainChart;
  }

  public int getStyle()
  {
    return style;
  }

  public void setStyle(int style)
  {
    this.style = style;
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove(MouseEvent e)
  {
    for (int i = 0; i < tools.size(); i++)
    {
      if (tools.get(i).isOnHandle(e.x, e.y))
      {
        if (selectedTool != tools.get(i))
        {
          setCursor(crossCursor);
          selectedTool = tools.get(i);
        }
        if (selectedTool != null)
        {
          if (selectedTool.isMousePressed())
            selectedTool.mouseDragged(e);
          else
            selectedTool.mouseMoved(e);
        }
        return;
      }
    }
    
    if (selectedTool != null)
    {
      setCursor(arrowCursor);
      selectedTool = null;
    }
  }

  public void setBarData(BarData barData)
  {
    this.barData = barData;
    for (int i = 0; i < indicators.size(); i++)
    {
      if (indicators.get(i) instanceof IndicatorPlugin)
        ((IndicatorPlugin)indicators.get(i)).setBarData(barData);
    }

    if (chartContainer.isDisposed() == true)
      return;
    chartContainer.getDisplay().asyncExec(new Runnable() {
      public void run()  
      {
        setControlSize();
        chart.redraw();
        scale.redraw();
        container.layout();
      }
    });
  }

  /**
   * Set the origin of the chart canvas from within the container.
   * <p></p>
   */
  public void setChartOrigin(int x)
  {
    chart.setLocation(x, 0);
  }
  
  /**
   * Get the height of the chart canvas.
   * <p></p>
   */
  public int getHeight()
  {
    return chart.getClientArea().height;
  }
  
  /**
   * Set the width of a chart column.
   * <p></p>
   */
  public void setColumnWidth(int columnWidth)
  {
    this.columnWidth = columnWidth;
  }
  /**
   * Get the width of a chart column.
   * <p></p>
   */
  public int getColumnWidth()
  {
    return columnWidth;
  }
  
  /**
   * Set the chart margin (blank border around the drawing area).
   * <p></p>
   */
  public void setMargin(int margin)
  {
    this.margin = margin;
  }
  /**
   * Get the chart margin (blank border around the drawing area).
   * <p></p>
   */
  public int getMargin()
  {
    return margin;
  }
  
  /**
   * Set the width the scale canvas.
   * <p></p>
   */
  public void setScaleWidth(int scaleWidth)
  {
    this.scaleWidth = scaleWidth;
  }
  /**
   * Get the width the scale canvas.
   * <p></p>
   */
  public int getScaleWidth()
  {
    return scaleWidth;
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#redraw()
   */
  public void redraw()
  {
    double scaleHigh = -99999999;
    double scaleLow = 99999999;
    List output = new ArrayList();

    if (isMainChart())
    {
      scaleHigh = barData.getHigh();
      scaleLow = barData.getLow();
    }
    
    // Populates the output buffer and sets the high and low scale values
    for (int i = 0; i < indicators.size(); i++)
    {
      Object obj = indicators.get(i);
      if (obj instanceof IndicatorPlugin)
      {
        IndicatorPlugin indicator = (IndicatorPlugin)obj;
        indicator.getOutput().clear();
        indicator.calculate();
        for(Iterator iter = indicator.getOutput().iterator(); iter.hasNext(); )
        {
          PlotLine plotLine = (PlotLine)iter.next();
          if (plotLine.getHigh() > scaleHigh)
            scaleHigh = plotLine.getHigh();
          if (plotLine.getLow() < scaleLow)
            scaleLow = plotLine.getLow();
          plotLine.setSelected(indicator.isSelected());
          output.add(plotLine);
        }
      }
    }
    
    // Calculates the logarithmic range
    double logScaleHigh = scaleHigh > 0.0 ? Math.log(scaleHigh) : 1;
    double logScaleLow = scaleLow > 0.0 ? Math.log(scaleLow) : 0;
    double logRange = logScaleHigh - logScaleLow;
    
    // Set the scaler values
    scaler.set(chart.getSize().y, scaleHigh, scaleLow, logScaleHigh, logRange, logScale);

    // Draw the chart
    if (chartImage != null)
    {
      GC gc = new GC(chartImage);
      gc.setBackground(chartBackground);
      gc.fillRectangle(chartImage.getBounds());
      
      if (barData != null)
      {
        gc.setForeground(gridColor);
        gc.setLineStyle(SWT.LINE_DOT);
        List scaleArray = scaler.getScaleArray();
        int loop;
        for (loop = 0; loop < (int) scaleArray.size(); loop++)
        {
          int y = scaler.convertToY(((Double)scaleArray.get(loop)).doubleValue());
          gc.drawLine (0, y, chart.getSize().x, y);
        }
        gc.setLineStyle(SWT.LINE_SOLID);
        
        // Draw the main price line
        if (isMainChart())
        {
          int[] pointArray = new int[barData.size() * 2];
          int x = getMargin() + columnWidth / 2;
          for (int i = 0, pa = 0; i < barData.size(); i++, x += columnWidth)
          {
            pointArray[pa++] = x;
            pointArray[pa++] = scaler.convertToY(barData.getClose(i));
          }
          gc.setForeground(lineColor);
          gc.drawPolyline(pointArray);
        }
        
        for(Iterator iter = output.iterator(); iter.hasNext(); )
        {
          PlotLine plotLine = (PlotLine)iter.next();
          switch(plotLine.getType())
          {
            case PlotLine.LINE:
            case PlotLine.DOT:
            case PlotLine.DASH:
              drawLine(gc, plotLine);
              break;
            case PlotLine.HISTOGRAM:
              drawHistogram(gc, plotLine);
              break;
            case PlotLine.HISTOGRAM_BAR:
              drawHistogramBar(gc, plotLine);
              break;
            case PlotLine.HORIZONTAL:
              drawHorizontalLine(gc, plotLine);
              break;
          }
        }
      }

      gc.dispose();
    }
    
    // Draw the scale
    if (scaleImage != null)
    {
      GC gc = new GC(scaleImage);
      gc.setBackground(scaleBackground);
      gc.fillRectangle(scaleImage.getBounds());

      List scaleArray = scaler.getScaleArray();
      int loop;
      for (loop = 0; loop < (int) scaleArray.size(); loop++)
      {
        double value = ((Double)scaleArray.get(loop)).doubleValue();
        int y = scaler.convertToY(value);
        gc.drawLine (0, y, 4, y);

        if (value >= 1000)
        {
          nf.setMaximumFractionDigits(0);
          nf.setMinimumFractionDigits(0);
        }
        else if (value > 3)
        {
          nf.setMaximumFractionDigits(2);
          nf.setMinimumFractionDigits(2);
        }
        else
        {
          nf.setMaximumFractionDigits(4);
          nf.setMinimumFractionDigits(4);
        }
        String s = nf.format(value);
        if (Math.abs(value) >= 1000000000)
          s = nf.format(value / 1000000000) + "b";
        else
        {
          if (Math.abs(value) >= 1000000)
            s = nf.format(value / 1000000) + "m";
          else
          {
            if (Math.abs(value) >= 1000)
              s = nf.format(value / 1000) + "k";
          }
          
          gc.drawString(s, 7, y - gc.stringExtent(s).y / 2);
        }
      }

      gc.dispose();
    }

    // Forces a redraw
    if (chart != null)
      chart.redraw();
    if (scale != null)
      scale.redraw();
  }

  private void drawLine(GC gc, PlotLine plotLine)
  {
    int ofs = barData.size() - plotLine.getSize();
    int[] pointArray = new int[plotLine.getSize() * 2];
    int x = getMargin() + columnWidth / 2 + ofs * columnWidth;
    for (int i = 0, pa = 0; i < plotLine.getSize(); i++, x += columnWidth)
    {
      pointArray[pa++] = x;
      pointArray[pa++] = scaler.convertToY(plotLine.getData(i));
    }

    gc.setForeground(plotLine.getColor());
    if (plotLine.getType() == PlotLine.DOT)
      gc.setLineStyle(SWT.LINE_DOT);
    else if (plotLine.getType() == PlotLine.DASH)
      gc.setLineStyle(SWT.LINE_DASH);
    else
      gc.setLineStyle(SWT.LINE_SOLID);

    gc.drawPolyline(pointArray);

    // Draw the selection marks
    if (plotLine.isSelected() && pointArray.length > 0)
    {
      gc.setBackground(plotLine.getColor());
      int length = pointArray.length / 2;
      if (length <= 20)
      {
        gc.fillRectangle(pointArray[0] - 1, pointArray[1] - 1, 5, 5);
        gc.fillRectangle(pointArray[(length / 2) * 2] - 1, pointArray[(length / 2) * 2 + 1] - 1, 5, 5);
      }
      else
      {
        for (int i = 0; i < length - 5; i += 10)
          gc.fillRectangle(pointArray[i * 2] - 1, pointArray[i * 2 + 1] - 1, 5, 5);
      }
      gc.fillRectangle(pointArray[pointArray.length - 2] - 1, pointArray[pointArray.length - 1] - 1, 5, 5);
    }
  }

  private void drawHistogram(GC gc, PlotLine plotLine)
  {
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setBackground(plotLine.getColor());

    int zero = scaler.convertToY(0);
    int ofs = barData.size() - plotLine.getSize();
    int x = -1;
    int x2 = getMargin() + columnWidth / 2 + ofs * columnWidth;
    int y = -1;
    int y2 = -1;
    int[] pointArray = new int[8];
    for (int i = 0; i < plotLine.getSize(); i++, x2 += columnWidth)
    {
      y2 = scaler.convertToY(plotLine.getData(i));

      pointArray[0] = x; pointArray[1] = zero;
      pointArray[2] = x; pointArray[3] = y;
      pointArray[4] = x2; pointArray[5] = y2;
      pointArray[6] = x2; pointArray[7] = zero;
      if (y != -1)
        gc.fillPolygon(pointArray);

      x = x2;
      y = y2;
    }
  }

  private void drawHistogramBar(GC gc, PlotLine plotLine)
  {
    gc.setLineStyle(SWT.LINE_SOLID);

    int zero = scaler.convertToY(0);
    int ofs = barData.size() - plotLine.getSize();
    int x = getMargin() + columnWidth / 2 + ofs * columnWidth;
    for (int i = 0; i < plotLine.getSize(); i++, x += columnWidth)
    {
      int y = scaler.convertToY(plotLine.getData(i));

      Color color = plotLine.getColor(i);
      if (color == null)
        color = plotLine.getColor();
      gc.setBackground(color);
      
      gc.fillRectangle(x, y, 2, zero - y);
    }
  }

  private void drawHorizontalLine(GC gc, PlotLine plotLine)
  {
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(plotLine.getColor());

    int y = scaler.convertToY(plotLine.getData(plotLine.getSize() - 1));
    gc.drawLine(0, y, chart.getSize().x, y);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    if (e.getSource() == chart)
    {
      e.gc.drawImage(chartImage, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
      for(int i = 0; i < tools.size(); i++)
        tools.get(i).paintTool(e.gc);
    }
    else if (e.getSource() == scale)
    {
      e.gc.drawImage(scaleImage, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);

      e.gc.setLineStyle(SWT.LINE_SOLID);
      if (isHilight() == true)
        e.gc.setForeground(hilightColor);
      else
        e.gc.setForeground(separatorColor);
      e.gc.drawLine(0, 0, 0, scale.getClientArea().height);
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
    if (e.getSource() == chartContainer)
      setControlSize();
    else if (e.getSource() == scaleContainer)
    {
      scale.setSize(scaleWidth, scaleContainer.getClientArea().height);
      if (scaleImage != null)
        scaleImage.dispose();
      scaleImage = new Image(scale.getDisplay(), scale.getSize().x, scale.getSize().y);
    }
  }
  
  private void setControlSize()
  {
    if (barData != null)
    {
      int w = barData.size() * columnWidth + margin * 2;
      if (w > chartContainer.getClientArea().width)
        chart.setSize(w, chartContainer.getClientArea().height);
      else
        chart.setSize(chartContainer.getClientArea().width, chartContainer.getClientArea().height);
    }
    else
      chart.setSize(chartContainer.getClientArea().width, chartContainer.getClientArea().height);
    
    if (chartImage != null)
      chartImage.dispose();
    if (chart.getSize().x != 0 && chart.getSize().y != 0)
      chartImage = new Image(chart.getDisplay(), chart.getSize().x, chart.getSize().y);
    
    redraw();
  }
  
  public void deselectAll()
  {
    for (int i = 0; i < indicators.size(); i++)
    {
      Object indicator = indicators.get(i);
      if (indicator instanceof IndicatorPlugin)
        ((IndicatorPlugin)indicator).setSelected(false);
      selectedTool = null;
    }
    for (int i = 0; i < indicators.size(); i++)
    {
      if (indicators.get(i) instanceof IndicatorPlugin)
      {
        for(Iterator iter = ((IndicatorPlugin)indicators.get(i)).getOutput().iterator(); iter.hasNext(); )
        {
          PlotLine plotLine = (PlotLine)iter.next();
          plotLine.setSelected(false);
        }
      }
    }
    
    redraw();
  }
  
  /**
   * Sets the receiver's selection to be the given item. The current selection is cleared
   * before the new items are selected.
   * 
   * @param obj - the item to select
   */
  public void select(Object obj)
  {
    for (int i = 0; i < indicators.size(); i++)
    {
      Object indicator = indicators.get(i);
      if (indicator instanceof IndicatorPlugin)
        ((IndicatorPlugin)indicator).setSelected((obj == indicator));
    }

    if (obj instanceof ToolPlugin)
      selectedTool = (ToolPlugin)obj;
    else
      selectedTool = null;
    
    redraw();
  }
  
  public Object getSelectedItem()
  {
    for (int i = 0; i < indicators.size(); i++)
    {
      Object obj = indicators.get(i);
      if (obj instanceof IndicatorPlugin)
      {
        if (((IndicatorPlugin)obj).isSelected())
          return obj;
      }
    }

    if (selectedTool != null)
      return selectedTool;
    
    return null;
  }
  
  /**
   * Return the item at the given point in the receiver or null if no such item exists.
   * 
   * @param x - the point x coordinate
   * @param y - the point y coordinate
   * @return the item at the given point
   */
  public Object getItemAt(int x, int y)
  {
    for (int i = 0; i < indicators.size(); i++)
    {
      Object obj = indicators.get(i);
      if (obj instanceof IndicatorPlugin)
      {
        for(Iterator iter = ((IndicatorPlugin)obj).getOutput().iterator(); iter.hasNext(); )
        {
          PlotLine plotLine = (PlotLine)iter.next();
          int ofs = barData.size() - plotLine.getSize();
          int[] pointArray = new int[plotLine.getSize() * 2];
          int px = getMargin() + columnWidth / 2 + ofs * columnWidth;
          for (int pi = 0, pa = 0; pi < plotLine.getSize(); pi++, px += columnWidth)
          {
            pointArray[pa++] = px;
            pointArray[pa++] = scaler.convertToY(plotLine.getData(pi));
          }
          if (PixelTools.isPointOnLine(x, y, pointArray))
            return obj;
        }
      }
    }

    // Attempt to select one of the tools
    for (int i = 0; i < tools.size(); i++)
    {
      if (tools.get(i).containsPoint(x, y))
        return tools.get(i);
    }
    
    return null;
  }
  
  public void updateLabels()
  {
    updateLabels(barData.size() - 1);
  }

  public void updateLabels(int index)
  {
    Control[] children = labels.getChildren();
    for (int i = 0; i < children.length; i++)
      children[i].dispose();
    
    if (indicators.size() != 0)
    {
      for (int i = 0; i < indicators.size(); i++)
      {
        Object obj = indicators.get(i);
        if (obj instanceof IndicatorPlugin)
        {
          IndicatorPlugin indicator = (IndicatorPlugin)obj;
          for (int ii = 0; ii < indicator.getOutput().size(); ii++)
          {
            PlotLine plotLine = (PlotLine)indicator.getOutput().get(ii);
            Label label = new Label(labels, SWT.NONE);
            int ofs = index - (barData.size() - plotLine.getSize());
            if (ofs < 0 || ofs >= plotLine.getSize())
              label.setText(plotLine.getLabel());
            else
              label.setText(plotLine.getLabel() + " (" + nf.format(plotLine.getData(ofs)) + ")");
            label.setForeground(plotLine.getColor());
            label.setBackground(labels.getBackground());
          }
        }
      }
      labels.pack();
      labels.layout();
    }
    else
      labels.setBounds(0, 0, 0, 0);
  }
  
  public void updateScaleLabel(int y)
  {
    if (y != -1)
    {
      double value = Scaler.roundToTick(scaler.convertToVal(y));
      scaleLabel.setText("  " + nf.format(value));
      scaleLabel.setBounds(1, y - 7, scale.getSize().x, 14);
    }
    else
      scaleLabel.setBounds(0, 0, 0, 0);
  }
  
  public IndicatorsCollection getIndicators()
  {
    return indicators;
  }
  
  public ToolsCollection getTools()
  {
    return tools;
  }

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    if (o instanceof IChartPlotter)
      updateLabels();
  }
}
