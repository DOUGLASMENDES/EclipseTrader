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
package net.sourceforge.eclipsetrader.ui.views.charts.actions;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.ui.views.charts.ChartSelection;
import net.sourceforge.eclipsetrader.ui.views.charts.ChartView;
import net.sourceforge.eclipsetrader.ui.views.charts.HistoryChartView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SetPeriod implements IViewActionDelegate, IWorkbenchWindowActionDelegate
{
  private static List list = new ArrayList();
  private IAction action;

  /* (non-Javadoc)
   * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
   */
  public void init(IViewPart viewPart)
  {
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose()
  {
    if (list.indexOf(action) != -1)
      list.remove(action);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (pg.getActivePart() instanceof ChartView)
    {
      ChartView view = (ChartView)pg.getActivePart();
      if (action.getId().equalsIgnoreCase("view.all") == true)
        view.setLimitPeriod(0);
      else if (action.getId().equalsIgnoreCase("view.last6months") == true)
        view.setLimitPeriod(6);
      else if (action.getId().equalsIgnoreCase("view.last1year") == true)
        view.setLimitPeriod(12);
      else if (action.getId().equalsIgnoreCase("view.last2years") == true)
        view.setLimitPeriod(24);
      view.savePreferences();
      view.getSite().getSelectionProvider().setSelection(new ChartSelection(view.getSelectedZone()));
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
    IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (pg != null)
    {
      action.setEnabled(pg.getActivePart() instanceof HistoryChartView);
  
      if (pg.getActivePart() instanceof ChartView)
      {
        ChartView view = (ChartView)pg.getActivePart();
        if (action.getId().equalsIgnoreCase("view.all") == true)
          action.setChecked(view.getLimitPeriod() == 0);
        else if (action.getId().equalsIgnoreCase("view.last6months") == true)
          action.setChecked(view.getLimitPeriod() == 6);
        else if (action.getId().equalsIgnoreCase("view.last1year") == true)
          action.setChecked(view.getLimitPeriod() == 12);
        else if (action.getId().equalsIgnoreCase("view.last2years") == true)
          action.setChecked(view.getLimitPeriod() == 24);
      }
    }

    this.action = action;
    if (list.indexOf(action) == -1)
      list.add(action);
  }
}
