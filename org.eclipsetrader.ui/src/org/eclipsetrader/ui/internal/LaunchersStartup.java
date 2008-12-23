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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipsetrader.core.ILauncher;

public class LaunchersStartup implements IStartup {
	public static final String LAUNCHERS_EXTENSION_ID = "org.eclipsetrader.core.launchers";

	public LaunchersStartup() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCHERS_EXTENSION_ID);
		if (extensionPoint != null) {
    		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
        	Set<String> set = new HashSet<String>(Arrays.asList(UIActivator.getDefault().getPreferenceStore().getString("STARTUP_LAUNCHERS").split(";")));

    		for (int j = 0; j < configElements.length; j++) {
    			String id = configElements[j].getAttribute("id"); //$NON-NLS-1$
    			if (set.contains(id)) {
        			try {
        				ILauncher launcher = (ILauncher) configElements[j].createExecutableExtension("class");
        				if (launcher != null)
        					launcher.launch(new NullProgressMonitor());
        			} catch (Exception e) {
        	    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error launching " + id, e);
        	    		UIActivator.getDefault().getLog().log(status);
        			}
    			}
    		}
		}
	}
}
