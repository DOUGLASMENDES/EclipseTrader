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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sourceforge.eclipsetrader.directa.DirectaPlugin;

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
public class BrowserActionReceiver implements IWorkbenchWindowActionDelegate, IViewActionDelegate
{
  private static String ID_PREFIX = "directa.browsePage.";
  private static String[] pages = {
    "http://www.directaworld.it/calendar1.html",
    "http://directatrading.com/trading/db2www/top15nd/input",
    "http://directatrading.com/trading/db2www/top15nd/input?Tipo=W",
    "http://directatrading.com/trading/db2www/scoperto/input",
    "http://directatrading.com/trading/db2www/marder2/input",
    "http://directatrading.com/trading/db2www/cambiodol/report",
    "http://directatrading.com/trading/estconc7?DATE=13112004&TPGE=A00",
    "http://directatrading.com/trading/estconc7?DATE=13112004&TPGE=D00",
    "http://directatrading.com/trading/db2www/ectitoli2/input?Tito=XXXXXX&TPGE=A00",
    "http://directatrading.com/trading/db2www/usectitoli/input?Tito=XXXXXX&TPGE=A00",
    "http://directatrading.com/trading/db2www/ectitoli2/input?Tito=XXXXXX&TPGE=D00",
    "http://directatrading.com/trading/db2www/usectitoli/input?Tito=XXXXXX&TPGE=D00",
    "http://directatrading.com/trading/db2www/messaggi2/input?TipDett=E",
    "http://directatrading.com/trading/db2www/trading/input",
    "http://directatrading.com/trading/capgac2",
    "http://directatrading.com/trading/db2www/perforder/input",
    "http://directatrading.com/trading/db2www/divesteri/input",
    "http://directatrading.com/trading/db2www/Sitprest/report",
    "http://notizie.directatrading.com/notizie/asca-ansa.php",
    "http://www.affaritaliani.it/directa_oraperora.htm",
  };
  protected SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");

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
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    if (Streamer.getInstance().isLoggedIn() == false)
    {
      if (DirectaPlugin.connectServer() == false)
        return;
    }

    try {
      IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      IViewPart browser = pg.showView("net.sourceforge.eclipsetrader.directa.views.browser");
      if (browser != null)
      {
        browser.setFocus();
        int index = Integer.parseInt(action.getId().substring(ID_PREFIX.length()));
        String page = pages[index];
        if (index != 0 && index != 18 && index != 19)
        {
          if (page.indexOf("?") != -1)
            page += "&";
          else
            page += "?";
          if (index == 6 || index == 7)
            page += "DATE=" + df.format(Calendar.getInstance().getTime()) + "&";
          page += "USER=" + Streamer.getUSER();
        }
        System.out.println("Browsing: " + page);
        ((WebBrowser)browser).setUrl(page);
      }
    } catch(PartInitException x) {};
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection)
  {
  }

}
