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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @author Marco
 */
public class AverageChartDialog
{
  private String period = "30";
  private Text selectedPeriods;
  
  public AverageChartDialog()
  {
    String name = "Moving Average";
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartParametersDialog#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Selected Periods");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    selectedPeriods = new Text(parent, SWT.BORDER);
    selectedPeriods.setText(period);
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    selectedPeriods.setLayoutData(gridData);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed()
  {
    period = selectedPeriods.getText();
//    super.okPressed();
  }

  /**
   * Method to return the period field.<br>
   *
   * @return Returns the period.
   */
  public int getPeriod()
  {
    return Integer.parseInt(period);
  }
  /**
   * Method to set the period field.<br>
   * 
   * @param period The period to set.
   */
  public void setPeriod(int period)
  {
    this.period = String.valueOf(period);
  }
}
