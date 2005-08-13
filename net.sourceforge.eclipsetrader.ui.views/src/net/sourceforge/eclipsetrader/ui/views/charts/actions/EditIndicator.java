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

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.ChartSelection;
import net.sourceforge.eclipsetrader.ui.views.charts.ChartView;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 */
public class EditIndicator implements IViewActionDelegate, IWorkbenchWindowActionDelegate
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
      final IndicatorPlugin item = (IndicatorPlugin)view.getSelectedZone().getSelectedItem();
      final IndicatorParametersPage parametersPage = item.getParametersPage();

      TitleAreaDialog dialog = new TitleAreaDialog(view.getViewSite().getShell()) {

        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        protected Control createDialogArea(Composite parent)
        {
          setTitle(item.getPluginName());
          setMessage(Messages.getString("ChartParametersDialog.message") + item.getPluginName()); //$NON-NLS-1$
          
          parametersPage.createControl(parent);

          return super.createDialogArea(parent);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        protected void okPressed()
        {
          parametersPage.performFinish();
          super.okPressed();
        }
      };
      
      if (dialog.open() == TitleAreaDialog.OK)
        view.getSelectedZone().redraw();
      view.savePreferences();
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

    this.action = action;
    if (list.indexOf(action) == -1)
      list.add(action);
  }
}
