/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */
package net.sourceforge.eclipsetrader;

import org.eclipse.jface.action.GroupMarker;
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
    private IWorkbenchAction quitAction; 
    private IWorkbenchAction helpContentsAction; 
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction editActionSetsAction;
    private IWorkbenchAction resetPerspectiveAction;
    private IWorkbenchAction preferencesAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
    {
        super(configurer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
     */
    protected void makeActions(IWorkbenchWindow window)
    {
        quitAction = ActionFactory.QUIT.create(window);
        editActionSetsAction = ActionFactory.EDIT_ACTION_SETS.create(window);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
        preferencesAction = ActionFactory.PREFERENCES.create(window);
        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        aboutAction = ActionFactory.ABOUT.create(window);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
     */
    protected void fillMenuBar(IMenuManager menuBar)
    {
        MenuManager menu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
        menu.add(new Separator(IWorkbenchActionConstants.FILE_START));
        MenuManager fileNewMenu = new MenuManager("New", "new");
        fileNewMenu.add(new GroupMarker("top"));
        fileNewMenu.add(new GroupMarker("additions"));
        fileNewMenu.add(new GroupMarker("bottom"));
        menu.add(fileNewMenu);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator(IWorkbenchActionConstants.FILE_END));
        menu.add(quitAction);
        menuBar.add(menu);

        menuBar.add(new GroupMarker("begin")); //$NON-NLS-1$
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(new GroupMarker("end")); //$NON-NLS-1$

        menu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$
        menu.add(new Separator("top")); //$NON-NLS-1$
        menu.add(new Separator("open")); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator());
        editActionSetsAction.setEnabled(true);
        menu.add(editActionSetsAction);
        resetPerspectiveAction.setEnabled(true);
        menu.add(resetPerspectiveAction);
        menu.add(new Separator());
        menu.add(preferencesAction);
        menu.add(new Separator("bottom")); //$NON-NLS-1$
        menuBar.add(menu);

        menu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
        // Welcome or intro page would go here
        menu.add(helpContentsAction);
        // Tips and tricks page would go here
        menu.add(new Separator(IWorkbenchActionConstants.HELP_START));
        menu.add(new Separator("group.main.ext")); //$NON-NLS-1$
        menu.add(new Separator("group.tutorials")); //$NON-NLS-1$
        menu.add(new Separator("group.tools")); //$NON-NLS-1$
        menu.add(new Separator("group.updates")); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.HELP_END)); 
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        // About should always be at the bottom
        // To use the real RCP About dialog uncomment these lines
        menu.add(new Separator("group.about")); //$NON-NLS-1$ 
        menu.add(aboutAction);
        menu.add(new Separator("group.about.ext")); //$NON-NLS-1$ 
        menuBar.add(menu);
    }
}