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

package org.eclipsetrader.news.tests;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;

public class TestSecurity implements ISecurity, IStoreObject, IStore {
	private String name;
	private IFeedIdentifier identifier;
	private URI uri;
	private IStore store;
	private IStoreProperties storeProperties;

	public TestSecurity() {
	}

	public TestSecurity(String name, IFeedIdentifier identifier, URI uri) {
	    this.name = name;
	    this.identifier = identifier;
	    this.uri = uri;
    }

	public TestSecurity(String name, IFeedIdentifier identifier, String uri) throws URISyntaxException {
		this(name, identifier, new URI(uri));
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.instruments.ISecurity#getIdentifier()
	 */
	public IFeedIdentifier getIdentifier() {
		return identifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.instruments.ISecurity#getName()
	 */
	public String getName() {
		return name;
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
		return store != null ? store : this;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
	 */
	public IStoreProperties getStoreProperties() {
		return storeProperties;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
	 */
	public void setStore(IStore store) {
		this.store = store;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
	 */
	public void setStoreProperties(IStoreProperties storeProperties) {
		this.storeProperties = storeProperties;
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
		return storeProperties;
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
		this.storeProperties = properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#toURI()
	 */
	public URI toURI() {
		return uri;
	}
}
