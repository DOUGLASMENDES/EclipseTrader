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

package org.eclipsetrader.ui.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.IServiceLocator;

public class TestWorkbenchPartSite implements IWorkbenchPartSite, IViewSite {
	private Shell shell;
	private String id;
	private String secondaryId;
	private String pluginId;
	private ISelectionProvider selectionProvider;

	private IActionBars actionBars = new IActionBars() {

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#clearGlobalActionHandlers()
         */
        public void clearGlobalActionHandlers() {
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getGlobalActionHandler(java.lang.String)
         */
        public IAction getGlobalActionHandler(String actionId) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getMenuManager()
         */
        public IMenuManager getMenuManager() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getServiceLocator()
         */
        public IServiceLocator getServiceLocator() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getStatusLineManager()
         */
        public IStatusLineManager getStatusLineManager() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getToolBarManager()
         */
        public IToolBarManager getToolBarManager() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#setGlobalActionHandler(java.lang.String, org.eclipse.jface.action.IAction)
         */
        public void setGlobalActionHandler(String actionId, IAction handler) {
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#updateActionBars()
         */
        public void updateActionBars() {
        }
	};

	public TestWorkbenchPartSite(Shell shell) {
		this.shell = shell;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.IViewSite#getSecondaryId()
     */
    public String getSecondaryId() {
	    return secondaryId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewSite#getActionBars()
     */
    public IActionBars getActionBars() {
	    return actionBars;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
	 */
	public IKeyBindingService getKeyBindingService() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
	 */
	public IWorkbenchPart getPart() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
	 */
	public String getPluginId() {
		return pluginId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
	 */
	public String getRegisteredName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String, org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getPage()
	 */
	public IWorkbenchPage getPage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getShell()
	 */
	public Shell getShell() {
		return shell;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		selectionProvider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getService(Class api) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public boolean hasService(Class api) {
		return false;
	}
}
