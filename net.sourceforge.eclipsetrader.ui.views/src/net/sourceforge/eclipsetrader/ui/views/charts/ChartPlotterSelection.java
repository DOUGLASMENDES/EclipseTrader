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

import org.eclipse.jface.viewers.ISelection;

/**
 * Instances of this class are used to enable or disable the menu and toolbar
 * items based on the selected chart component.
 */
public class ChartPlotterSelection implements ISelection
{
  private IChartPlotter plotter = null;
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelection#isEmpty()
   */
  public boolean isEmpty()
  {
    return (plotter == null);
  }

  /**
   * Method to return the plotter field.<br>
   *
   * @return Returns the plotter.
   */
  public IChartPlotter getPlotter()
  {
    return plotter;
  }

  /**
   * Method to set the plotter field.<br>
   * 
   * @param plotter The plotter to set.
   */
  public void setPlotter(IChartPlotter plotter)
  {
    this.plotter = plotter;
  }
}
