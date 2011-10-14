/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipsetrader.core.ILauncher;

public class StopFeedAction implements IWorkbenchWindowActionDelegate, IWorkbenchWindowPulldownDelegate2 {

    public static final String LAUNCHERS_EXTENSION_ID = "org.eclipsetrader.core.launchers";

    private Menu menubarMenu;
    private Menu toolbarMenu;

    public StopFeedAction() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
        if (menubarMenu != null) {
            menubarMenu.dispose();
            menubarMenu = null;
        }
        if (toolbarMenu != null) {
            toolbarMenu.dispose();
            toolbarMenu = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
     */
    @Override
    public Menu getMenu(Menu parent) {
        if (menubarMenu != null) {
            menubarMenu.dispose();
        }
        menubarMenu = new Menu(parent);
        initMenu(menubarMenu);
        return menubarMenu;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
     */
    @Override
    public Menu getMenu(Control parent) {
        if (toolbarMenu != null) {
            toolbarMenu.dispose();
        }
        toolbarMenu = new Menu(parent);
        initMenu(toolbarMenu);
        return toolbarMenu;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        Job job = new Job("Feed Shutdown") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
                try {
                    boolean startAll = UIActivator.getDefault().getPreferenceStore().getBoolean("RUN_ALL_LAUNCHERS");
                    Set<String> set = new HashSet<String>(Arrays.asList(UIActivator.getDefault().getPreferenceStore().getString("RUN_LAUNCHERS").split(";")));

                    ILauncher[] launchers = getLaunchers();
                    for (int i = 0; i < launchers.length; i++) {
                        if (startAll || set.contains(launchers[i].getId())) {
                            try {
                                launchers[i].terminate(monitor);
                            } catch (Exception e) {
                                Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error terminating " + launchers[i].getId(), e);
                                UIActivator.getDefault().getLog().log(status);
                            }
                        }
                    }
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * Creates the menu for the action
     */
    private void initMenu(Menu menu) {
        menu.addMenuListener(new MenuAdapter() {

            @Override
            public void menuShown(MenuEvent e) {
                Menu m = (Menu) e.widget;
                MenuItem[] items = m.getItems();
                for (int i = 0; i < items.length; i++) {
                    items[i].dispose();
                }
                fillMenu(m);
            }
        });
    }

    protected void fillMenu(Menu menu) {
        ActionContributionItem item = new ActionContributionItem(new StopAllAction());
        item.fill(menu, -1);

        new MenuItem(menu, SWT.SEPARATOR);

        ILauncher[] launchers = getLaunchers();
        for (int i = 0; i < launchers.length; i++) {
            item = new ActionContributionItem(new LauncherTerminateAction(launchers[i]));
            item.fill(menu, -1);
        }
    }

    ILauncher[] getLaunchers() {
        List<ILauncher> list = new ArrayList<ILauncher>();

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCHERS_EXTENSION_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();

            for (int j = 0; j < configElements.length; j++) {
                String id = configElements[j].getAttribute("id"); //$NON-NLS-1$
                try {
                    ILauncher launcher = (ILauncher) configElements[j].createExecutableExtension("class");
                    if (launcher != null) {
                        list.add(launcher);
                    }
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error launching " + id, e);
                    UIActivator.getDefault().getLog().log(status);
                }
            }
        }

        Collections.sort(list, new Comparator<ILauncher>() {

            @Override
            public int compare(ILauncher o1, ILauncher o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return list.toArray(new ILauncher[list.size()]);
    }
}
