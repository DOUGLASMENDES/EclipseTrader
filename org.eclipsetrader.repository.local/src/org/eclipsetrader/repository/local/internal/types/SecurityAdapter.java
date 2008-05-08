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

package org.eclipsetrader.repository.local.internal.types;

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.repository.local.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SecurityAdapter extends XmlAdapter<String, ISecurity> {
	private static IRepositoryService repositoryService;

	public class FailsafeSecurity implements ISecurity, IStoreObject, IStore {
		private URI uri;

		public FailsafeSecurity(URI uri) {
	        this.uri = uri;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getIdentifier()
         */
        public IFeedIdentifier getIdentifier() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getName()
         */
        public String getName() {
	        return uri.toString();
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getProperties()
         */
        public IUserProperties getProperties() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
        	if (adapter.isAssignableFrom(getClass()))
        		return this;
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
         */
        public IStore getStore() {
	        return this;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
         */
        public IStoreProperties getStoreProperties() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
         */
        public void setStore(IStore store) {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
         */
        public void setStoreProperties(IStoreProperties storeProperties) {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        public void delete(IProgressMonitor monitor) throws CoreException {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStore[] fetchChilds(IProgressMonitor monitor) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#createChild()
         */
        public IStore createChild() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#getRepository()
         */
        public IRepository getRepository() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        public URI toURI() {
	        return uri;
        }
	}

	public SecurityAdapter() {
	}

	public static void setRepositoryService(IRepositoryService service) {
    	SecurityAdapter.repositoryService = service;
    }

	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(ISecurity v) throws Exception {
    	if (v == null)
    		return null;
    	IStoreObject storeObject = (IStoreObject) v.getAdapter(IStoreObject.class);
	    return storeObject.getStore().toURI().toString();
    }

	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ISecurity unmarshal(String v) throws Exception {
    	if (v == null)
    		return null;

		URI uri = new URI(v);
		if (repositoryService == null) {
	    	try {
	    		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
	    		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
	    		repositoryService = (IRepositoryService) context.getService(serviceReference);
	    		context.ungetService(serviceReference);
	    	} catch(Exception e) {
	    		Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
	    		Activator.getDefault().getLog().log(status);
	    	}
		}

		ISecurity security = repositoryService.getSecurityFromURI(uri);
		if (security == null) {
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Failed to load security " + uri.toString(), null);
    		Activator.getDefault().getLog().log(status);
			return new FailsafeSecurity(uri);
		}

		return security;
    }
}
