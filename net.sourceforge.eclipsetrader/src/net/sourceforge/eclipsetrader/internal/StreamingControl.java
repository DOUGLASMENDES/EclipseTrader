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
package net.sourceforge.eclipsetrader.internal;

import net.sourceforge.eclipsetrader.IBasicDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Handle the actions for the start streaming and stop streaming toolbar buttons.
 * <p></p>
 * @since 1.0
 */
public class StreamingControl implements IWorkbenchWindowActionDelegate
{
  public static IAction actionStart;
  public static IAction actionStop;

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
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.startStreaming") == true)
    {
      IBasicDataProvider dataProvider = TraderPlugin.getDataProvider();
      if (dataProvider == null)
      {
        // remind user to choose Data Provider
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageBox msg = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        msg.setMessage("Choose Data Provider\nIt is under Window -> Preferences");
        msg.open();
      } 
      else
        TraderPlugin.getDataProvider().startStreaming();
    }
    else if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.stopStreaming") == true)
    {
      IBasicDataProvider dataProvider = TraderPlugin.getDataProvider();
      if (dataProvider != null)
        TraderPlugin.getDataProvider().stopStreaming();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
    if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.startStreaming") == true)
      actionStart = action;
    else if (action.getId().equalsIgnoreCase("net.sourceforge.eclipsetrader.stopStreaming") == true)
    {
      actionStop = action;
      action.setEnabled(false);
    }
  }

}
