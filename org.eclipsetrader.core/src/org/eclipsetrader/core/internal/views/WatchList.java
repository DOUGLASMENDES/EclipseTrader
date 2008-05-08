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

package org.eclipsetrader.core.internal.views;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.IWatchListVisitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Default implementation of the <code>IWatchList</code> interface.
 *
 * @since 1.0
 */
public class WatchList implements IWatchList, IStoreObject {
	private String name;
	private List<IWatchListColumn> columns = new ArrayList<IWatchListColumn>();
	private List<IWatchListElement> items = new ArrayList<IWatchListElement>();

	private IStore store;
	private IStoreProperties storeProperties;

	private List<WatchListView> views = new ArrayList<WatchListView>();
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	protected WatchList() {
	}

	public WatchList(String name, IWatchListColumn[] columns) {
	    this.name = name;
	    setColumns(columns);
    }

	public WatchList(IStore store, IStoreProperties storeProperties) {
	    setStore(store);
	    setStoreProperties(storeProperties);
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#setName(java.lang.String)
     */
    public void setName(String name) {
    	this.name = name;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#setColumns(org.eclipsetrader.core.views.IWatchListColumn[])
     */
    public void setColumns(IWatchListColumn[] columns) {
		IWatchListColumn[] oldValue = this.columns.toArray(new IWatchListColumn[this.columns.size()]);

		this.columns = new ArrayList<IWatchListColumn>(Arrays.asList(columns));
		for (WatchListView view : this.views)
			view.doSetColumns(columns);

		propertyChangeSupport.firePropertyChange(IWatchList.COLUMNS, oldValue, this.columns.toArray(new IWatchListColumn[this.columns.size()]));
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getColumns()
	 */
	public IWatchListColumn[] getColumns() {
		return columns.toArray(new IWatchListColumn[columns.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getColumnCount()
	 */
	public int getColumnCount() {
		return columns.size();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#addItem(org.eclipsetrader.core.views.IWatchListElement)
     */
    public void addItem(IWatchListElement item) {
		this.items.add(item);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#addItems(org.eclipsetrader.core.views.IWatchListElement[])
     */
    public void addItems(IWatchListElement[] items) {
		this.items.addAll(Arrays.asList(items));
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#addSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    public IWatchListElement addSecurity(ISecurity security) {
    	IWatchListElement element = new WatchListElement(security);
    	this.items.add(element);
	    return element;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#addSecurities(org.eclipsetrader.core.instruments.ISecurity[])
     */
    public IWatchListElement[] addSecurities(ISecurity[] securities) {
    	IWatchListElement[] elements = new WatchListElement[securities.length];
    	for (int i = 0; i < elements.length; i++)
    		elements[i] = new WatchListElement(securities[i]);
	    return elements;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getItem(int)
	 */
	public IWatchListElement getItem(int index) {
		if (index < 0 || index >= items.size())
			throw new IllegalArgumentException(index + " index out of range");
		return items.get(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getItem(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public IWatchListElement[] getItem(ISecurity security) {
		List<IWatchListElement> list = new ArrayList<IWatchListElement>();
		for (IWatchListElement element : items) {
			if (element.getSecurity() == security)
				list.add(element);
		}
		return list.toArray(new IWatchListElement[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getItemCount()
	 */
	public int getItemCount() {
		return items.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#getItems()
	 */
	public IWatchListElement[] getItems() {
		return items.toArray(new IWatchListElement[items.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#removeItem(org.eclipsetrader.core.views.IWatchListElement)
	 */
	public void removeItem(IWatchListElement item) {
		removeItems(new IWatchListElement[] { item });
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchList#removeItems(org.eclipsetrader.core.views.IWatchListElement[])
	 */
	public void removeItems(IWatchListElement[] items) {
		IWatchListElement[] oldItems = this.items.toArray(new IWatchListElement[this.items.size()]);

		this.items.removeAll(Arrays.asList(items));
		for (WatchListView view : views)
			view.doRemoveElements(items);

		if (oldItems.length != this.items.size())
			propertyChangeSupport.firePropertyChange(IWatchList.HOLDINGS, oldItems, this.items.toArray(new IWatchListElement[this.items.size()]));
	}

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getView()
     */
    public IView getView() {
    	WatchListView view = new WatchListView(this, getColumns(), getItems(), getMarketService());
    	views.add(view);
    	return view;
    }

    protected void disposeView(WatchListView view) {
    	views.remove(view);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#accept(org.eclipsetrader.core.views.IWatchListVisitor)
     */
    public void accept(IWatchListVisitor visitor) {
    	if (visitor.visit(this)) {
    		for (IWatchListColumn c : columns)
    			visitor.visit(c);
    		for (IWatchListElement e : items)
    			visitor.visit(e);
    	}
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
	    return store;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
     */
    public void setStore(IStore store) {
    	this.store = store;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
     */
    public IStoreProperties getStoreProperties() {
		if (storeProperties == null)
			storeProperties = new StoreProperties();

		storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
		storeProperties.setProperty(IPropertyConstants.NAME, getName());
		storeProperties.setProperty(IPropertyConstants.COLUMNS, columns.toArray(new IColumn[columns.size()]));
		storeProperties.setProperty(IPropertyConstants.HOLDINGS, items.toArray(new IHolding[items.size()]));

		return storeProperties;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    public void setStoreProperties(IStoreProperties storeProperties) {
    	this.storeProperties = storeProperties;
		this.name = (String) storeProperties.getProperty(IPropertyConstants.NAME);

		this.columns = new ArrayList<IWatchListColumn>();
		IColumn[] columns = (IColumn[]) storeProperties.getProperty(IPropertyConstants.COLUMNS);
		if (columns != null) {
			for (IColumn column : columns)
				this.columns.add(new WatchListColumn(column));
		}

		this.items = new ArrayList<IWatchListElement>();
		IHolding[] holdings = (IHolding[]) storeProperties.getProperty(IPropertyConstants.HOLDINGS);
		if (holdings != null) {
			for (IHolding holding : holdings)
				this.items.add(new WatchListElement(holding));
		}
    }

    protected IMarketService getMarketService() {
    	try {
    		BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
    		IMarketService service = (IMarketService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error reading market service", e);
    		CoreActivator.log(status);
    	}
    	return null;
    }
}
