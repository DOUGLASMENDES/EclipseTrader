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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Stockwatch view action handler.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class ContextAction implements IViewActionDelegate
{
  private static PortfolioView view;
  
  /*
   * @see IViewActionDelegate#init(IViewPart)
   */
  public void init(IViewPart viewPart) 
  {
    if (viewPart instanceof PortfolioView)
      view = (PortfolioView)viewPart;
  }

  /*
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) 
  {
    if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.views.openChart") == true)
      view.openHistoryChart();
    else if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.views.openRealtimeChart") == true)
      view.openRealtimeChart();
    else if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.views.openPriceBook") == true)
      view.openPriceBook();
    else if (action.getId().equalsIgnoreCase("portfolio.moveup") == true)
      view.moveUp();
    else if (action.getId().equalsIgnoreCase("portfolio.movedown") == true)
      view.moveDown();
    else if (action.getId().equalsIgnoreCase("portfolio.add") == true)
      view.addItem();
    else if (action.getId().equalsIgnoreCase("portfolio.edit") == true)
      view.editItem();
    else if (action.getId().equalsIgnoreCase("portfolio.remove") == true)
      view.deleteItem();
    else if (action.getId().equalsIgnoreCase("portfolio.editAlerts") == true)
      view.editAlerts();
    else if (action.getId().equalsIgnoreCase("portfolio.clearAlerts") == true)
      view.clearAlerts();
    else
      System.out.println(action.getId());
  }

  /*
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
