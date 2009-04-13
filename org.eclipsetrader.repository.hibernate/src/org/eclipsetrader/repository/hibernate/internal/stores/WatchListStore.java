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

package org.eclipsetrader.repository.hibernate.internal.stores;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.types.RepositoryFactoryType;
import org.eclipsetrader.repository.hibernate.internal.types.WatchListColumn;
import org.eclipsetrader.repository.hibernate.internal.types.WatchListHolding;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "watchlists")
public class WatchListStore implements IStore {
	@Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id;

	@Version
	@Column(name = "version")
	@SuppressWarnings("unused")
	private Integer version;

	@Column(name = "factory")
	@Type(type = "string")
	@Target(RepositoryFactoryType.class)
	private IRepositoryElementFactory factory;

	@Column(name = "name")
	private String name;

	@OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL)
	@OrderBy("index")
	private List<WatchListColumn> columns = new ArrayList<WatchListColumn>();

	@OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL)
	@OrderBy("index")
	private List<WatchListHolding> elements = new ArrayList<WatchListHolding>();

	@Transient
	private HibernateRepository repository;

	public WatchListStore() {
	}

	public WatchListStore(HibernateRepository repository) {
	    this.repository = repository;
    }

	public void setRepository(HibernateRepository repository) {
    	this.repository = repository;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    public URI toURI() {
	    try {
	        return new URI(repository.getSchema(), HibernateRepository.URI_WATCHLIST_PART, id);
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
    	Session session = repository.getSession();

    	this.factory = (IRepositoryElementFactory) properties.getProperty(IPropertyConstants.ELEMENT_FACTORY);

		this.name = (String) properties.getProperty(IPropertyConstants.NAME);

		IColumn[] c = (IColumn[]) properties.getProperty(IPropertyConstants.COLUMNS);
		if (c != null) {
	    	for (WatchListColumn column : this.columns)
	    		session.delete(column);
	    	this.columns.clear();

	    	for (int i = 0; i < c.length; i++)
	    		this.columns.add(new WatchListColumn(c[i], this, i));
		}

		IHolding[] e = (IHolding[]) properties.getProperty(IPropertyConstants.HOLDINGS);
		if (e != null) {
			for (WatchListHolding holding : this.elements)
				session.delete(holding);
			this.elements.clear();

			for (int i = 0; i < e.length; i++)
				this.elements.add(new WatchListHolding(e[i], this, i));
		}

		session.save(this);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(IProgressMonitor monitor) throws CoreException {
		Session session = repository.getSession();

		Transaction currentTransaction = session.beginTransaction();
		try {
			session.delete(this);
			currentTransaction.commit();
			currentTransaction = null;
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
			Activator.log(status);
		} finally {
			if (currentTransaction != null) {
				try {
					currentTransaction.rollback();
				} catch(Exception e1) {
					Status status1 = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error rolling back transaction", e1); //$NON-NLS-1$
					Activator.log(status1);
				}
			}
		}

		session.clear();
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
	@Transient
    public IRepository getRepository() {
	    return repository;
    }
}
