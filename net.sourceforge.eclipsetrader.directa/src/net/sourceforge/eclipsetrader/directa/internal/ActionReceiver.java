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
package net.sourceforge.eclipsetrader.directa.internal;

import net.sourceforge.eclipsetrader.directa.DirectaPlugin;
import net.sourceforge.eclipsetrader.directa.ui.views.Orders;
import net.sourceforge.eclipsetrader.directa.ui.views.Trading;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 */
public class ActionReceiver implements IWorkbenchWindowActionDelegate, IViewActionDelegate
{
  private static Orders ordersView;
  private static Trading tradingView;
  private static WebBrowser browserView;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose()
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
   */
  public void init(IViewPart view)
  {
    if (view instanceof Orders)
      ordersView = (Orders)view;
    if (view instanceof Trading)
      tradingView = (Trading)view;
    if (view instanceof WebBrowser)
      browserView = (WebBrowser)view;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    if (action.getId().equalsIgnoreCase("directa.showTrading") == true)
    {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
        page.showView("net.sourceforge.eclipsetrader.directa.views.trading");
      } catch (PartInitException e) {}
    }
    else if (action.getId().equalsIgnoreCase("directa.showOrders") == true)
    {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
        page.showView("net.sourceforge.eclipsetrader.directa.views.orders");
      } catch (PartInitException e) {}
    }
    else if (action.getId().equalsIgnoreCase("orders.cancel") == true)
    {
      if (ordersView != null)
        ordersView.cancelOrder();
    }
    else if (action.getId().equalsIgnoreCase("trading.refresh") == true)
    {
      Streamer streamer = Streamer.getInstance();
      if (streamer.isLoggedIn() == false)
      {
        if (DirectaPlugin.connectServer() == false)
          return;
      }
      streamer.updateValues();
      streamer.updateOrderStatus();
    }
    else if (action.getId().equalsIgnoreCase("orders.refresh") == true)
    {
      Streamer streamer = Streamer.getInstance();
      if (streamer.isLoggedIn() == false)
      {
        if (DirectaPlugin.connectServer() == false)
          return;
      }
      streamer.updateOrderStatus();
      streamer.updateValues();
    }
    else if (action.getId().equalsIgnoreCase("browser.refresh") == true)
    {
      if (browserView != null)
        browserView.refresh();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
  }

}
