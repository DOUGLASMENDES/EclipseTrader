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

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.ChartSelection;
import net.sourceforge.eclipsetrader.ui.views.charts.ChartView;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 */
public class DeleteIndicator implements IViewActionDelegate, IWorkbenchWindowActionDelegate
{

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
      Object item = view.getSelectedZone().getSelectedItem();
      if (MessageDialog.openConfirm(view.getViewSite().getShell(), Messages.getString("ChartView.ConfirmDeleteTitle"), Messages.getString("ChartView.ConfirmDeleteMessage")) == true) //$NON-NLS-1$ //$NON-NLS-2$
      {
        view.getSelectedZone().getIndicators().remove((IndicatorPlugin)item);
        view.getSite().getSelectionProvider().setSelection(new ChartSelection(view.getSelectedZone()));
        view.savePreferences();
      }
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
    if (selection instanceof ChartSelection && ((ChartSelection)selection).getChartCanvas() != null)
      action.setEnabled(((ChartSelection)selection).getChartCanvas().getSelectedItem() instanceof IndicatorPlugin);
    else
      action.setEnabled(false);
  }
}
