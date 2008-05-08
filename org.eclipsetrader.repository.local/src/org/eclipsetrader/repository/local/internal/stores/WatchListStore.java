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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.WatchListCollection;
import org.eclipsetrader.repository.local.internal.types.ColumnAdapter;
import org.eclipsetrader.repository.local.internal.types.HoldingAdapter;
import org.eclipsetrader.repository.local.internal.types.RepositoryFactoryAdapter;

@XmlRootElement(name = "watchlist")
public class WatchListStore implements IStore {

	@XmlAttribute(name = "id")
	private Integer id;

	@XmlAttribute(name = "factory")
	@XmlJavaTypeAdapter(RepositoryFactoryAdapter.class)
	private IRepositoryElementFactory factory;

	@XmlElement(name = "name")
	private String name;

	@XmlElementWrapper(name = "columns")
	@XmlElementRef
	@XmlJavaTypeAdapter(ColumnAdapter.class)
	private List<IColumn> columns = new ArrayList<IColumn>();

	@XmlElementWrapper(name = "elements")
	@XmlElementRef
	@XmlJavaTypeAdapter(HoldingAdapter.class)
	private List<IHolding> elements = new ArrayList<IHolding>();

	public WatchListStore() {
	}

	public WatchListStore(Integer id) {
	    this.id = id;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    public URI toURI() {
	    try {
	        return new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_WATCHLIST_PART, String.valueOf(id));
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
    	properties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());

		properties.setProperty(IPropertyConstants.NAME, name);
		properties.setProperty(IPropertyConstants.COLUMNS, columns.toArray(new IColumn[columns.size()]));
		properties.setProperty(IPropertyConstants.HOLDINGS, elements.toArray(new IHolding[elements.size()]));

		return properties;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
		this.factory = (IRepositoryElementFactory) properties.getProperty(IPropertyConstants.ELEMENT_FACTORY);

		this.name = (String) properties.getProperty(IPropertyConstants.NAME);

		IHolding[] e = (IHolding[]) properties.getProperty(IPropertyConstants.HOLDINGS);
		this.elements = e != null ? new ArrayList<IHolding>(Arrays.asList(e)) : new ArrayList<IHolding>();

		IColumn[] c = (IColumn[]) properties.getProperty(IPropertyConstants.COLUMNS);
		this.columns = c != null ? new ArrayList<IColumn>(Arrays.asList(c)) : new ArrayList<IColumn>();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(IProgressMonitor monitor) throws CoreException {
    	WatchListCollection.getInstance().delete(this);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStore[] fetchChilds(IProgressMonitor monitor) {
	    return new IStore[0];
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
	@XmlTransient
    public IRepository getRepository() {
	    return Activator.getDefault().getRepository();
    }
}
