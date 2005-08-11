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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.ICollectionObserver;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
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
public class ChartCanvas extends Composite implements ControlListener, MouseListener, MouseMoveListener, Observer, PaintListener, ISelectionProvider
{
  private boolean hilight = false;
  private Composite container;
  private Composite chartContainer;
  private Composite labels;
  private Composite scaleContainer;
  private Canvas chart;
  private Canvas scale;
  private Image chartImage;
  private Label scaleLabel;
  private Color chartBackground = new Color(null, 255, 255, 240);
  private Color scaleBackground = new Color(null, 255, 255, 255);
  private Color separatorColor = new Color(null, 0, 0, 0);
  private Color hilightColor = new Color(null, 255, 0, 0);
  private Color scaleLabelColor = new Color(null, 255, 255, 0);
  private int columnWidth = 5;
  private int margin = 2;
  private int scaleWidth = 50;
  private IndicatorsCollection indicators = new IndicatorsCollection();
  private ToolsCollection tools = new ToolsCollection();
  private IChartData[] data;
  private ChartPlotterSelection selection = new ChartPlotterSelection();
  private List selectionChangedListeners = new ArrayList();
  private Scaler scaler = new Scaler();

  /**
   * Standard SWT widget constructor.
   * <p>The style refers to the same styles available for the Composite widget.</p>
   */
  public ChartCanvas(Composite parent, int style)
  {
    super(parent, style);

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
//    chart.addMouseListener(this);
//    chart.addMouseMoveListener(this);

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
        ((IChartPlotter)obj).setCanvas(ChartCanvas.this);
        if (data != null)
        {
          ((IChartPlotter)obj).setData(data);
          redraw();
          updateLabels();
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
        if (data != null)
        {
          redraw();
          updateLabels();
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
    chart.removeMouseListener(this);
    chart.removeMouseMoveListener(this);
    
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
        setSelection(selection);
      }
    });
    
    // Create menu.
    Menu menu = menuMgr.createContextMenu(chart);
    chart.setMenu(menu);
    
    // Register menu for extension.
    site.registerContextMenu(menuMgr, this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
    if (selectionChangedListeners.contains(listener) == false)
      selectionChangedListeners.add(listener);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection()
  {
    return selection;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
    selectionChangedListeners.remove(listener);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
    for (int i = 0; i < selectionChangedListeners.size(); i++)
      ((ISelectionChangedListener)selectionChangedListeners.get(i)).selectionChanged(new SelectionChangedEvent(this, selection));
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
    redraw();
  }


  // ********************* UNVERIFIED ****************************
  private Cursor crossCursor = new Cursor(null, SWT.CURSOR_CROSS);
  private Cursor arrowCursor = new Cursor(null, SWT.CURSOR_ARROW);
  private ToolPlugin selectedTool = null;

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
    if (selectedTool != null)
      selectedTool.mousePressed(e);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp(MouseEvent e)
  {
    if (selectedTool != null)
      selectedTool.mouseReleased(e);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
   */
  public void addMouseListener(MouseListener listener)
  {
    chart.addMouseListener(listener);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#addMouseMoveListener(org.eclipse.swt.events.MouseMoveListener)
   */
  public void addMouseMoveListener(MouseMoveListener listener)
  {
    chart.addMouseMoveListener(listener);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#removeMouseListener(org.eclipse.swt.events.MouseListener)
   */
  public void removeMouseListener(MouseListener listener)
  {
    chart.removeMouseListener(listener);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#removeMouseMoveListener(org.eclipse.swt.events.MouseMoveListener)
   */
  public void removeMouseMoveListener(MouseMoveListener listener)
  {
    chart.removeMouseMoveListener(listener);
  }

  public void updatePainter(IChartPlotter plotter)
  {
    updateLabels();
  }
  
  /**
   * Return the number of painter objects in this canvas.
   */
  public int getPainterCount()
  {
    return indicators.size();
  }
  
  /**
   * Return the painter object at index position.
   * <p></p>
   */
  public IChartPlotter getPainter(int index)
  {
    return (IChartPlotter)indicators.get(index);
  }

  /**
   * Set the chart data to all registered painter objects.
   * <p></p>
   */
  public void setData(IChartData[] chartData)
  {
    data = chartData;
    for (int i = 0; i < indicators.size(); i++)
      ((IChartPlotter)indicators.get(i)).setData(data);
    
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
  
  public void redraw()
  {
    if (chartImage != null)
    {
      GC gc = new GC(chartImage);
      gc.setBackground(chartBackground);
      gc.fillRectangle(chartImage.getBounds());
      
      if (data != null)
      {
        for (int i = 0; i < indicators.size(); i++)
        {
          Object obj = indicators.get(i);
          if (obj instanceof IChartPlotter)
          {
            // Draw the grid lines only for the first plotter
            if (i == 0)
              ((IChartPlotter)obj).paintGrid(gc, chart.getClientArea().width, chart.getClientArea().height);
            // Draw the charts
            ((IChartPlotter)obj).paintChart(gc, chart.getClientArea().width, chart.getClientArea().height);
          }
        }
      }
      gc.dispose();
    }

    if (chart != null)
      chart.redraw();
    if (scale != null)
      scale.redraw();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    if (data != null)
    {
      if (e.getSource() == chart)
      {
        e.gc.drawImage(chartImage, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
        for(int i = 0; i < tools.size(); i++)
          tools.get(i).paintTool(e.gc);
      }
      else if (e.getSource() == scale)
      {
        if (indicators.size() != 0)
        {
          // The first plotter draws the scale
          Object obj = indicators.get(0);
          if (obj instanceof IChartPlotter)
            ((IChartPlotter)indicators.get(0)).paintScale(e.gc, scale.getClientArea().width, scale.getClientArea().height);
        }
  
        e.gc.setLineStyle(SWT.LINE_SOLID);
        if (isHilight() == true)
          e.gc.setForeground(hilightColor);
        else
          e.gc.setForeground(separatorColor);
        e.gc.drawLine(0, 0, 0, scale.getClientArea().height);
      }
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
      scale.setSize(scaleWidth, scaleContainer.getClientArea().height);
  }
  
  private void setControlSize()
  {
    if (data != null)
    {
      int w = data.length * columnWidth + margin * 2;
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
    
    ChartPlotter plotter = (ChartPlotter)indicators.get(0);
    scaler.set(chart.getSize().y, plotter.getMax(), plotter.getMin(), 0, 0, false);

    redraw();
  }
  
  public void deselectAll()
  {
    if (selection.isEmpty() == false)
    {
      ((ChartPlotter)selection.getPlotter()).setSelected(false);
      selection.setPlotter(null);
      setSelection(selection);
      redraw();
    }
  }
  
  /**
   * Sets the receiver's selection to be the given item. The current selection is cleared
   * before the new items are selected.
   * 
   * @param obj - the item to select
   */
  public void select(Object obj)
  {
    if (obj instanceof IChartPlotter)
    {
      IChartPlotter plotter = (IChartPlotter)obj;
      if (plotter != selection.getPlotter())
      {
        if (selection.isEmpty() == false)
          ((ChartPlotter)selection.getPlotter()).setSelected(false);
        
        selection.setPlotter(plotter);
        setSelection(selection);
    
        if (plotter != null && ((ChartPlotter)plotter).isSelected() == false)
          ((ChartPlotter)plotter).setSelected(true);

        redraw();
      }
    }
    else
    {
      if (selection.isEmpty() == false)
      {
        ((ChartPlotter)selection.getPlotter()).setSelected(false);
        selection.setPlotter(null);
        setSelection(selection);
        redraw();
      }
    }

    if (obj instanceof ToolPlugin)
      selectedTool = (ToolPlugin)obj;
    else
      selectedTool = null;
  }
  
  public Object getSelectedItem()
  {
    if (selection.isEmpty() == false)
      return selection.getPlotter();
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
    ImageData imageData = chartImage.getImageData(); 
    for (int y1 = y - 1; y1 <= y + 1; y1++)
    {
      if (y1 < 0 || y1 >= imageData.height)
        continue;
      for (int x1 = x - 1; x1 <= x + 1; x1++)
      {
        if (x1 < 0 || x1 >= imageData.width)
          continue;
        int pixel = imageData.getPixel(x1, y1);
        RGB rgb = imageData.palette.getRGB(pixel);
        for (int i = 0; i < indicators.size(); i++)
        {
          IChartPlotter plotter = (IChartPlotter)indicators.get(i);
          if (plotter.getColor().getRGB().equals(rgb) == true && !(plotter instanceof PriceChart) && !(plotter instanceof VolumeChart))
            return plotter;
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
  
  private void updateLabels()
  {
    Control[] children = labels.getChildren();
    for (int i = 0; i < children.length; i++)
      children[i].dispose();
    
    if (indicators.size() != 0)
    {
      for (int i = 0; i < indicators.size(); i++)
      {
        IChartPlotter plotter = (IChartPlotter)indicators.get(i);
        if (plotter.getDescription() != null)
        {
          Label label = new Label(labels, SWT.NONE);
          label.setText(((IChartPlotter)indicators.get(i)).getDescription());
          label.setForeground(plotter.getColor());
          label.setBackground(labels.getBackground());
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
    if (indicators.size() != 0 && y != -1)
    {
      ChartPlotter plotter = (ChartPlotter)indicators.get(0);
      scaleLabel.setText("  " + plotter.getFormattedValue(y, scale.getClientArea().height));
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
