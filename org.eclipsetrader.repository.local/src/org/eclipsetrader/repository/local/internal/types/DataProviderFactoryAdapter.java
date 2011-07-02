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
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.repository.local.internal.Activator;

public class DataProviderFactoryAdapter extends XmlAdapter<String, IDataProviderFactory> {

    public static final String PROVIDERS_FACTORY_ID = "org.eclipsetrader.core.providers";

    public class FailsafeDataProviderFactory implements IDataProviderFactory {

        private String id;

        public FailsafeDataProviderFactory(String id) {
            this.id = id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
         */
        @Override
        public IDataProvider createProvider() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getId()
         */
        @Override
        public String getId() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getName()
         */
        @Override
        public String getName() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Class[] getType() {
            return new Class[0];
        }
    }

    public DataProviderFactoryAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IDataProviderFactory v) throws Exception {
        return v != null ? v.getId() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IDataProviderFactory unmarshal(String v) throws Exception {
        return v != null ? getFactory(v) : null;
    }

    protected IDataProviderFactory getFactory(String id) {
        IConfigurationElement[] configElements = getConfigurationElements();
        if (configElements == null) {
            return null;
        }

        for (int j = 0; j < configElements.length; j++) {
            String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
            if (id.equals(strID)) {
                try {
                    IDataProviderFactory factory = (IDataProviderFactory) configElements[j].createExecutableExtension("class");
                    return factory;
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Unable to create data provider factory with id " + id, e);
                    Activator.getDefault().getLog().log(status);
                }
                break;
            }
        }

        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load provider " + id, null);
        Activator.getDefault().getLog().log(status);

        return new FailsafeDataProviderFactory(id);
    }

    protected IConfigurationElement[] getConfigurationElements() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_FACTORY_ID);
        if (extensionPoint == null) {
            return null;
        }
        return extensionPoint.getConfigurationElements();
    }
}
