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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 */
public class ChartActions implements IViewActionDelegate
{
  private static HistoryChartView view;

  /* (non-Javadoc)
   * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
   */
  public void init(IViewPart viewPart)
  {
    if (viewPart instanceof HistoryChartView)
      view = (HistoryChartView)viewPart;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    if (action.getId().equalsIgnoreCase("chart.refresh") == true)
      view.updateChart();
    else if (action.getId().equalsIgnoreCase("chart.next") == true)
      view.showNext();
    else if (action.getId().equalsIgnoreCase("chart.previous") == true)
      view.showPrevious();
    else if (action.getId().equalsIgnoreCase("chart.add") == true)
    {
      OscillatorDialog dlg = new OscillatorDialog();
      if (dlg.open() == OscillatorDialog.OK)
        view.addOscillator(dlg.getId());
    }
    else if (action.getId().equalsIgnoreCase("chart.edit") == true)
      view.editOscillator();
    else if (action.getId().equalsIgnoreCase("chart.remove") == true)
      view.removeOscillator();
    else if (action.getId().equalsIgnoreCase("chart.line") == true)
      view.setChartType(PriceChart.LINE);
    else if (action.getId().equalsIgnoreCase("chart.candle") == true)
      view.setChartType(PriceChart.CANDLE);
    else if (action.getId().equalsIgnoreCase("chart.bar") == true)
      view.setChartType(PriceChart.BAR);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
  }
}
