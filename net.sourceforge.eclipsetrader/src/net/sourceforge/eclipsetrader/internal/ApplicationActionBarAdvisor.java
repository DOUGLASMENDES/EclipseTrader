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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{

  public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
  {
    super(configurer);
  }

  
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
   */
  protected void makeActions(IWorkbenchWindow window)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
   */
  protected void fillMenuBar(IMenuManager menuBar)
  {
    IWorkbenchWindow window = getActionBarConfigurer().getWindowConfigurer().getWindow(); 

    menuBar.add(createFileMenu(window));
    menuBar.add(new GroupMarker("group1")); //$NON-NLS-1$
    menuBar.add(new GroupMarker("group2")); //$NON-NLS-1$
    menuBar.add(new GroupMarker("group3")); //$NON-NLS-1$
    menuBar.add(new GroupMarker("group4")); //$NON-NLS-1$
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menuBar.add(createWindowMenu(window));
    menuBar.add(createHelpMenu(window));
  }

  
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillActionBars(int)
   */
  public void fillActionBars(int flags)
  {
    super.fillActionBars(flags);
    ICoolBarManager coolBar = getActionBarConfigurer().getCoolBarManager();
    coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
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
}
