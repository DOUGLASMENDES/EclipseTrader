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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @author Marco
 */
public class RSIChartDialog implements ModifyListener
{
  private String period = "30";
  private Text text2;
  
  public RSIChartDialog()
  {
    String name = "R.S.I.";
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartParametersDialog#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Periodi Selezionati");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text2 = new Text(parent, SWT.BORDER);
    text2.addModifyListener(this);
    text2.setText(period);
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text2.setLayoutData(gridData);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText(ModifyEvent e)
  {
    if (e.getSource() == text2)
      period = text2.getText();
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
