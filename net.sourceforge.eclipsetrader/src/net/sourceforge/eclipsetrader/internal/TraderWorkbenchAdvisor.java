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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class TraderWorkbenchAdvisor extends WorkbenchAdvisor
{
  public void initialize(IWorkbenchConfigurer configurer) {
    super.initialize(configurer);
    configurer.setSaveAndRestore(true);
  }

  public void preWindowOpen(IWorkbenchWindowConfigurer configurer) 
  {
    super.preWindowOpen(configurer);
    configurer.setTitle("Eclipse Trader");
    configurer.setShowCoolBar(true);
    configurer.setShowPerspectiveBar(true);
    configurer.setShowStatusLine(true);
    configurer.setShowProgressIndicator(true);
    // By default dock the perspective bar on the top-right side
    PlatformUI.getPreferenceStore().setDefault(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, IWorkbenchPreferenceConstants.TOP_RIGHT);
    // Show the curvy view tabs
    PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
  }

  public String getInitialWindowPerspectiveId() 
  {
    return "net.sourceforge.eclipsetrader.TraderPerspective";
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#fillActionBars(org.eclipse.ui.IWorkbenchWindow, org.eclipse.ui.application.IActionBarConfigurer, int)
   */
  public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags)
  {
    super.fillActionBars(window, configurer, flags);
    if ((flags & FILL_MENU_BAR) != 0) {
      fillMenuBar(window, configurer);
    }
    if ((flags & FILL_COOL_BAR) != 0) {
      ICoolBarManager coolBar = configurer.getCoolBarManager();
      coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }
  }
  private void fillMenuBar(IWorkbenchWindow window, IActionBarConfigurer configurer) {
    IMenuManager menuBar = configurer.getMenuManager();
    menuBar.add(createFileMenu(window));
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menuBar.add(createWindowMenu(window));
    menuBar.add(createHelpMenu(window));
  }

  private MenuManager createFileMenu(IWorkbenchWindow window) 
  {
    MenuManager menu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
    menu.add(ActionFactory.QUIT.create(window));
    return menu;
  }

  private MenuManager createWindowMenu(IWorkbenchWindow window) 
  {
    MenuManager menu = new MenuManager("Window", IWorkbenchActionConstants.M_WINDOW);

    MenuManager perspectiveMenu = new MenuManager("Open_Perspective", "openPerspective"); //$NON-NLS-1$ //$NON-NLS-2$
    IContributionItem perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
    perspectiveMenu.add(perspectiveList);
    menu.add(perspectiveMenu);

    MenuManager viewMenu = new MenuManager("Show_View"); //$NON-NLS-1$
    IContributionItem viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
    viewMenu.add(viewList);
    menu.add(viewMenu);
    menu.add(new Separator());

    menu.add(ActionFactory.EDIT_ACTION_SETS.create(window));
    menu.add(ActionFactory.SAVE_PERSPECTIVE.create(window));
    menu.add(new Separator());
    menu.add(ActionFactory.PREFERENCES.create(window));

    return menu;
  }

  private MenuManager createHelpMenu(IWorkbenchWindow window) 
  {
    MenuManager menu = new MenuManager("Help", IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
    // Welcome or intro page would go here
    menu.add(ActionFactory.HELP_CONTENTS.create(window));
    // Tips and tricks page would go here
    menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
    menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
    menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    // About should always be at the bottom
    // To use the real RCP About dialog uncomment these lines
    menu.add(new Separator());
    menu.add(ActionFactory.ABOUT.create(window));

    return menu;
  }
}
