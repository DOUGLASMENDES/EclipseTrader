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

import java.util.Vector;

import net.sourceforge.eclipsetrader.IChartData;

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
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Define a canvas over which a IChartPlotter object can draw.
 * <p></p>
 * 
 * @since 1.0
 */
public class ChartCanvas extends Composite implements ControlListener, PaintListener, ISelectionProvider
{
  private boolean hilight = false;
  private Composite container;
  private Composite chartContainer;
  private Composite labels;
  private Canvas chart;
  private Canvas scale;
  private Image chartImage;
  private Color chartBackground = new Color(Display.getCurrent(), 255, 255, 240);
  private Color scaleBackground = new Color(Display.getCurrent(), 255, 255, 240);
  private Color separatorColor = new Color(null, 255, 0, 0);
  private int columnWidth = 5;
  private int margin = 2;
  private int scaleWidth = 60;
  private Vector painters = new Vector();
  private IChartData[] data;
  private ChartPlotterSelection selection = new ChartPlotterSelection();
  private Vector selectionChangedListeners = new Vector();

  /**
   * Standard SWT widget constructor.
   * <p>The style refers to the same styles available for the Canvas widget.</p>
   */
  public ChartCanvas(Composite parent)
  {
    super(parent, SWT.NONE);

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
    
    // Scale canvas
    scale = new Canvas(container, SWT.NONE);
    scale.setBackground(scaleBackground);
    GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);
    gridData.widthHint = scaleWidth;
    scale.setLayoutData(gridData);
    scale.addPaintListener(this);
    
    updateLabels();
  }
  
  public void dispose()
  {
    scale.removePaintListener(this);
    chartContainer.removeControlListener(this);
    chart.removePaintListener(this);
    
    painters.removeAllElements();

    scale.dispose();
    chart.dispose();
    chartContainer.dispose();
    if (chartImage != null)
      chartImage.dispose();
    
    super.dispose();
  }
  
  public void createContextMenu(IViewPart part) 
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
    part.getSite().registerContextMenu(menuMgr, this);
  }
  
  public Composite getContainer()
  {
    return container;
  }
  
  public Composite getChart()
  {
    return chart;
  }
  
  public Composite getScale()
  {
    return scale;
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
      ((ISelectionChangedListener)selectionChangedListeners.elementAt(i)).selectionChanged(new SelectionChangedEvent(this, selection));
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

  /**
   * Add a painter object to this canvas.
   * <p></p>
   * <p><b>Note:</b><br>The charts are painted in the same order as they are added.</p>
   */
  public void addPainter(IChartPlotter plotter)
  {
    painters.addElement(plotter);
    plotter.setCanvas(this);
    updateLabels();
  }
  
  public void updatePainter(IChartPlotter plotter)
  {
    updateLabels();
  }
  
  /**
   * Remove a painter object from this canvas.
   * <p></p>
   */
  public void removePainter(IChartPlotter plotter)
  {
    painters.removeElement(plotter);
    updateLabels();
  }
  
  /**
   * Return the number of painter objects in this canvas.
   * <p></p>
   */
  public int getPainterCount()
  {
    return painters.size();
  }
  
  /**
   * Return the painter object at index position.
   * <p></p>
   */
  public IChartPlotter getPainter(int index)
  {
    return (IChartPlotter)painters.elementAt(index);
  }
  
  /**
   * Set the chart data to all registered painter objects.
   * <p></p>
   */
  public void setData(IChartData[] chartData)
  {
    data = chartData;
    for (int i = 0; i < painters.size(); i++)
      ((IChartPlotter)painters.elementAt(i)).setData(data);
    
    if (chartContainer.isDisposed() == true)
      return;
    chartContainer.getDisplay().asyncExec(new Runnable() {
      public void run()  
      {
        setControlSize();
        chart.redraw();
        GridData gridData = (GridData)scale.getLayoutData();
        gridData.widthHint = scaleWidth;
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
  
  /**
   * Method to return the hilight field.<br>
   *
   * @return Returns the hilight.
   */
  public boolean isHilight()
  {
    return hilight;
  }

  /**
   * Method to set the hilight field.<br>
   * 
   * @param hilight The hilight to set.
   */
  public void setHilight(boolean hilight)
  {
    this.hilight = hilight;
    scale.redraw();
  }
  
  public void redraw()
  {
    chart.redraw();
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
        GC gc = new GC(chartImage);
        gc.setBackground(chartBackground);
        gc.fillRectangle(chartImage.getBounds());
        for (int i = 0; i < painters.size(); i++)
        {
          Object obj = painters.elementAt(i);
          if (obj instanceof IChartPlotter)
            ((IChartPlotter)obj).paintChart(gc, chart.getClientArea().width, chart.getClientArea().height);
        }
        gc.dispose();
        e.gc.drawImage(chartImage, 0, 0);
      }
      else if (e.getSource() == scale)
      {
        for (int i = 0; i < painters.size(); i++)
        {
          Object obj = painters.elementAt(i);
          if (obj instanceof IChartPlotter)
            ((IChartPlotter)painters.elementAt(i)).paintScale(e.gc, scale.getClientArea().width, scale.getClientArea().height);
        }
  
        if (isHilight() == true)
        {
          e.gc.setForeground(separatorColor);
          e.gc.drawLine(0, 0, 0, scale.getClientArea().height);
        }
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
    setControlSize();
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
  }
  
  public void selectChart(IChartPlotter plotter)
  {
    if (plotter != selection.getPlotter())
    {
      if (selection.isEmpty() == false)
        ((ChartPlotter)selection.getPlotter()).setSelected(false);
      
      selection.setPlotter(plotter);
      setSelection(selection);
  
      if (plotter != null && ((ChartPlotter)plotter).isSelected() == false)
        ((ChartPlotter)plotter).setSelected(true);
  
      chart.redraw();
    }
  }
  
  public IChartPlotter getSelectedChart(int x, int y)
  {

    ImageData imageData = chartImage.getImageData(); 
    for (int y1 = y - 1; y1 <= y + 1; y1++)
    {
      for (int x1 = x - 1; x1 <= x + 1; x1++)
      {
        int pixel = imageData.getPixel(x1, y1);
        RGB rgb = imageData.palette.getRGB(pixel);
        for (int i = 0; i < painters.size(); i++)
        {
          IChartPlotter plotter = (IChartPlotter)painters.elementAt(i);
          if (plotter.getColor().getRGB().equals(rgb) == true && !(plotter instanceof PriceChart) && !(plotter instanceof VolumeChart))
            return plotter;
        }
      }
    }
    return null;
  }
  
  private void updateLabels()
  {
    Control[] children = labels.getChildren();
    for (int i = 0; i < children.length; i++)
      children[i].dispose();
    
    if (painters.size() != 0)
    {
      for (int i = 0; i < painters.size(); i++)
      {
        IChartPlotter plotter = (IChartPlotter)painters.elementAt(i);
        if (plotter.getDescription() != null)
        {
          Label label = new Label(labels, SWT.NONE);
          label.setText(((IChartPlotter)painters.elementAt(i)).getDescription());
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
}
