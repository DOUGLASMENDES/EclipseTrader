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
package net.sourceforge.eclipsetrader.ui.views.trading;

import net.sourceforge.eclipsetrader.IChartData;


/**
 * This class computes the data to be plotted by the chart view.
   * <p></p>
 * 
 * @author Marco Maccaferri - 15/08/2004
 */
public class Plotter
{
  /**
   * Compute and return the data to be plotted.
   * <p></p>
   */
  public double[] getPlotData(IChartData[] data)
  {
    double[] value = new double[data.length];
    for (int i = 0; i < value.length; i++)
      value[i] = data[i].getClosePrice();
    return value;
  }
}
