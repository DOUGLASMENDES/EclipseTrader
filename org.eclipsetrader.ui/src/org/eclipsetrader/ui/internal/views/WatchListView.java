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

package org.eclipsetrader.ui.internal.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.core.views.WatchListElement;
import org.eclipsetrader.ui.internal.UIActivator;

public class WatchListView {
	private WatchList parent;
	private WatchListViewer viewPart;

	private String name;
	private List<WatchListViewColumn> columns = new ArrayList<WatchListViewColumn>();
	private Map<ISecurity, Set<WatchListViewItem>> items = new HashMap<ISecurity, Set<WatchListViewItem>>();

	private MarketPricingEnvironment pricingEnvironment;
	private Map<WatchListViewItem, Set<String>> updatedItems = new HashMap<WatchListViewItem, Set<String>>();

	private IPricingListener pricingListener = new IPricingListener() {
        public void pricingUpdate(PricingEvent event) {
        	doPricingUpdate(event.getSecurity());
        }
	};

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getSource() instanceof ISecurity)
        		doPricingUpdate((ISecurity) evt.getSource());
        	else if (evt.getSource() instanceof IWatchList) {
        		if (IWatchList.HOLDINGS.equals(evt.getPropertyName()))
        			doUpdateItems((IWatchListElement[]) evt.getNewValue());
        	}
        }
	};

	private Runnable updateRunnable = new Runnable() {
        public void run() {
    		if (!viewPart.getViewer().getControl().isDisposed()) {
        		synchronized(updatedItems) {
        			for (WatchListViewItem viewItem : updatedItems.keySet()) {
        				Set<String> propertyNames = updatedItems.get(viewItem);
        				viewPart.getViewer().update(viewItem, propertyNames.toArray(new String[propertyNames.size()]));
        			}
        			updatedItems.clear();
        		}
    		}
        }
	};

	public WatchListView(WatchList parent, WatchListViewer viewer) {
		this.parent = parent;
		this.viewPart = viewer;

		this.name = parent.getName();

		for (IWatchListColumn column : parent.getColumns())
			columns.add(new WatchListViewColumn(column));

		if (UIActivator.getDefault() != null) {
			pricingEnvironment = new MarketPricingEnvironment(UIActivator.getDefault().getMarketService());
			pricingEnvironment.addPricingListener(pricingListener);
		}

		for (IWatchListElement element : parent.getItems()) {
			Set<WatchListViewItem> set = items.get(element.getSecurity());
			if (set == null) {
				set = new HashSet<WatchListViewItem>();
				items.put(element.getSecurity(), set);

				if (pricingEnvironment != null)
					pricingEnvironment.addSecurity(element.getSecurity());

				PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) element.getSecurity().getAdapter(PropertyChangeSupport.class);
				if (propertyChangeSupport != null)
					propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
			}

			WatchListViewItem viewItem = new WatchListViewItem(element);

			if (pricingEnvironment != null) {
				viewItem.setLastClose(pricingEnvironment.getLastClose(element.getSecurity()));
				viewItem.setQuote(pricingEnvironment.getQuote(element.getSecurity()));
				viewItem.setTrade(pricingEnvironment.getTrade(element.getSecurity()));
				viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(element.getSecurity()));
			}

			for (WatchListViewColumn viewColumn : columns) {
				String propertyName = viewColumn.getDataProviderFactory().getId();
				IDataProvider dataProvider = viewColumn.getDataProviderFactory().createProvider();
				viewItem.setDataProvider(propertyName, dataProvider);

				IAdaptable value = dataProvider.getValue(viewItem);
				viewItem.setValue(propertyName, value);
			}

			set.add(viewItem);
		}

		parent.addPropertyChangeListener(propertyChangeListener);
	}

	public WatchList getParent() {
    	return parent;
    }

	public void dispose() {
		parent.removePropertyChangeListener(propertyChangeListener);

		for (ISecurity security : items.keySet()) {
			PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) security.getAdapter(PropertyChangeSupport.class);
			if (propertyChangeSupport != null)
				propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
		}

		pricingEnvironment.dispose();
	}

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public void addColumn(WatchListViewColumn column) {
		this.columns.add(column);

		for (Set<WatchListViewItem> set : items.values()) {
			for (WatchListViewItem viewItem : set) {
				String propertyName = column.getDataProviderFactory().getId();

				IDataProvider dataProvider = column.getDataProviderFactory().createProvider();
				viewItem.setDataProvider(propertyName, dataProvider);

				IAdaptable value = dataProvider.getValue(viewItem);
				viewItem.setValue(propertyName, value);
			}
		}

		viewPart.updateColumns(getColumns());
	}

	public void removeColumn(WatchListViewColumn column) {
		this.columns.remove(column);
		viewPart.updateColumns(getColumns());
	}

	public WatchListViewColumn[] getColumns() {
		return columns.toArray(new WatchListViewColumn[columns.size()]);
	}

	public WatchListViewColumn getColumn(int index) {
		if (index < 0 || index >= columns.size())
			return null;
		return columns.get(index);
	}

	public void setColumns(WatchListViewColumn[] columns) {
		this.columns = new ArrayList<WatchListViewColumn>(Arrays.asList(columns));

		for (Set<WatchListViewItem> set : items.values()) {
			for (WatchListViewItem viewItem : set) {
				for (WatchListViewColumn viewColumn : columns) {
					String propertyName = viewColumn.getDataProviderFactory().getId();
					if (viewItem.getDataProvider(propertyName) == null) {
						IDataProvider dataProvider = viewColumn.getDataProviderFactory().createProvider();
						viewItem.setDataProvider(propertyName, dataProvider);

						IAdaptable value = dataProvider.getValue(viewItem);
						viewItem.setValue(propertyName, value);
					}
				}
			}
		}

		viewPart.updateColumns(getColumns());
		viewPart.setDirty();
	}

	public void addItem(ISecurity security) {
		Set<WatchListViewItem> set = items.get(security);
		if (set == null) {
			set = new HashSet<WatchListViewItem>();
			items.put(security, set);
			pricingEnvironment.addSecurity(security);

			PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) security.getAdapter(PropertyChangeSupport.class);
			if (propertyChangeSupport != null)
				propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
		}

		WatchListViewItem viewItem = new WatchListViewItem(security);

		viewItem.setLastClose(pricingEnvironment.getLastClose(security));
		viewItem.setQuote(pricingEnvironment.getQuote(security));
		viewItem.setTrade(pricingEnvironment.getTrade(security));
		viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(security));

		for (WatchListViewColumn viewColumn : columns) {
			String propertyName = viewColumn.getDataProviderFactory().getId();
			IDataProvider dataProvider = viewColumn.getDataProviderFactory().createProvider();
			viewItem.setDataProvider(propertyName, dataProvider);

			IAdaptable value = dataProvider.getValue(viewItem);
			viewItem.setValue(propertyName, value);
		}

		set.add(viewItem);

		viewPart.getViewer().add(viewItem);
	}

	public void removeItem(WatchListViewItem viewItem) {
		Set<WatchListViewItem> set = items.get(viewItem.getSecurity());
		if (set != null) {
			set.remove(viewItem);
			if (set.isEmpty()) {
				pricingEnvironment.removeSecurity(viewItem.getSecurity());
				items.remove(set);

				PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) viewItem.getSecurity().getAdapter(PropertyChangeSupport.class);
				if (propertyChangeSupport != null)
					propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
			}
		}

		viewPart.getViewer().remove(viewItem);
	}

	public WatchListViewItem[] getItems() {
		Set<WatchListViewItem> l = new HashSet<WatchListViewItem>();
		for (Set<WatchListViewItem> set : items.values())
			l.addAll(set);
		return l.toArray(new WatchListViewItem[l.size()]);
	}

	public void doSave(IProgressMonitor monitor) {
		parent.setName(name);

		IWatchListColumn[] c = new IWatchListColumn[columns.size()];
		for (int i = 0; i < c.length; i++) {
			WatchListViewColumn viewColumn = columns.get(i);
			c[i] = viewColumn.getReference();
			if (c[i] != null)
				c[i].setName(viewColumn.getName());
			else
				c[i] = new WatchListColumn(viewColumn.getName(), viewColumn.getDataProviderFactory());
		}
		parent.setColumns(c);

		List<IWatchListElement> e = new ArrayList<IWatchListElement>();
		for (Set<WatchListViewItem> set : items.values()) {
			for (WatchListViewItem viewItem : set) {
				IWatchListElement element = viewItem.getReference();
				if (element != null) {
					element.setPosition(viewItem.getPosition());
					element.setPurchasePrice(viewItem.getPurchasePrice());
					element.setDate(viewItem.getDate());
				}
				else
					element = new WatchListElement(viewItem.getSecurity(), viewItem.getPosition(), viewItem.getPurchasePrice(), viewItem.getDate());
				e.add(element);
			}
		}
		parent.setItems(e.toArray(new IWatchListElement[e.size()]));

		final IRepositoryService repositoryService = UIActivator.getDefault().getRepositoryService();
		repositoryService.runInService(new IRepositoryRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
            	repositoryService.saveAdaptable(new IAdaptable[] { parent });
	            return Status.OK_STATUS;
            }
		}, monitor);
	}

	protected void doPricingUpdate(ISecurity security) {
		synchronized(updatedItems) {
			Set<WatchListViewItem> set = items.get(security);
			if (set != null) {
				for (WatchListViewItem viewItem : set) {
					viewItem.setLastClose(pricingEnvironment.getLastClose(security));
					viewItem.setQuote(pricingEnvironment.getQuote(security));
					viewItem.setTrade(pricingEnvironment.getTrade(security));
					viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(security));

					for (WatchListViewColumn viewColumn : columns) {
						String propertyName = viewColumn.getDataProviderFactory().getId();
						IDataProvider dataProvider = viewItem.getDataProvider(propertyName);
						if (dataProvider != null) {
							IAdaptable oldValue = viewItem.getValue(propertyName);
							IAdaptable newValue = dataProvider.getValue(viewItem);

							if (oldValue == newValue)
								continue;
							if (oldValue != null && newValue != null && oldValue.equals(newValue))
								continue;

							viewItem.setValue(propertyName, newValue);

	        				Set<String> propertyNames = updatedItems.get(viewItem);
	        				if (propertyNames == null) {
	        					propertyNames = new HashSet<String>();
	        					updatedItems.put(viewItem, propertyNames);
	        				}
							propertyNames.add(propertyName);

							if (viewPart.getViewer() != null && updatedItems.size() == 1) {
								try {
									if (!viewPart.getViewer().getControl().isDisposed())
										viewPart.getViewer().getControl().getDisplay().asyncExec(updateRunnable);
								} catch(SWTException e) {
									if (e.code != SWT.ERROR_WIDGET_DISPOSED)
										throw e;
								}
							}
						}
					}
				}
			}
		}
	}

	protected void doUpdateItems(IWatchListElement[] newItems) {
		Set<IWatchListElement> newItemsSet = new HashSet<IWatchListElement>(Arrays.asList(newItems));

		Set<IWatchListElement> existingItemsSet = new HashSet<IWatchListElement>();
		for (Set<WatchListViewItem> set : items.values()) {
			for (Iterator<WatchListViewItem> viewItemIterator = set.iterator(); viewItemIterator.hasNext(); ) {
				WatchListViewItem viewItem = viewItemIterator.next();
				if (viewItem.getReference() != null) {
					if (!newItemsSet.contains(viewItem.getReference()))
						viewItemIterator.remove();
					else
						existingItemsSet.add(viewItem.getReference());
				}
			}
		}

		for (IWatchListElement newItem : newItemsSet) {
			if (!existingItemsSet.contains(newItem)) {
				Set<WatchListViewItem> set = items.get(newItem.getSecurity());
				if (set == null) {
					set = new HashSet<WatchListViewItem>();
					items.put(newItem.getSecurity(), set);

					if (pricingEnvironment != null)
						pricingEnvironment.addSecurity(newItem.getSecurity());

					PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) newItem.getSecurity().getAdapter(PropertyChangeSupport.class);
					if (propertyChangeSupport != null)
						propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
				}

				WatchListViewItem viewItem = new WatchListViewItem(newItem);

				if (pricingEnvironment != null) {
					viewItem.setLastClose(pricingEnvironment.getLastClose(newItem.getSecurity()));
					viewItem.setQuote(pricingEnvironment.getQuote(newItem.getSecurity()));
					viewItem.setTrade(pricingEnvironment.getTrade(newItem.getSecurity()));
					viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(newItem.getSecurity()));
				}

				for (WatchListViewColumn viewColumn : columns) {
					String propertyName = viewColumn.getDataProviderFactory().getId();
					IDataProvider dataProvider = viewColumn.getDataProviderFactory().createProvider();
					viewItem.setDataProvider(propertyName, dataProvider);

					IAdaptable value = dataProvider.getValue(viewItem);
					viewItem.setValue(propertyName, value);
				}

				set.add(viewItem);
			}
		}

		for (Iterator<Entry<ISecurity, Set<WatchListViewItem>>> iter = items.entrySet().iterator(); iter.hasNext(); ) {
			Entry<ISecurity, Set<WatchListViewItem>> entry = iter.next();
			if (entry.getValue().size() == 0) {
				if (pricingEnvironment != null)
					pricingEnvironment.removeSecurity(entry.getKey());
				iter.remove();
			}
		}

		if (viewPart != null && viewPart.getViewer() != null)
			viewPart.getViewer().refresh();
	}

	Map<WatchListViewItem, Set<String>> getUpdatedItems() {
    	return updatedItems;
    }

	Map<ISecurity, Set<WatchListViewItem>> getItemsMap() {
		return items;
	}
}
