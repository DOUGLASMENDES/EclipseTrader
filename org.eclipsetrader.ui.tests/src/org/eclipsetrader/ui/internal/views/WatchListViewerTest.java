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

package org.eclipsetrader.ui.internal.views;

import java.util.UUID;

import junit.framework.TestCase;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class WatchListViewerTest extends TestCase {
	private Shell shell;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		Display display = Display.getCurrent();
		shell = new Shell(display);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
	}

	public void testOpenWithoutView() throws Exception {
		TestWatchListViewer part = new TestWatchListViewer();
		part.createPartControl(shell);
		assertNull(part.getViewer());
    }

	private class TestWatchListViewer extends WatchListViewer implements IViewSite {
		private String secondaryId;
		private Composite parent;

		public TestWatchListViewer() {
	        this.secondaryId = UUID.randomUUID().toString();
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.ui.internal.views.WatchListViewer#createPartControl(org.eclipse.swt.widgets.Composite)
         */
        @Override
        public void createPartControl(Composite parent) {
	        super.createPartControl(parent);
	        this.parent = parent;
        }

		public Composite getParent() {
        	return parent;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.part.ViewPart#getViewSite()
         */
        @Override
        public IViewSite getViewSite() {
	        return this;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IViewSite#getActionBars()
         */
        public IActionBars getActionBars() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IViewSite#getSecondaryId()
         */
        public String getSecondaryId() {
	        return this.secondaryId;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IWorkbenchPartSite#getId()
         */
        public String getId() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
         */
        @SuppressWarnings("deprecation")
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
	        return null;
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
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.ui.IWorkbenchSite#getShell()
         */
        public Shell getShell() {
	        return null;
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
}
