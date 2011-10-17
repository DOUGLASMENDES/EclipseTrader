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

package org.eclipsetrader.core.internal.trading;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.trading.IAlert;

public class AlertTypeAdapter extends XmlAdapter<String, IAlert> {

    public AlertTypeAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IAlert v) throws Exception {
        if (v == null) {
            return null;
        }
        return v.getId();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IAlert unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        IConfigurationElement[] configElements = getExtensionConfigurationElements();
        for (int j = 0; j < configElements.length; j++) {
            if (v.equals(configElements[j].getAttribute("id"))) { //$NON-NLS-1$
                try {
                    IAlert alert = (IAlert) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
                    return alert;
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Unable to create alert with id " + v, e);
                    CoreActivator.log(status);
                }
            }
        }

        return null;
    }

    IConfigurationElement[] getExtensionConfigurationElements() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CoreActivator.ALERTS_EXTENSION_ID);
        if (extensionPoint == null) {
            return new IConfigurationElement[0];
        }

        return extensionPoint.getConfigurationElements();
    }
}
