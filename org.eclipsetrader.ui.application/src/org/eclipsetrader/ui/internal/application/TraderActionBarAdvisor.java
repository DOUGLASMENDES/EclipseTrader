/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.application;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardDropDownAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipsetrader.ui.internal.actions.NewWizardMenu;
import org.eclipsetrader.ui.internal.actions.OpenAction;
import org.eclipsetrader.ui.internal.actions.QuickMenuAction;
import org.eclipsetrader.ui.internal.actions.SettingsAction;

public class TraderActionBarAdvisor extends ActionBarAdvisor {
	private final IWorkbenchWindow window;

	private IWorkbenchAction newWizardAction;
	private NewWizardMenu newWizardMenu;
	private QuickMenuAction newQuickMenu;
	private IWorkbenchAction newWizardDropDownAction;

	private IWorkbenchAction openAction;

	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAsAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction printAction;

	private IWorkbenchAction importResourcesAction;
    private IWorkbenchAction exportResourcesAction;

    private IWorkbenchAction propertiesAction;
	private IWorkbenchAction quitAction;

	private IWorkbenchAction cutAction;
    private IWorkbenchAction copyAction;
    private IWorkbenchAction pasteAction;
    private IWorkbenchAction deleteAction;
    private IWorkbenchAction settingsAction;

    private IWorkbenchAction newWindowAction;

    private IWorkbenchAction editActionSetsAction;
    private IWorkbenchAction savePerspectiveAction;
    private IWorkbenchAction resetPerspectiveAction;
    private IWorkbenchAction closePerspAction;
    private IWorkbenchAction closeAllPerspsAction;

	private IWorkbenchAction helpContentsAction;
    private IWorkbenchAction helpSearchAction;
    private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction openPreferencesAction;

	public TraderActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    protected void makeActions(final IWorkbenchWindow window) {
        newWizardAction = ActionFactory.NEW.create(window);
        register(newWizardAction);

        newWizardDropDownAction = NEW_WIZARD_DROP_DOWN.create(window);
		register(newWizardDropDownAction);

		openAction = new OpenAction(window);
		register(openAction);

		saveAction = ActionFactory.SAVE.create(window);
		saveAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/etool16/save.png"));
		saveAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dtool16/save.png"));
		register(saveAction);

		saveAsAction = ActionFactory.SAVE_AS.create(window);
		saveAsAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/etool16/save_as.png"));
		saveAsAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dtool16/save_as.png"));
		register(saveAsAction);

		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		saveAllAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/etool16/save_all.png"));
		saveAllAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dtool16/save_all.png"));
		register(saveAllAction);

		printAction = ActionFactory.PRINT.create(window);
		printAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/etool16/print.png"));
		printAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dtool16/print.png"));
		register(printAction);

        importResourcesAction = ActionFactory.IMPORT.create(window);
		register(importResourcesAction);

		exportResourcesAction = ActionFactory.EXPORT.create(window);
		register(exportResourcesAction);

		propertiesAction = ActionFactory.PROPERTIES.create(window);
		register(propertiesAction);

		quitAction = ActionFactory.QUIT.create(window);
		register(quitAction);

        cutAction = ActionFactory.CUT.create(window);
		register(cutAction);
        copyAction = ActionFactory.COPY.create(window);
		register(copyAction);
        pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);
        deleteAction = ActionFactory.DELETE.create(window);
		register(deleteAction);

		settingsAction = new SettingsAction(window);
		register(settingsAction);

		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(getWindow());
        newWindowAction.setText("&New Window");
        register(newWindowAction);

		editActionSetsAction = ActionFactory.EDIT_ACTION_SETS.create(window);
		register(editActionSetsAction);
	    savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
	    register(savePerspectiveAction);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
        register(resetPerspectiveAction);
	    closePerspAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
	    register(closePerspAction);
	    closeAllPerspsAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(window);
        register(closeAllPerspsAction);

		openPreferencesAction = ActionFactory.PREFERENCES.create(window);
		register(openPreferencesAction);

		helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpContentsAction);

        helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
        register(helpSearchAction);

        dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
        register(dynamicHelpAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface.action.ICoolBarManager)
	 */
	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_FILE));
		{
            IToolBarManager fileToolBar = new ToolBarManager(SWT.FLAT);
            fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
            fileToolBar.add(newWizardDropDownAction);
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
            fileToolBar.add(saveAction);
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
            fileToolBar.add(printAction);
            fileToolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            // Add to the cool bar manager
			coolBar.add(new ToolBarContributionItem(fileToolBar, IWorkbenchActionConstants.TOOLBAR_HELP));
		}

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new GroupMarker("group.nav"));
		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));
        { // Help group
            IToolBarManager helpToolBar = new ToolBarManager(SWT.FLAT);
            helpToolBar.add(new Separator(IWorkbenchActionConstants.GROUP_HELP));
            // Add the group for applications to contribute
            helpToolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
            // Add to the cool bar manager
			coolBar.add(new ToolBarContributionItem(helpToolBar, IWorkbenchActionConstants.TOOLBAR_HELP));
        }
	}

	@Override
    protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu(menuBar));
		menuBar.add(createEditMenu(menuBar));
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(createWindowMenu(menuBar));
		menuBar.add(createHelpMenu(menuBar));
	}

	private MenuManager createFileMenu(IMenuManager menuBar) {
		MenuManager menu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		{
	        String newId = ActionFactory.NEW.getId();
	        MenuManager newMenu = new MenuManager("&New", newId) {
	            @Override
                public String getMenuText() {
	                String result = super.getMenuText();
	                if (newQuickMenu == null) {
						return result;
					}
	                String shortCut = newQuickMenu.getShortCutString();
	                if (shortCut == null) {
						return result;
					}
	                return result + "\t" + shortCut; //$NON-NLS-1$
	            }
	        };
	        newMenu.add(new Separator(newId));
	        this.newWizardMenu = new NewWizardMenu(getWindow());
	        newMenu.add(this.newWizardMenu);
	        newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	        menu.add(newMenu);
		}

        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator());

		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);
        menu.add(new Separator());
		menu.add(printAction);

        menu.add(new Separator());
        menu.add(importResourcesAction);
        menu.add(exportResourcesAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator(IWorkbenchActionConstants.FILE_END));
		menu.add(propertiesAction);
		menu.add(new Separator());
		menu.add(quitAction);
		return menu;
	}

	private MenuManager createEditMenu(IMenuManager menuBar) {
		MenuManager menu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(new Separator());
        menu.add(deleteAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.EDIT_END));
        menu.add(settingsAction);
		return menu;
	}

	private MenuManager createWindowMenu(IMenuManager menuBar) {
		MenuManager menu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);

		menu.add(newWindowAction);

		menu.add(new Separator());
		addPerspectiveActions(menu);

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// See the comment for quit in createFileMenu
		ActionContributionItem openPreferencesItem = new ActionContributionItem(openPreferencesAction);
		openPreferencesItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		menu.add(openPreferencesItem);

		menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
		return menu;
	}

	private void addPerspectiveActions(MenuManager menu) {
		MenuManager changePerspMenuMgr = new MenuManager("&Open Perspective", "openPerspective");
		IContributionItem changePerspMenuItem = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(getWindow());
		changePerspMenuMgr.add(changePerspMenuItem);
		menu.add(changePerspMenuMgr);

		MenuManager showViewMenuMgr = new MenuManager("Show &View", "showView");
		IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
		showViewMenuMgr.add(showViewMenu);
		menu.add(showViewMenuMgr);

		menu.add(new Separator());
		menu.add(editActionSetsAction);
        menu.add(savePerspectiveAction);
        menu.add(resetPerspectiveAction);
        menu.add(closePerspAction);
        menu.add(closeAllPerspsAction);
	}

	private MenuManager createHelpMenu(IMenuManager menuBar) {
		MenuManager menu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		menu.add(new GroupMarker("group.intro")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
		menu.add(new Separator("group.main")); //$NON-NLS-1$
		menu.add(helpContentsAction);
        menu.add(helpSearchAction);
		menu.add(dynamicHelpAction);
		menu.add(new Separator("group.assist")); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.tutorials")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.tools")); //$NON-NLS-1$
		menu.add(new GroupMarker("group.updates")); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom
		menu.add(new Separator("group.about"));

		ActionContributionItem aboutItem = new ActionContributionItem(aboutAction);
		aboutItem.setVisible(!"carbon".equals(SWT.getPlatform()));
        menu.add(aboutItem);
		menu.add(new GroupMarker("group.about.ext"));
		return menu;
	}

	private IWorkbenchWindow getWindow() {
		return window;
	}

	public static final ActionFactory NEW_WIZARD_DROP_DOWN = new ActionFactory("newWizardDropDown") { //$NON-NLS-1$
		@Override
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}

			IWorkbenchAction innerAction = ActionFactory.NEW.create(window);
			innerAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/etool16/new_wizard.png"));
			innerAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dtool16/new_wizard.png"));

			NewWizardMenu newWizardMenu = new NewWizardMenu(window);

			IWorkbenchAction action = new NewWizardDropDownAction(window, innerAction, newWizardMenu);
			action.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/etool16/new_wizard.png"));
	        action.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dtool16/new_wizard.png"));
			action.setId(getId());
			return action;
		}
	};
}
