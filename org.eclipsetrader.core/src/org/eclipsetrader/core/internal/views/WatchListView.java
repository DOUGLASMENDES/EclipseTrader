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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.ISessionData;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.core.views.ViewItemDelta;

public class WatchListView implements IView, IPricingListener {
	private static final String K_ORIGINAL = "original";

	private WatchList parent;
	private List<IWatchListColumn> columns = new ArrayList<IWatchListColumn>();
	private List<IWatchListElement> items= new ArrayList<IWatchListElement>();

	private String name;
	private Map<ISecurity, Set<WatchListViewItem>> map = new HashMap<ISecurity, Set<WatchListViewItem>>();
	private List<IDataProvider> dataProviders = new ArrayList<IDataProvider>();

	private WatchListViewItem root = new WatchListViewItem(this);
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private MarketPricingEnvironment pricingEnvironment;

	private PropertyChangeSupport propertyChangeSupport;

	protected WatchListView() {
	}

	public WatchListView(WatchList parent, IWatchListColumn[] columns, IWatchListElement[] elements, IMarketService marketService) {
		this.parent = parent;
		this.name = parent.getName();

		if (marketService != null) {
			pricingEnvironment = (MarketPricingEnvironment) marketService.getPricingEnvironment();
			pricingEnvironment.addPricingListener(this);
		}

		doSetColumns(columns);
		addElements(elements);

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public String getName() {
    	return name;
    }

	public void setName(String name) {
		Object oldValue = this.name;
    	this.name = name;
    	propertyChangeSupport.firePropertyChange(IWatchList.NAME, oldValue, this.name);
    }

	public IWatchList getParent() {
    	return parent;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#dispose()
	 */
	public void dispose() {
		parent.disposeView(this);

		pricingEnvironment.removePricingListener(this);
    	pricingEnvironment.dispose();

    	listeners.clear();
    	root = new WatchListViewItem(this);

    	for (IDataProvider dataProvider : dataProviders)
			dataProvider.dispose();
	}

	public IWatchListColumn[] getColumns() {
		return this.columns.toArray(new IWatchListColumn[this.columns.size()]);
	}

	public void setColumns(IWatchListColumn[] columns) {
		IWatchListColumn[] oldValue = this.columns.toArray(new IWatchListColumn[this.columns.size()]);
		doSetColumns(columns);
    	propertyChangeSupport.firePropertyChange(IWatchList.COLUMNS, oldValue, this.columns.toArray(new IWatchListColumn[this.columns.size()]));
	}

	protected void doSetColumns(IWatchListColumn[] newColumns) {
		Map<IWatchListColumn, IWatchListColumn> currentColumns = new HashMap<IWatchListColumn, IWatchListColumn>();
		for (IWatchListColumn c : columns) {
			ISessionData session = (ISessionData) c.getAdapter(ISessionData.class);
			if (session != null && session.getData(K_ORIGINAL) != null)
				currentColumns.put((IWatchListColumn) session.getData(K_ORIGINAL), c);
			else
				currentColumns.put(c, c);
		}

		columns = new ArrayList<IWatchListColumn>();
		for (IWatchListColumn c : newColumns) {
			ISessionData session = (ISessionData) c.getAdapter(ISessionData.class);
			if (session != null && session.getData(K_ORIGINAL) != null) {
				columns.add(c);
				continue;
			}

			if (currentColumns.containsKey(c)) {
				columns.add(currentColumns.get(c));
				continue;
			}

			try {
				Method method = c.getClass().getMethod("clone", new Class[0]);
				if (method != null) {
					IWatchListColumn cloned = (IWatchListColumn) method.invoke(c, new Object[0]);
					session = (ISessionData) cloned.getAdapter(ISessionData.class);
					if (session != null)
						session.setData(K_ORIGINAL, c);
					columns.add(cloned);
				}
            } catch (Exception e) {
	            e.printStackTrace();
	            columns.add(c);
            }
		}

		updateDataProviders(columns.toArray(new WatchListColumn[columns.size()]));
	}

	public void addElements(IWatchListElement[] elements) {
		IWatchListElement[] oldItems = this.items.toArray(new IWatchListElement[this.items.size()]);

		for (IWatchListElement i : elements) {
			try {
				Method method = i.getClass().getMethod("clone", new Class[0]);
				if (method != null) {
					IWatchListElement cloned = (IWatchListElement) method.invoke(i, new Object[0]);
					ISessionData session = (ISessionData) cloned.getAdapter(ISessionData.class);
					if (session != null)
						session.setData(K_ORIGINAL, i);
		            this.items.add(cloned);
				}
            } catch (Exception e) {
	            e.printStackTrace();
	            this.items.add(i);
            }
		}
		updateSecurities(this.items.toArray(new IWatchListElement[this.items.size()]));

		if (propertyChangeSupport != null && oldItems.length != this.items.size())
			propertyChangeSupport.firePropertyChange(IWatchList.HOLDINGS, oldItems, this.items.toArray(new IWatchListElement[this.items.size()]));
	}

	public IWatchListElement addSecurity(ISecurity security) {
		WatchListElement element = new WatchListElement(security);
		addElements(new IWatchListElement[] { element });
		return element;
	}

	public IWatchListElement[] addSecurities(ISecurity[] securities) {
		WatchListElement[] elements = new WatchListElement[securities.length];
		for (int i = 0; i < elements.length; i++)
			elements[i] = new WatchListElement(securities[i]);
		addElements(elements);
		return elements;
	}

	public void removeElements(IWatchListElement[] elements) {
		IWatchListElement[] oldItems = this.items.toArray(new IWatchListElement[this.items.size()]);
		doRemoveElements(elements);
		if (oldItems.length != this.items.size())
			propertyChangeSupport.firePropertyChange(IWatchList.HOLDINGS, oldItems, this.items.toArray(new IWatchListElement[this.items.size()]));
	}

	protected void doRemoveElements(IWatchListElement[] elements) {
		List<IWatchListElement> toRemove = Arrays.asList(elements);
		for (Iterator<IWatchListElement> iter = this.items.iterator(); iter.hasNext(); ) {
			IWatchListElement element = iter.next();
			if (toRemove.contains(element))
				iter.remove();
			else {
				ISessionData session = (ISessionData) element.getAdapter(ISessionData.class);
				if (session != null) {
					if (toRemove.contains(session.getData(K_ORIGINAL)))
						iter.remove();
				}
			}
		}
		updateSecurities(this.items.toArray(new WatchListElement[this.items.size()]));
	}

	public IWatchListElement[] getElements() {
		return this.items.toArray(new WatchListElement[this.items.size()]);
	}

	public void synchronize() {
		parent.setName(name);
		synchronizeElements();
		synchronizeColumns();
	}

	protected void synchronizeElements() {
		List<IWatchListElement> currentList = new ArrayList<IWatchListElement>(Arrays.asList(parent.getItems()));
		List<IWatchListElement> originalList = new ArrayList<IWatchListElement>();

		for (IWatchListElement i : this.items) {
			ISessionData session = (ISessionData) i.getAdapter(ISessionData.class);
			if (session != null) {
				IWatchListElement original = (IWatchListElement) session.getData(K_ORIGINAL);
				original.setPosition(i.getPosition());
				original.setPurchasePrice(i.getPurchasePrice());
				original.setDate(i.getDate());
				if (!currentList.contains(original)) {
					currentList.add(original);
					parent.addItem(original);
				}
				originalList.add(original);
			}
			else {
				if (!currentList.contains(i)) {
					currentList.add(i);
					parent.addItem(i);
				}
				originalList.add(i);
			}
		}

		for (Iterator<IWatchListElement> iter = currentList.iterator(); iter.hasNext(); ) {
			IWatchListElement element = iter.next();
			if (!originalList.contains(element)) {
				iter.remove();
				parent.removeItem(element);
			}
		}
	}

	protected void synchronizeColumns() {
		List<IWatchListColumn> list = new ArrayList<IWatchListColumn>();

		for (IWatchListColumn i : this.columns) {
			ISessionData session = (ISessionData) i.getAdapter(ISessionData.class);
			if (session != null && session.getData(K_ORIGINAL) != null) {
				IWatchListColumn original = (IWatchListColumn) session.getData(K_ORIGINAL);
				original.setName(i.getName());
				list.add(original);
			}
			else
				list.add(i);
		}

		parent.setColumns(list.toArray(new IWatchListColumn[list.size()]));
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#getItems()
	 */
	public IViewItem[] getItems() {
        return root.getItems();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#addListener(org.eclipsetrader.core.views.IViewChangeListener)
	 */
	public void addViewChangeListener(IViewChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#removeListener(org.eclipsetrader.core.views.IViewChangeListener)
	 */
	public void removeViewChangeListener(IViewChangeListener listener) {
		listeners.remove(listener);
	}

	protected void updateDataProviders(IWatchListColumn[] columns) {
		synchronized(dataProviders) {
			for (IDataProvider dataProvider : dataProviders) {
				if (dataProvider != null)
					dataProvider.dispose();
			}
			dataProviders.clear();
			for (int i = 0; i < columns.length; i++)
				dataProviders.add(columns[i].getDataProviderFactory() != null ? columns[i].getDataProviderFactory().createProvider() : null);
		}
	}

	protected void updateSecurities(IWatchListElement[] elements) {
    	List<ViewItemDelta> itemDelta = new ArrayList<ViewItemDelta>();

    	synchronized(root) {
	    	for (IWatchListElement element : elements) {
				Set<WatchListViewItem> set = map.get(element.getSecurity());
				if (set == null) {
					set = new HashSet<WatchListViewItem>();
					map.put(element.getSecurity(), set);
				}
				if (!root.hasChild(element)) {
					WatchListViewItem child = root.createChild(element);
					set.add(child);
					itemDelta.add(new ViewItemDelta(ViewItemDelta.ADDED, child, null, null));
				}
			}
			List<IWatchListElement> items = new ArrayList<IWatchListElement>(Arrays.asList(elements));
			for (Iterator<WatchListViewItem> iter = root.iterator(); iter.hasNext(); ) {
				WatchListViewItem viewItem = iter.next();
				if (!items.contains(viewItem.getReference())) {
					iter.remove();
					itemDelta.add(new ViewItemDelta(ViewItemDelta.REMOVED, viewItem, viewItem.getValues(), null));
				}
			}
		}

		if (pricingEnvironment != null) {
			Set<ISecurity> s = map.keySet();
			pricingEnvironment.addSecurities(s.toArray(new ISecurity[s.size()]));

			for (ISecurity security : s) {
				for (WatchListViewItem viewItem : map.get(security)) {
					WatchListElement element = (WatchListElement) viewItem.getReference();
					element.setTrade(pricingEnvironment.getTrade(security));
					element.setQuote(pricingEnvironment.getQuote(security));
					element.setTodayOHL(pricingEnvironment.getTodayOHL(security));
					element.setLastClose(pricingEnvironment.getLastClose(security));

					IAdaptable[] newValues = new IAdaptable[dataProviders.size()];
					for (int i = 0; i < newValues.length; i++)
						newValues[i] = dataProviders.get(i) != null ? dataProviders.get(i).getValue(element) : null;
					viewItem.setValues(newValues);
				}
			}
		}

		if (itemDelta.size() != 0)
			notifyListeners(new ViewEvent(this, itemDelta.toArray(new ViewItemDelta[itemDelta.size()])));
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IPricingListener#pricingUpdate(org.eclipsetrader.core.feed.PricingEvent)
     */
    public void pricingUpdate(PricingEvent event) {
		WatchListViewItem[] viewItems = null;
    	synchronized(map) {
    		Set<WatchListViewItem> set = map.get(event.getSecurity());
    		if (set == null)
    			return;
    		viewItems = set.toArray(new WatchListViewItem[set.size()]);
    	}

    	IDataProvider[] providers;
    	synchronized(dataProviders) {
    		providers = dataProviders.toArray(new IDataProvider[dataProviders.size()]);
    	}

    	List<ViewItemDelta> itemDelta = new ArrayList<ViewItemDelta>();

    	for (WatchListViewItem viewItem : viewItems) {
			WatchListElement element = (WatchListElement) viewItem.getReference();

			for (PricingDelta delta : event.getDelta()) {
				if (delta.getNewValue() instanceof ITrade)
					element.setTrade((ITrade) delta.getNewValue());
				if (delta.getNewValue() instanceof IQuote)
					element.setQuote((IQuote) delta.getNewValue());
				if (delta.getNewValue() instanceof ITodayOHL)
					element.setTodayOHL((ITodayOHL) delta.getNewValue());
				if (delta.getNewValue() instanceof ILastClose)
					element.setLastClose((ILastClose) delta.getNewValue());
			}

			IAdaptable[] oldValues = viewItem.getValues();

			IAdaptable[] newValues = new IAdaptable[providers.length];
			for (int i = 0; i < newValues.length; i++)
				newValues[i] = providers[i] != null ? providers[i].getValue(element) : null;

			if (!valuesEquals(oldValues, newValues)) {
				viewItem.setValues(newValues);
				itemDelta.add(new ViewItemDelta(ViewItemDelta.CHANGED, viewItem, oldValues, newValues));
			}
		}

		if (itemDelta.size() != 0)
			notifyListeners(new ViewEvent(this, itemDelta.toArray(new ViewItemDelta[itemDelta.size()])));
    }

	protected void notifyListeners(ViewEvent event) {
		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			try {
				((IViewChangeListener) l[i]).viewChanged(event);
			} catch(Exception e) {
	    		Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running view listener", e);
	    		CoreActivator.log(status);
			} catch(LinkageError e) {
	    		Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running view listener", e);
	    		CoreActivator.log(status);
			}
		}
	}

    protected boolean valuesEquals(IAdaptable[] oldValues, IAdaptable[] newValues) {
    	if (oldValues == newValues)
    		return true;

    	if ((oldValues == null && newValues != null) || (oldValues != null && newValues == null))
    		return false;
    	if (oldValues.length != newValues.length)
    		return false;

    	for (int i = 0; i < newValues.length; i++) {
        	if (oldValues[i] == newValues[i])
        		continue;
        	if ((oldValues[i] == null && newValues[i] != null) || (oldValues[i] != null && newValues[i] == null))
        		return false;
        	if (!newValues[i].equals(oldValues[i]))
        		return false;
    	}

    	return true;
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(parent.getClass()))
    		return parent;
    	if (adapter.isAssignableFrom(PropertyChangeSupport.class))
    		return propertyChangeSupport;
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#accept(org.eclipsetrader.core.views.IViewVisitor)
     */
    public void accept(IViewVisitor visitor) {
    	if (visitor.visit(this)) {
    		for (IViewItem viewItem : getItems())
    			viewItem.accept(visitor);
    	}
    }
}
