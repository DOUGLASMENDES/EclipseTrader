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

package org.eclipsetrader.repository.local.internal.stores;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.SecurityCollection;
import org.eclipsetrader.repository.local.internal.types.FeedIdentifierAdapter;
import org.eclipsetrader.repository.local.internal.types.RepositoryFactoryAdapter;

@XmlRootElement(name = "security")
public class SecurityStore implements IStore {

	@XmlAttribute(name = "id")
	private Integer id;

	@XmlAttribute(name = "factory")
	@XmlJavaTypeAdapter(RepositoryFactoryAdapter.class)
	private IRepositoryElementFactory factory;

	@XmlElement(name = "name")
	private String name;

	@XmlElement(name = "identifier")
	@XmlJavaTypeAdapter(FeedIdentifierAdapter.class)
	private IFeedIdentifier identifier;

	private IUserProperties userProperties;

	private HistoryStore historyStore;

	public SecurityStore() {
	}

	public SecurityStore(Integer id) {
	    this.id = id;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    public URI toURI() {
	    try {
	        return new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_SECURITY_PART, String.valueOf(id));
        } catch (URISyntaxException e) {
        }
        return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
    	StoreProperties properties = new StoreProperties();

    	properties.setProperty(IPropertyConstants.ELEMENT_FACTORY, factory);
    	properties.setProperty(IPropertyConstants.OBJECT_TYPE, ISecurity.class.getName());

		properties.setProperty(IPropertyConstants.NAME, name);
		properties.setProperty(IPropertyConstants.IDENTIFIER, identifier);
		properties.setProperty(IPropertyConstants.USER_PROPERTIES, userProperties);

		return properties;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
		this.factory = (IRepositoryElementFactory) properties.getProperty(IPropertyConstants.ELEMENT_FACTORY);

		this.name = (String) properties.getProperty(IPropertyConstants.NAME);
		this.identifier = (IFeedIdentifier) properties.getProperty(IPropertyConstants.IDENTIFIER);
		this.userProperties = (IUserProperties) properties.getProperty(IPropertyConstants.USER_PROPERTIES);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(IProgressMonitor monitor) throws CoreException {
    	SecurityCollection.getInstance().delete(this);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStore[] fetchChilds(IProgressMonitor monitor) {
    	if (historyStore == null) {
        	IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
    		File file = path.append(String.valueOf(id) + ".xml").toFile();
    		if (file.exists())
				historyStore = new HistoryStore(id);
    	}

    	List<IStore> l = new ArrayList<IStore>();
    	if (historyStore != null)
    		l.add(historyStore);
		return l.toArray(new IStore[l.size()]);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    public IStore createChild() {
	    return new RepositoryStore();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
	@XmlTransient
    public IRepository getRepository() {
	    return Activator.getDefault().getRepository();
    }

	public HistoryStore createHistoryStore() {
		historyStore = new HistoryStore(id);
    	return historyStore;
    }

	public IStore createHistoryStore(Date date) {
    	return new AggregateHistoryStore(id, null);
    }
}
