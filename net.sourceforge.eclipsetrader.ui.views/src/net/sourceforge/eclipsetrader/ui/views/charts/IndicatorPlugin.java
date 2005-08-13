/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan S. Stratigakos - Original qtstalker code
 *     Marco Maccaferri      - Java porting and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Base class for the indicator plugins.
 */
public abstract class IndicatorPlugin extends ChartObject
{
  private List output = new ArrayList();
  private BarData data;

  /**
   * Return the name of the chart.
   * 
   * @return the name of the chart
   */
  public String getPluginName()
  {
    return "";
  }
  
  /**
   * Get the line data used as input to this plugin.
   * 
   * @return the input line data
   */
  public BarData getData()
  {
    return this.data;
  }
  
  public void setBarData(BarData data)
  {
    this.data = data;
  }

  /**
   * Perform the calculations necessary to draw this indicator.
   * <p>Implementations must override this method and add the plotlines to the output list.</p>
   */
  public void calculate()
  {
  }
  
  /**
   * Get the output buffer list.
   * <p>Implementations should add all output lines to this buffer.</p>
   * 
   * @return the output buffer
   */
  public List getOutput()
  {
    return output;
  }

  public IWizardPage getWizardPage()
  {
    return null;
  }
  
  public IndicatorParametersPage getParametersPage()
  {
    return null;
  }

  public static Combo getLineTypeControl(Composite parent)
  {
    return getLineTypeControl(parent, "Line Type"); //$NON-NLS-1$
  }

  public static Combo getLineTypeControl(Composite parent, String text)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(text);
    Combo type = new Combo(parent, SWT.READ_ONLY);
    type.add("Dot");
    type.add("Dash");
    type.add("Histogram ");
    type.add("Histogram Bar");
    type.add("Line");
    type.add("Invisible");
    type.add("Horizontal");
    return type;
  }
}
