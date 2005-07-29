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

import java.util.Observable;

import net.sourceforge.eclipsetrader.IExtendedData;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class EditAction implements IWorkbenchWindowActionDelegate, IViewActionDelegate
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
      
      PortfolioDialog dlg = new PortfolioDialog();
      dlg.setSymbol(data.getSymbol());
      dlg.setTicker(data.getTicker());
      dlg.setDescription(data.getDescription());
      dlg.setMinimumQuantity(data.getMinimumQuantity());
      dlg.setQuantity(data.getQuantity());
      dlg.setPaid(data.getPaid());
      if (dlg.open() == PortfolioDialog.OK)
      {
        data.setSymbol(dlg.getSymbol());
        data.setTicker(dlg.getTicker());
        data.setDescription(dlg.getDescription());
        data.setMinimumQuantity(dlg.getMinimumQuantity());
        data.setQuantity(dlg.getQuantity());
        data.setPaid(dlg.getPaid());
        if (data instanceof Observable)
          ((Observable)data).notifyObservers();
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
