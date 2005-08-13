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

import net.sourceforge.eclipsetrader.ui.views.charts.wizards.NewIndicatorWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 */
public class RealtimeChartActions implements IViewActionDelegate
{

  /* (non-Javadoc)
   * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
   */
  public void init(IViewPart viewPart)
  {
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (pg.getActivePart() instanceof RealtimeChartView)
    {
      RealtimeChartView view = (RealtimeChartView)pg.getActivePart();

      if (action.getId().equalsIgnoreCase("chart.add") == true)
      {
        NewIndicatorWizard wizard = new NewIndicatorWizard();
//        wizard.setChartView(view);
        wizard.open();
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
      else if (action.getId().equalsIgnoreCase("chart.refresh") == true)
        view.refreshChart();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (page != null && page.getActivePart() instanceof RealtimeChartView)
    {
      RealtimeChartView view = (RealtimeChartView)page.getActivePart();

      if (action.getId().equalsIgnoreCase("chart.line") == true)
        action.setChecked(view.getChartType() == PriceChart.LINE);
      else if (action.getId().equalsIgnoreCase("chart.candle") == true)
        action.setChecked(view.getChartType() == PriceChart.CANDLE);
      else if (action.getId().equalsIgnoreCase("chart.bar") == true)
        action.setChecked(view.getChartType() == PriceChart.BAR);
    }
  }
}
