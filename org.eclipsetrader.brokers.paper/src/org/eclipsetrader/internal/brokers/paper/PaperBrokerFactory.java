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

package org.eclipsetrader.internal.brokers.paper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class PaperBrokerFactory implements IExecutableExtension, IExecutableExtensionFactory {

    private static PaperBroker instance;

    private IConfigurationElement config;
    private String propertyName;
    private Object data;

    public PaperBrokerFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        this.config = config;
        this.propertyName = propertyName;
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    @Override
    public Object create() throws CoreException {
        if (instance == null) {
            instance = new PaperBroker();
            instance.setInitializationData(config, propertyName, data);
            try {
                instance.load(Activator.getDefault().getStateLocation().append("monitors.xml").toFile());
            } catch (Exception e) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error loading monitors", e); //$NON-NLS-1$
                Activator.log(status);
            }
        }
        return instance;
    }

    public void dispose() {
        try {
            if (instance != null) {
                instance.save(Activator.getDefault().getStateLocation().append("monitors.xml").toFile());
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error saving monitors", e); //$NON-NLS-1$
            Activator.log(status);
        }
        instance = null;
    }
}
