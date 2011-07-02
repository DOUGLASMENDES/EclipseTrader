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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class TraderApplication implements IApplication {

    /* (non-Javadoc)
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    @Override
    public Object start(IApplicationContext context) throws Exception {
        Display display = PlatformUI.createDisplay();
        try {
            Shell shell = new Shell(display, SWT.ON_TOP);
            try {
                if (!checkInstanceLocation(shell)) {
                    Platform.endSplash();
                    return EXIT_OK;
                }
            } finally {
                if (shell != null) {
                    shell.dispose();
                }
            }

            int returnCode = PlatformUI.createAndRunWorkbench(display, new TraderWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IApplication.EXIT_RESTART;
            }
            else {
                return IApplication.EXIT_OK;
            }
        } finally {
            display.dispose();
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    @Override
    public void stop() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }

    /**
     * Return true if a valid workspace path has been set and false otherwise.
     *
     * @return true if a valid instance location has been set and false otherwise
     */
    private boolean checkInstanceLocation(Shell shell) {
        Location instanceLoc = Platform.getInstanceLocation();

        // -data @none was specified but workspace is required
        if (instanceLoc == null) {
            MessageDialog.openError(shell, "Workspace is Mandatory", "Platform need a valid workspace. Restart without the @none option.");
            return false;
        }

        // -data "/valid/path", workspace already set
        if (instanceLoc.isSet()) {

            // at this point its valid, so try to lock it and update the
            // metadata version information if successful
            try {
                if (instanceLoc.lock()) {
                    return true;
                }

                // we failed to create the directory.
                // Two possibilities:
                // 1. directory is already in use
                // 2. directory could not be created
                File workspaceDirectory = new File(instanceLoc.getURL().getFile());
                if (workspaceDirectory.exists()) {
                    MessageDialog.openError(shell, "Workspace Cannot Be Locked", "Could not launch the product because the associated workspace is currently in use by another Eclipse application.");
                }
                else {
                    MessageDialog.openError(shell, "Workspace Cannot Be Created", "Could not launch the product because the specified workspace cannot be created. The specified workspace directory is either invalid or read-only.");
                }
            } catch (IOException e) {
                Activator.log("Could not obtain lock for workspace location", e);
                MessageDialog.openError(shell, "Internal Error", e.getMessage());
            }
        }

        // create the workspace if it does not already exist
        File workspace = new File(System.getProperty("user.dir") + File.separator + "workspace");
        if (!workspace.exists()) {
            workspace.mkdir();
        }

        try {
            // Don't use File.toURL() since it adds a leading slash that Platform does not
            // handle properly.  See bug 54081 for more details.
            String path = workspace.getAbsolutePath().replace(File.separatorChar, '/');
            instanceLoc.setURL(new URL("file", null, path), true);
            return true;
        } catch (MalformedURLException e) {
            Activator.log("Selected workspace is not valid", e);
            MessageDialog.openError(shell, "Invalid Workspace", "Selected workspace is not valid; choose a different one.");
        }

        return false;
    }
}
