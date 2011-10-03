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

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.repository.local.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ScriptAdapter extends XmlAdapter<String, IScript> {

    private static IRepositoryService repositoryService;

    public class FailsafeScript implements IScript, IStoreObject, IStore {

        private URI uri;

        public FailsafeScript(URI uri) {
            this.uri = uri;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void delete(IProgressMonitor monitor) throws CoreException {
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
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
         */
        @Override
        public IStore getStore() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
         */
        @Override
        public void setStore(IStore store) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
         */
        @Override
        public IStoreProperties getStoreProperties() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
         */
        @Override
        public void setStoreProperties(IStoreProperties storeProperties) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.IScript#getName()
         */
        @Override
        public String getName() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.IScript#getLanguage()
         */
        @Override
        public String getLanguage() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.IScript#getText()
         */
        @Override
        public String getText() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        @Override
        public URI toURI() {
            return uri;
        }
    }

    public ScriptAdapter() {
    }

    public static void setRepositoryService(IRepositoryService service) {
        ScriptAdapter.repositoryService = service;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IScript v) throws Exception {
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
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public IScript unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        URI uri = new URI(v);
        if (repositoryService == null && Activator.getDefault() != null) {
            try {
                BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                repositoryService = (IRepositoryService) context.getService(serviceReference);
                context.ungetService(serviceReference);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
                Activator.getDefault().getLog().log(status);
            }
        }

        Object result = repositoryService != null ? repositoryService.getObjectFromURI(uri) : null;
        if (!(result instanceof IScript)) {
            if (Activator.getDefault() != null) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load script " + uri.toString(), null);
                Activator.getDefault().getLog().log(status);
            }
            return new FailsafeScript(uri);
        }

        return (IScript) result;
    }
}
