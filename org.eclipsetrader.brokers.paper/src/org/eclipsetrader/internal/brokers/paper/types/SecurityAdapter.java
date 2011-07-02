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

package org.eclipsetrader.internal.brokers.paper.types;

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.internal.brokers.paper.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SecurityAdapter extends XmlAdapter<String, ISecurity> {

    public class FailsafeSecurity implements ISecurity, IStoreObject, IStore {

        private URI uri;

        public FailsafeSecurity(URI uri) {
            this.uri = uri;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getIdentifier()
         */
        @Override
        public IFeedIdentifier getIdentifier() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getName()
         */
        @Override
        public String getName() {
            return uri.toString();
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getProperties()
         */
        @Override
        public IUserProperties getProperties() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(getClass())) {
                return this;
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
         */
        @Override
        public IStore getStore() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
         */
        @Override
        public IStoreProperties getStoreProperties() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
         */
        @Override
        public void setStore(IStore store) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
         */
        @Override
        public void setStoreProperties(IStoreProperties storeProperties) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void delete(IProgressMonitor monitor) throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStore[] fetchChilds(IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#createChild()
         */
        @Override
        public IStore createChild() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#getRepository()
         */
        @Override
        public IRepository getRepository() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        @Override
        public URI toURI() {
            return uri;
        }
    }

    public SecurityAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(ISecurity v) throws Exception {
        if (v == null) {
            return null;
        }
        IStoreObject storeObject = (IStoreObject) v.getAdapter(IStoreObject.class);
        return storeObject.getStore().toURI().toString();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ISecurity unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        ISecurity security = null;

        URI uri = new URI(v);
        if (Activator.getDefault() != null) {
            try {
                BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                if (serviceReference != null) {
                    IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
                    security = repositoryService.getSecurityFromURI(uri);
                    context.ungetService(serviceReference);
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
                Activator.getDefault().getLog().log(status);
            }
        }

        if (security == null) {
            if (Activator.getDefault() != null) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load security " + uri.toString(), null);
                Activator.getDefault().getLog().log(status);
            }
            return new FailsafeSecurity(uri);
        }

        return security;
    }
}
