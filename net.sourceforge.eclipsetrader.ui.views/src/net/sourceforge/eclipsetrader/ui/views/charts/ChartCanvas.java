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

import java.util.Vector;

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Define a canvas over which a IChartPlotter object can draw.
 * <p></p>
 * 
 * @since 1.0
 */
public class ChartCanvas implements ControlListener, PaintListener, ISelectionProvider
{
  private Composite container;
  private Composite chartContainer;
  private Canvas chart;
  private Canvas scale;
  private Color chartBackground = new Color(Display.getCurrent(), 255, 255, 240);
  private Color scaleBackground = new Color(Display.getCurrent(), 255, 255, 240);
  private int columnWidth = 5;
  private int margin = 2;
  private int scaleWidth = 60;
  private Vector painters = new Vector();
  private IChartData[] data;

  /**
   * Standard SWT widget constructor.
   * <p>The style refers to the same styles available for the Canvas widget.</p>
   */
  public ChartCanvas(Composite parent)
  {
    // Main container with a GridLayout of 2 columns to accomodate for the chart
    // and the scale
    container = new Composite(parent, SWT.NONE);
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
    container.dispose();
  }
  
  public void createContextMenu(IViewPart part) 
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
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection()
  {
    return null;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
  }
  
  public void addMouseListener(Object listener)
  {
    if (listener instanceof MouseMoveListener)
      chart.addMouseMoveListener((MouseMoveListener)listener);
    if (listener instanceof MouseListener)
      chart.addMouseListener((MouseListener)listener);
  }
  
  public void removeMouseListener(Object listener)
  {
    if (listener instanceof MouseMoveListener)
      chart.removeMouseMoveListener((MouseMoveListener)listener);
    if (listener instanceof MouseListener)
      chart.removeMouseListener((MouseListener)listener);
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
  }
  
  /**
   * Remove a painter object from this canvas.
   * <p></p>
   */
  public void removePainter(IChartPlotter plotter)
  {
    painters.removeElement(plotter);
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
        GridData gridData = (GridData)scale.getLayoutData();
        gridData.widthHint = scaleWidth;
        container.layout();
        chart.redraw();
        scale.redraw();
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
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    if (data != null)
    {
      if (e.getSource() == chart)
      { 
        for (int i = 0; i < painters.size(); i++)
        {
          Object obj = painters.elementAt(i);
          if (obj instanceof IChartPlotter)
            ((IChartPlotter)obj).paintChart(e.gc, chart.getClientArea().width, chart.getClientArea().height);
        }
        int y = 0;
        for (int i = 0; i < painters.size(); i++)
        {
          IChartPlotter plotter = (IChartPlotter)painters.elementAt(i);
          if (plotter.getDescription() != null)
          {
            e.gc.setForeground(plotter.getColor());
            e.gc.drawString(((IChartPlotter)painters.elementAt(i)).getDescription(), 2, y);
            y += e.gc.getFontMetrics().getHeight();
          }
        }
      }
      else if (e.getSource() == scale)
      {
        for (int i = 0; i < painters.size(); i++)
        {
          Object obj = painters.elementAt(i);
          if (obj instanceof IChartPlotter)
            ((IChartPlotter)painters.elementAt(i)).paintScale(e.gc, scale.getClientArea().width, scale.getClientArea().height);
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
  }
}
