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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * The main application class
 */
public class Application implements IPlatformRunnable
{

  /**
   * The application entry point
   */
  public Object run(Object args) throws CoreException
  {
/*    IChartDataProvider cdp = new ChartDataProvider();
    cdp.load("CPTA.MI");
    cdp.update();
    cdp.store();
    
    return IPlatformRunnable.EXIT_OK;*/
    
    // Application launching
    WorkbenchAdvisor workbenchAdvisor = new TraderWorkbenchAdvisor();
    Display display = PlatformUI.createDisplay();
    try {
      int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
      
      if (returnCode == PlatformUI.RETURN_RESTART)
        return IPlatformRunnable.EXIT_RESTART;
      else
        return IPlatformRunnable.EXIT_OK;
    } finally {
      display.dispose();
    }
  }
}
