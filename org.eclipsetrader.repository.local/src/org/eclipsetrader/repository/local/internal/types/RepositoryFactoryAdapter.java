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

package org.eclipsetrader.repository.local.internal.types;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.repository.local.internal.Activator;

public class RepositoryFactoryAdapter extends XmlAdapter<String, IRepositoryElementFactory> {

    private static final String ELEMENT_FACTORY_ID = "org.eclipsetrader.core.elementFactories";

    public RepositoryFactoryAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IRepositoryElementFactory v) throws Exception {
        return v != null ? v.getId() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IRepositoryElementFactory unmarshal(String v) throws Exception {
        return v != null ? getFactory(v) : null;
    }

    protected IRepositoryElementFactory getFactory(String id) {
        IConfigurationElement[] configElements = getConfigurationElements();
        if (configElements == null) {
            return null;
        }

        for (int j = 0; j < configElements.length; j++) {
            String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
            if (id.equals(strID)) {
                try {
                    IRepositoryElementFactory factory = (IRepositoryElementFactory) configElements[j].createExecutableExtension("class");
                    return factory;
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Unable to create factory with id " + id, e);
                    Activator.getDefault().getLog().log(status);
                }
                break;
            }
        }

        return null;
    }

    protected IConfigurationElement[] getConfigurationElements() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ELEMENT_FACTORY_ID);
        if (extensionPoint == null) {
            return null;
        }
        return extensionPoint.getConfigurationElements();
    }
}
