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
package net.sourceforge.eclipsetrader.ui.views.news;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NewsAction implements IViewActionDelegate
{
  private static NewsView view;
  
  /*
   * @see IViewActionDelegate#init(IViewPart)
   */
  public void init(IViewPart viewPart) 
  {
    if (viewPart instanceof NewsView)
      view = (NewsView)viewPart;
    else if (view == null)
    {
      IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      view = (NewsView)pg.findView("net.sourceforge.eclipsetrader.ui.views.News");
    }
  }

  /*
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) 
  {
    if (action.getId().equalsIgnoreCase("news.refresh") == true)
      view.startUpdate();
    if (action.getId().equalsIgnoreCase("news.next") == true)
      view.next();
    if (action.getId().equalsIgnoreCase("news.previous") == true)
      view.previous();
  }

  /*
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
