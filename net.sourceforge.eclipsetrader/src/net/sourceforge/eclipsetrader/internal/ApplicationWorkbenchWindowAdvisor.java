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
package net.sourceforge.eclipsetrader.internal;

import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{

  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
  {
    super(configurer);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
   */
  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer)
  {
    return new ApplicationActionBarAdvisor(configurer);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
   */
  public void preWindowOpen()
  {
    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

    configurer.setTitle("Eclipse Trader"); //$NON-NLS-1$
    configurer.setShowCoolBar(true);
    configurer.setShowPerspectiveBar(true);
    configurer.setShowStatusLine(true);
    configurer.setShowProgressIndicator(true);

    // By default dock the perspective bar on the top-right side
    PlatformUI.getPreferenceStore().setDefault(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, IWorkbenchPreferenceConstants.TOP_RIGHT);

    // Show the curvy view tabs
    PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowShellClose()
   */
  public boolean preWindowShellClose()
  {
    IPreferenceStore store = TraderPlugin.getDefault().getPreferenceStore();
    boolean promptOnExit = store.getBoolean("net.sourceforge.eclipsetrader.promptOnExit"); //$NON-NLS-1$

    if (promptOnExit)
    {
      MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(getWindowConfigurer().getWindow().getShell(), Messages.getString("TraderWorkbenchAdvisor.ConfirmExit"), //$NON-NLS-1$
          Messages.getString("TraderWorkbenchAdvisor.ExitMessage"), //$NON-NLS-1$
          Messages.getString("TraderWorkbenchAdvisor.ExitWithoutPrompt"), //$NON-NLS-1$
          false, null, null);
      if (dlg.getReturnCode() != IDialogConstants.OK_ID)
        return false;
      if (dlg.getToggleState())
      {
        store.setValue("net.sourceforge.eclipsetrader.promptOnExit", false); //$NON-NLS-1$
        TraderPlugin.getDefault().savePluginPreferences();
      }
    }

    return super.preWindowShellClose();
  }
}
