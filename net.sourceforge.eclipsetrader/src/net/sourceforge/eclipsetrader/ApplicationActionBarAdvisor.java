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

import net.sourceforge.eclipsetrader.internal.CloseAction;
import net.sourceforge.eclipsetrader.internal.CloseAllAction;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
    private IWorkbenchAction cutAction; 
    private IWorkbenchAction copyAction; 
    private IWorkbenchAction pasteAction; 
    private IWorkbenchAction pasteSpecialAction; 
    private IWorkbenchAction deleteAction; 
    private IWorkbenchAction settingsAction; 
    private IWorkbenchAction propertiesAction; 
    private IWorkbenchAction quitAction; 
    private IWorkbenchAction newWindowAction; 
    private IWorkbenchAction helpContentsAction; 
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction editActionSetsAction;
    private IWorkbenchAction resetPerspectiveAction;
    private IWorkbenchAction savePerspectiveAction;
    private IWorkbenchAction preferencesAction;
    private IContributionItem perspectiveList;
    private IContributionItem viewList;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
    {
        super(configurer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
     */
    protected void makeActions(IWorkbenchWindow window)
    {
        propertiesAction = ActionFactory.PROPERTIES.create(window);
        quitAction = ActionFactory.QUIT.create(window);

        cutAction = ActionFactory.CUT.create(window);
        copyAction = ActionFactory.COPY.create(window);
        pasteAction = ActionFactory.PASTE.create(window);
        pasteSpecialAction = new PasteSpecialAction(window);
        deleteAction = ActionFactory.DELETE.create(window);
        settingsAction = new SettingsAction(window);
        preferencesAction = ActionFactory.PREFERENCES.create(window);

        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        editActionSetsAction = ActionFactory.EDIT_ACTION_SETS.create(window);
        savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        aboutAction = ActionFactory.ABOUT.create(window);
        
        perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
        viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
     */
    protected void fillMenuBar(IMenuManager menuBar)
    {
        MenuManager menu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
        menu.add(new Separator(IWorkbenchActionConstants.FILE_START));

        MenuManager wizardMenu = new MenuManager("New", "newWizard");
        wizardMenu.add(new Separator("top"));
        wizardMenu.add(new Separator("plugins"));
        wizardMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        wizardMenu.add(new Separator("bottom"));
        menu.add(wizardMenu);        
        menu.add(new Separator());
        menu.add(new CloseAction());        
        menu.add(new CloseAllAction());        
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator(IWorkbenchActionConstants.FILE_END));
        menu.add(propertiesAction);
        menu.add(new Separator());
        menu.add(quitAction);
        menuBar.add(menu);

        menu = new MenuManager("Edit", IWorkbenchActionConstants.M_EDIT);
        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(pasteSpecialAction);
        menu.add(new Separator());
        menu.add(deleteAction);
        menu.add(new Separator());
        menu.add(settingsAction);
        menu.add(preferencesAction);
        menuBar.add(menu);

        menuBar.add(new GroupMarker("begin")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("charts")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("news")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("group1")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("group2")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("group3")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("group4")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("group5")); //$NON-NLS-1$
        menuBar.add(new GroupMarker("group6")); //$NON-NLS-1$
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(new GroupMarker("end")); //$NON-NLS-1$

        menu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$
        menu.add(new Separator("top")); //$NON-NLS-1$
        menu.add(newWindowAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator());
        MenuManager perspectiveMenu = new MenuManager("Open Perspective", "openPerspective"); //$NON-NLS-1$ //$NON-NLS-2$
        perspectiveMenu.add(perspectiveList);
        menu.add(perspectiveMenu);
        MenuManager viewMenu = new MenuManager("Show View", "showView");
        viewMenu.add(viewList);
        menu.add(viewMenu);        

        menu.add(new Separator());
        menu.add(editActionSetsAction);
        menu.add(savePerspectiveAction);
        menu.add(resetPerspectiveAction);
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

        getActionBarConfigurer().registerGlobalAction(cutAction);
        getActionBarConfigurer().registerGlobalAction(copyAction);
        getActionBarConfigurer().registerGlobalAction(pasteAction);
        getActionBarConfigurer().registerGlobalAction(deleteAction);
    }
}
