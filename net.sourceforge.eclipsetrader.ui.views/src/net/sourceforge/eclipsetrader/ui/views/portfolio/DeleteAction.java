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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class DeleteAction implements IWorkbenchWindowActionDelegate, IViewActionDelegate
{
  private PortfolioSelection selection;
  
  /* (non-Javadoc)
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

  /* (non-Javadoc)
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) 
  {
    if (selection != null && !selection.isEmpty())
    {
      if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.getString("PortfolioView.ConfirmDeleteTitle"), Messages.getString("PortfolioView.ConfirmDeleteMessage")) == true) //$NON-NLS-1$ //$NON-NLS-2$
        TraderPlugin.getDataStore().getStockwatchData().remove(selection.getData());
    }
  }

  /* (non-Javadoc)
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) 
  {
    if (selection instanceof PortfolioSelection)
      this.selection = (PortfolioSelection)selection;
  }
}
