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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class TraderWorkbenchAdvisor extends WorkbenchAdvisor
{
  private IWorkbenchWindowConfigurer configurer;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
   */
  public void initialize(IWorkbenchConfigurer configurer)
  {
    super.initialize(configurer);
    configurer.setSaveAndRestore(true);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#preWindowOpen(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
   */
  public void preWindowOpen(IWorkbenchWindowConfigurer configurer)
  {
    super.preWindowOpen(configurer);
    this.configurer = configurer;

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
   * @see org.eclipse.ui.application.WorkbenchAdvisor#preShutdown()
   */
  public boolean preShutdown()
  {
    IPreferenceStore store = TraderPlugin.getDefault().getPreferenceStore();
    boolean promptOnExit = store.getBoolean("net.sourceforge.eclipsetrader.promptOnExit"); //$NON-NLS-1$

    if (promptOnExit) 
    {
      MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(
        configurer.getWindow().getShell(),
          Messages.getString("TraderWorkbenchAdvisor.ConfirmExit"), //$NON-NLS-1$
          Messages.getString("TraderWorkbenchAdvisor.ExitMessage"), //$NON-NLS-1$
          Messages.getString("TraderWorkbenchAdvisor.ExitWithoutPrompt"), //$NON-NLS-1$
          false,
          null,
          null);
      if (dlg.getReturnCode() != IDialogConstants.OK_ID)
        return false;
      if (dlg.getToggleState()) 
      {
        store.setValue("net.sourceforge.eclipsetrader.promptOnExit", false); //$NON-NLS-1$
        TraderPlugin.getDefault().savePluginPreferences();
      } 
    }
    return super.preShutdown();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
   */
  public String getInitialWindowPerspectiveId()
  {
    return "net.sourceforge.eclipsetrader.TraderPerspective"; //$NON-NLS-1$
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#getMainPreferencePageId()
   */
  public String getMainPreferencePageId()
  {
    return "eclipsetrader"; //$NON-NLS-1$
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
  
  private void fillMenuBar(IWorkbenchWindow window, IActionBarConfigurer configurer) 
  {
    IMenuManager menuBar = configurer.getMenuManager();
    menuBar.add(createFileMenu(window));
    menuBar.add(new GroupMarker("group1")); //$NON-NLS-1$
    menuBar.add(new GroupMarker("group2")); //$NON-NLS-1$
    menuBar.add(new GroupMarker("group3")); //$NON-NLS-1$
    menuBar.add(new GroupMarker("group4")); //$NON-NLS-1$
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menuBar.add(createWindowMenu(window));
    menuBar.add(createHelpMenu(window));
  }

  private MenuManager createFileMenu(IWorkbenchWindow window) 
  {
    MenuManager menu = new MenuManager(Messages.getString("TraderWorkbenchAdvisor.File"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$

    menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
    menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
    IWorkbenchAction quitAction = ActionFactory.QUIT.create(window);
    menu.add(quitAction);
    return menu;
  }

  private MenuManager createWindowMenu(IWorkbenchWindow window) 
  {
/*
    ContributionItemFactory VIEWS_SHORTLIST = new ContributionItemFactory("viewsShortlist") { //$NON-NLS-1$
      // (non-javadoc) method declared on ContributionItemFactory
      public IContributionItem create(IWorkbenchWindow window) {
        if (window == null) {
          throw new IllegalArgumentException();
        }
        // indicate that a show views submenu has been created
         ((WorkbenchWindow)window).addSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);
        IContributionItem item = new ShowViewMenu(window, getId());
        return item;
      }
    };
*/
    MenuManager menu = new MenuManager(Messages.getString("TraderWorkbenchAdvisor.Window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$

/*    MenuManager showViewMenuMgr = new MenuManager("Show View", "showView");
    IContributionItem showViewMenu = VIEWS_SHORTLIST.create(window);
    showViewMenuMgr.add(showViewMenu);
    menu.add(showViewMenuMgr); 
    menu.add(new Separator());*/
    
    menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menu.add(new Separator());
    IWorkbenchAction action = ActionFactory.EDIT_ACTION_SETS.create(window);
    action.setEnabled(true);
    menu.add(action);
    action = ActionFactory.RESET_PERSPECTIVE.create(window);
    action.setEnabled(true);
    menu.add(action);
    menu.add(new Separator());
    menu.add(ActionFactory.PREFERENCES.create(window));

    return menu;
  }

  private MenuManager createHelpMenu(IWorkbenchWindow window) 
  {
    MenuManager menu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
    // Welcome or intro page would go here
    menu.add(ActionFactory.HELP_CONTENTS.create(window));
    // Tips and tricks page would go here
    menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
    menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
    menu.add(new GroupMarker("group.tutorials")); //$NON-NLS-1$
    menu.add(new GroupMarker("group.tools")); //$NON-NLS-1$
    menu.add(new GroupMarker("group.updates")); //$NON-NLS-1$
    menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END)); 
    menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    // About should always be at the bottom
    // To use the real RCP About dialog uncomment these lines
    menu.add(new Separator("group.about")); //$NON-NLS-1$ 
    menu.add(ActionFactory.ABOUT.create(window));
    menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$ 

    return menu;
  }

  /**
   * Adds a <code>GroupMarker</code> or <code>Separator</code> to a menu.  The 
   * test for whether a separator should be added is done by checking for the existence
   * of a preference matching the string useSeparator.MENUID.GROUPID that is set
   * to <code>true</code>.
   * 
   * @param menu  the menu to add to
   * @param string  the group id for the added separator or group marker
   */
  private void addSeparatorOrGroupMarker(MenuManager menu, String groupId) 
  {
    String prefId = "useSeparator." + menu.getId() + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
    boolean addExtraSeparators = TraderPlugin.getDefault().getPreferenceStore().getBoolean(prefId);
    if (addExtraSeparators)
      menu.add(new Separator(groupId));
    else
      menu.add(new GroupMarker(groupId));
  }
}
