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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * @author Marco
 */
public class ChartDialog extends TitleAreaDialog implements SelectionListener
{
  private List list;
  private IChartPlotter obj;
  private Vector chart;
  
  public ChartDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
  }
  
  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Oscillatori");
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout(1, false));
    
    list = new List(composite, SWT.SINGLE|SWT.BORDER);
    GridData gridData = new GridData(GridData.GRAB_VERTICAL|GridData.VERTICAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL);
    gridData.heightHint = 250;
    list.setLayoutData(gridData);
    list.addSelectionListener(this);
    
    // Add the plugin names to the listbox
    int index = 0;
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      for (int ii = 0; ii < canvas.getPainterCount(); ii++)
      {
        IChartPlotter painter = canvas.getPainter(ii);
        if (painter instanceof PriceChart || painter instanceof VolumeChart)
          continue;
        list.add(painter.getDescription(), index);
        list.setData(String.valueOf(index), painter);
        index++;
      }
    }

    return super.createDialogArea(parent);
  }

  public int open()
  {
    create();
    
    setTitle("Selezione Oscillatore");
    setMessage("Selezionare l'oscillatore da modificare.");
    
    return super.open();
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
    int index = list.getSelectionIndex();
    obj = (IChartPlotter)list.getData(String.valueOf(index));
  }

  /**
   * Method to return the obj field.<br>
   *
   * @return Returns the obj.
   */
  public IChartPlotter getObject()
  {
    return obj;
  }

  /**
   * Method to set the chart field.<br>
   * 
   * @param chart The chart to set.
   */
  public void setChart(Vector chart)
  {
    this.chart = chart;
  }
}
