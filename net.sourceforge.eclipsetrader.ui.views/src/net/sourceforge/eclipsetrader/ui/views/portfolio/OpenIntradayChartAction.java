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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;
import net.sourceforge.eclipsetrader.ui.views.charts.RealtimeChartView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OpenIntradayChartAction implements IWorkbenchWindowActionDelegate, IViewActionDelegate
{
  
  /*
   * @see IViewActionDelegate#init(IViewPart)
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

  /*
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) 
  {
    IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (pg.getActivePart() instanceof PortfolioView)
    {
      PortfolioView view = (PortfolioView)pg.getActivePart();
      IExtendedData data = view.getSelectedItem();
      if (data != null)
      {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (int i = 1;; i++)
        {
          IViewReference ref = page.findViewReference(RealtimeChartView.VIEW_ID, String.valueOf(i));
          if (ref == null)
          {
            ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + String.valueOf(i), data.getSymbol()); //$NON-NLS-1$
            try {
              page.showView(RealtimeChartView.VIEW_ID, String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException e) {}
            break;
          }
        }
      }
    }
  }

  /*
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) 
  {
  }
}
