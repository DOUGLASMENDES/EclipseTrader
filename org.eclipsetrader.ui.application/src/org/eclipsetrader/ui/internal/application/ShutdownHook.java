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

package org.eclipsetrader.ui.internal.application;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ShutdownHook extends Thread {

    public ShutdownHook() {
    }

    /**
     * @param target
     */
    public ShutdownHook(Runnable target) {
        super(target);
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final Display display = PlatformUI.getWorkbench().getDisplay();
            if (workbench != null && !workbench.isClosing()) {
                display.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
                        for (int i = 0; i < workbenchWindows.length; i++) {
                            IWorkbenchWindow workbenchWindow = workbenchWindows[i];
                            if (workbenchWindow == null) {
                                // SIGTERM shutdown code must access
                                // workbench using UI thread!!
                            }
                            else {
                                IWorkbenchPage[] pages = workbenchWindow.getPages();
                                for (int j = 0; j < pages.length; j++) {
                                    IEditorPart[] dirtyEditors = pages[j].getDirtyEditors();
                                    for (int k = 0; k < dirtyEditors.length; k++) {
                                        dirtyEditors[k].doSave(new NullProgressMonitor());
                                    }
                                }
                            }
                        }
                    }
                });
                display.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        workbench.close();
                    }
                });
            }
        } catch (IllegalStateException e) {
            // ignore
        }
    }
}
