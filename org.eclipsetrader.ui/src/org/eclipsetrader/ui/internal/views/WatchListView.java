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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.core.views.WatchListElement;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.repositories.RepositoryObjectTransfer;
import org.eclipsetrader.ui.internal.securities.SecurityObjectTransfer;

public class WatchListView extends ViewPart implements ISaveablePart {
	public static final String VIEW_ID = "org.eclipsetrader.ui.views.watchlist";
	public static final String K_VIEWS = "Views";
	public static final String K_URI = "uri";

	private URI uri;
	private WatchList watchList;

	private String name;
	private List<WatchListViewColumn> columns = new ArrayList<WatchListViewColumn>();
	private Map<ISecurity, Set<WatchListViewItem>> items = new HashMap<ISecurity, Set<WatchListViewItem>>();

	private Action deleteAction;
	private Action settingsAction;

	private TableViewer viewer;
	private boolean dirty = false;

	private Color evenRowsColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	private Color oddRowsColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	private Color tickBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
	private Color[] tickOddRowsFade = new Color[3];
	private Color[] tickEvenRowsFade = new Color[3];

	private IDialogSettings dialogSettings;
	private int sortColumn = 0;
	private int sortDirection = SWT.UP;

	private MarketPricingEnvironment pricingEnvironment;
	private Set<WatchListViewItem> updatedItems = new HashSet<WatchListViewItem>();

	private ControlAdapter columnControlListener = new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {
        	TableColumn tableColumn = (TableColumn) e.widget;
        	if (dialogSettings != null) {
        		IDialogSettings columnsSection = dialogSettings.getSection("columns");
        		if (columnsSection == null)
        			columnsSection = dialogSettings.addNewSection("columns");
        		columnsSection.put(tableColumn.getText(), tableColumn.getWidth());
        	}
        }
	};

	private SelectionAdapter columnSelectionAdapter = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
        	TableColumn tableColumn = (TableColumn) e.widget;
        	Table table = tableColumn.getParent();

        	sortColumn = tableColumn.getParent().indexOf(tableColumn);
        	if (table.getSortColumn() == tableColumn)
            	sortDirection = sortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
            else {
            	sortDirection = SWT.UP;
            	table.setSortColumn(table.getColumn(sortColumn));
            }
        	table.setSortDirection(sortDirection);

        	WatchListViewColumn column = columns.get(sortColumn);
            dialogSettings.put("sortColumn", column.getDataProviderFactory().getId());
            dialogSettings.put("sortDirection", sortDirection == SWT.UP ? 1 : -1);
            viewer.refresh();
        }
	};

	private IRepositoryChangeListener repositoryListener = new IRepositoryChangeListener() {
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
        	for (RepositoryResourceDelta delta : event.getDeltas()) {
        		if (delta.getResource() == watchList) {
        		    IStoreObject objectStore = (IStoreObject) ((IAdaptable) delta.getResource()).getAdapter(IStoreObject.class);
        		    dialogSettings.put(K_URI, objectStore.getStore().toURI().toString());
        			break;
        		}
        	}
        }
	};

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
        	if (evt.getSource() instanceof ISecurity)
        		doPricingUpdate((ISecurity) evt.getSource());
        	else if (evt.getSource() instanceof IWatchList) {
	        	if (IWatchList.NAME.equals(evt.getPropertyName())) {
	        		if (name.equals(evt.getOldValue())) {
		        		name = (String) evt.getNewValue();
		        		setPartName(name);
	        		}
	        	}
	        	else if (IWatchList.HOLDINGS.equals(evt.getPropertyName()))
        			doUpdateItems((IWatchListElement[]) evt.getNewValue());
	        	else if (IWatchList.COLUMNS.equals(evt.getPropertyName())) {
	        		// TODO
	        	}
        	}
        }
	};

	private IPricingListener pricingListener = new IPricingListener() {
        public void pricingUpdate(PricingEvent event) {
        	doPricingUpdate(event.getSecurity());
        }
	};

	private Runnable updateRunnable = new Runnable() {
        public void run() {
    		if (!viewer.getControl().isDisposed()) {
        		synchronized(updatedItems) {
        			for (WatchListViewItem viewItem : updatedItems) {
        				Set<String> propertyNames = new HashSet<String>();

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
    							if (oldValue != null && viewColumn.getDataProviderFactory().getType()[0] != Image.class)
    								viewItem.setUpdateTime(propertyName, 6);

    							propertyNames.add(propertyName);
    						}
    					}

    					if (propertyNames.size() != 0)
    						viewer.update(viewItem, propertyNames.toArray(new String[propertyNames.size()]));
        			}
        			updatedItems.clear();
        		}
    		}
        }
	};

	public WatchListView() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);
		ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();

        try {
    		dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(K_VIEWS).getSection(site.getSecondaryId());
        	uri = new URI(dialogSettings.get(K_URI));
	        IWatchList watchList = UIActivator.getDefault().getRepositoryService().getWatchListFromURI(uri);
	        if (watchList instanceof WatchList)
	        	this.watchList = (WatchList) watchList;
        } catch (URISyntaxException e) {
        	Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, "Error loading view " + site.getSecondaryId(), e);
	        UIActivator.getDefault().getLog().log(status);
        }

		deleteAction = new Action("Delete") {
			@Override
			public void run() {
				Object[] s = ((IStructuredSelection) viewer.getSelection()).toArray();
				if (s.length != 0) {
					for (int i = 0; i < s.length; i++)
						removeItem((WatchListViewItem) s[i]);
					setDirty();
				}
			}
		};
		deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_ICON));
		deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_DISABLED_ICON));
		deleteAction.setId(ActionFactory.DELETE.getId());
		deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
		deleteAction.setEnabled(false);

		settingsAction = new SettingsAction(site.getShell(), this);

		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(settingsAction.getId(), settingsAction);
		actionBars.setGlobalActionHandler(deleteAction.getId(), deleteAction);
		actionBars.updateActionBars();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
    @Override
	public void createPartControl(Composite parent) {
    	if (watchList == null) {
    		Composite composite = new Composite(parent, SWT.NONE);
    		composite.setLayout(new GridLayout(1, false));
    		Label label = new Label(composite, SWT.NONE);
    		label.setText(NLS.bind("Unable to load view {0}", new Object[] { uri != null ? uri.toString() : "" }));
    		return;
    	}

		setTickBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB());

		viewer = createViewer(parent);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
				deleteAction.setEnabled(!event.getSelection().isEmpty());
            }
		});
		viewer.addDropSupport(
				DND.DROP_COPY | DND.DROP_MOVE,
				new Transfer[] {
						SecurityObjectTransfer.getInstance(),
						RepositoryObjectTransfer.getInstance(),
					},
				new ViewerDropAdapter(viewer) {
                    @Override
                    public boolean validateDrop(Object target, int operation, TransferData transferType) {
	                    return SecurityObjectTransfer.getInstance().isSupportedType(transferType) ||
	                           RepositoryObjectTransfer.getInstance().isSupportedType(transferType);
                    }

                    @Override
                    public boolean performDrop(Object data) {
						final IAdaptable[] contents = (IAdaptable[]) data;
						for (int i = 0; i < contents.length; i++) {
							ISecurity security = (ISecurity) contents[i].getAdapter(ISecurity.class);
							if (security != null) {
								addItem(security);
								setDirty();
							}
						}
						return true;
                    }
				});

		initializeContextMenu();
		getViewSite().setSelectionProvider(viewer);

		pricingEnvironment = new MarketPricingEnvironment(UIActivator.getDefault().getMarketService());

		buildWatchListView();

		UIActivator.getDefault().getRepositoryService().addRepositoryResourceListener(repositoryListener);

		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) watchList.getAdapter(PropertyChangeSupport.class);
		if (propertyChangeSupport != null)
			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);

		pricingEnvironment.addPricingListener(pricingListener);
	}

    private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(new Separator("group.new"));
				menuManager.add(new GroupMarker("group.goto"));
				menuManager.add(new Separator("group.open"));
				menuManager.add(new GroupMarker("group.openWith"));
				menuManager.add(new Separator("group.trade"));
				menuManager.add(new GroupMarker("group.tradeWith"));
				menuManager.add(new Separator("group.show"));
				menuManager.add(new Separator("group.edit"));
				menuManager.add(new GroupMarker("group.reorganize"));
				menuManager.add(new GroupMarker("group.port"));
				menuManager.add(new Separator("group.generate"));
				menuManager.add(new Separator("group.search"));
				menuManager.add(new Separator("group.build"));
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				menuManager.add(new Separator("group.properties"));

				menuManager.appendToGroup("group.edit", deleteAction);
			}
		});
		viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
		pricingEnvironment.removePricingListener(pricingListener);
		pricingEnvironment.dispose();

    	if (watchList != null) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) watchList.getAdapter(PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    	}

		for (ISecurity security : items.keySet()) {
			PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) security.getAdapter(PropertyChangeSupport.class);
			if (propertyChangeSupport != null)
				propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
		}

		UIActivator.getDefault().getRepositoryService().removeRepositoryResourceListener(repositoryListener);

		for (Set<WatchListViewItem> set : items.values()) {
			for (Iterator<WatchListViewItem> viewItemIterator = set.iterator(); viewItemIterator.hasNext(); ) {
				WatchListViewItem viewItem = viewItemIterator.next();
				for (WatchListViewColumn viewColumn : columns) {
					String propertyName = viewColumn.getDataProviderFactory().getId();
					viewItem.getDataProvider(propertyName).dispose();
				}
			}
		}

		for (int i = 0; i < tickEvenRowsFade.length; i++) {
			if (tickEvenRowsFade[i] != null)
				tickEvenRowsFade[i].dispose();
		}
		for (int i = 0; i < tickOddRowsFade.length; i++) {
			if (tickOddRowsFade[i] != null)
				tickOddRowsFade[i].dispose();
		}

		super.dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) watchList.getAdapter(PropertyChangeSupport.class);
		if (propertyChangeSupport != null)
			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);

		watchList.setName(name);

		IWatchListColumn[] c = new IWatchListColumn[columns.size()];
		for (int i = 0; i < c.length; i++) {
			WatchListViewColumn viewColumn = columns.get(i);
			c[i] = viewColumn.getReference();
			if (c[i] != null)
				c[i].setName(viewColumn.getName());
			else
				c[i] = new WatchListColumn(viewColumn.getName(), viewColumn.getDataProviderFactory());
		}
		watchList.setColumns(c);

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
		watchList.setItems(e.toArray(new IWatchListElement[e.size()]));

		final IRepositoryService repositoryService = UIActivator.getDefault().getRepositoryService();
		IStatus status = repositoryService.runInService(new IRepositoryRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
            	repositoryService.saveAdaptable(new IAdaptable[] { watchList });
	            return Status.OK_STATUS;
            }
		}, monitor);

		if (status == Status.OK_STATUS) {
			dirty = false;
	    	firePropertyChange(PROP_DIRTY);
		}

		if (propertyChangeSupport != null)
			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty() {
	    return dirty;
    }

	public void setDirty() {
		if (!dirty) {
	    	dirty = true;
	    	firePropertyChange(PROP_DIRTY);
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
	    return dirty;
    }

	protected TableViewer createViewer(Composite parent) {
    	Composite container = new Composite(parent, SWT.NONE);
		TableColumnLayout tableLayout = new TableColumnLayout();
		container.setLayout(tableLayout);

		viewer = new TableViewer(container, SWT.MULTI | SWT.FULL_SELECTION) {
            @Override
            protected void inputChanged(Object input, Object oldInput) {
	            super.inputChanged(input, oldInput);
	    		if (sortColumn >= getTable().getColumnCount()) {
	    			sortColumn = 0;
	    			sortDirection = SWT.UP;
	    		}
	    		if (sortColumn < getTable().getColumnCount()) {
		    		getTable().setSortDirection(sortDirection);
		    		getTable().setSortColumn(getTable().getColumn(sortColumn));
	    		}
            }
		};
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		viewer.setUseHashlookup(true);

		viewer.setContentProvider(new WatchListViewContentProvider());
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	if (sortColumn < 0 || sortColumn >= columns.size())
            		return 0;
            	String propertyName = columns.get(sortColumn).getDataProviderFactory().getId();
            	IAdaptable v1 = ((WatchListViewItem) e1).getValue(propertyName);
            	IAdaptable v2 = ((WatchListViewItem) e2).getValue(propertyName);
            	if (sortDirection == SWT.DOWN) {
                	v1 = ((WatchListViewItem) e2).getValue(propertyName);
                	v2 = ((WatchListViewItem) e1).getValue(propertyName);
            	}
            	return compareValues(v1, v2);
            }
		});

		return viewer;
	}

    @SuppressWarnings("unchecked")
    protected int compareValues(IAdaptable v1, IAdaptable v2) {
    	if (v1 == null || v2 == null)
    		return 0;

    	Object o1 = v1.getAdapter(Comparable.class);
    	Object o2 = v2.getAdapter(Comparable.class);
    	if (o1 != null && o2 != null)
    		return ((Comparable) o1).compareTo(o2);

    	o1 = v1.getAdapter(Number.class);
    	o2 = v2.getAdapter(Number.class);
    	if (o1 != null && o2 != null) {
    		if (((Number) o1).doubleValue() < ((Number) o2).doubleValue())
    			return -1;
    		if (((Number) o1).doubleValue() > ((Number) o2).doubleValue())
    			return 1;
    		return 0;
    	}

    	return 0;
    }

	protected void updateColumns() {
		String[] properties = createColumns(viewer, columns.toArray(new WatchListViewColumn[columns.size()]));
		viewer.setColumnProperties(properties);
	}

    @SuppressWarnings("unchecked")
	protected String[] createColumns(TableViewer viewer, WatchListViewColumn[] columns) {
    	Table table = viewer.getTable();
		TableColumnLayout tableLayout = (TableColumnLayout) table.getParent().getLayout();
		IDialogSettings columnsSection = dialogSettings != null ? dialogSettings.getSection("columns") : null;

		String[] properties = new String[columns.length];

		table.setRedraw(false);
		try {
			TableColumn[] tableColumn = table.getColumns();
			for (int i = 0; i < tableColumn.length; i++)
				tableColumn[i].dispose();

			int index = 0;
			for (WatchListViewColumn column : columns) {
				int alignment = SWT.LEFT;
				if (column.getDataProviderFactory().getType() != null && column.getDataProviderFactory().getType().length != 0) {
					Class type = column.getDataProviderFactory().getType()[0];
					if (type == Long.class || type == Double.class || type == Date.class)
						alignment = SWT.RIGHT;
					if (type == Image.class)
						alignment = SWT.CENTER;
				}

				TableViewerColumn viewerColumn = new TableViewerColumn(viewer, alignment);
				viewerColumn.getColumn().setText(column.getName() != null ? column.getName() : column.getDataProviderFactory().getName());

				WatchListColumnLabelProvider labelProvider = new WatchListColumnLabelProvider(column);
				labelProvider.setColors(evenRowsColor, oddRowsColor, tickBackgroundColor, tickEvenRowsFade, tickOddRowsFade);
				viewerColumn.setLabelProvider(labelProvider);

				properties[index] = column.getDataProviderFactory().getId();

				int width = columnsSection != null && columnsSection.get(viewerColumn.getColumn().getText()) != null ? columnsSection.getInt(viewerColumn.getColumn().getText()) : 100;
				tableLayout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(width));

				viewerColumn.getColumn().addControlListener(columnControlListener);
				viewerColumn.getColumn().addSelectionListener(columnSelectionAdapter);

				if (dialogSettings != null) {
					if (column.getDataProviderFactory().getId().equals(dialogSettings.get("sortColumn")))
						sortColumn = index;
				}

				index++;
			}
		} finally {
			table.setRedraw(true);
			table.getParent().layout();
			viewer.refresh();
		}

		return properties;
	}

    Table getTable() {
    	return viewer.getTable();
    }

    TableViewer getViewer() {
    	return viewer;
    }

	protected void buildWatchListView() {
		this.name = watchList.getName();
		setPartName(name);

		for (IWatchListColumn column : watchList.getColumns())
			columns.add(new WatchListViewColumn(column));
		updateColumns();

		for (IWatchListElement element : watchList.getItems()) {
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
			setInitialValues(viewItem);
			set.add(viewItem);
		}
		viewer.setInput(items);
    }

    protected void setInitialValues(WatchListViewItem viewItem) {
    	ISecurity security = viewItem.getSecurity();

    	if (pricingEnvironment != null) {
			viewItem.setLastClose(pricingEnvironment.getLastClose(security));
			viewItem.setQuote(pricingEnvironment.getQuote(security));
			viewItem.setTrade(pricingEnvironment.getTrade(security));
			viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(security));
			viewItem.setPricingEnvironment(pricingEnvironment);
		}

    	for (String propertyName : viewItem.getValueProperties())
			viewItem.clearValue(propertyName);

		for (WatchListViewColumn viewColumn : columns) {
			String propertyName = viewColumn.getDataProviderFactory().getId();
			IDataProvider dataProvider = viewColumn.getDataProviderFactory().createProvider();
			viewItem.setDataProvider(propertyName, dataProvider);

			dataProvider.init(viewItem);

			IAdaptable value = dataProvider.getValue(viewItem);
			viewItem.setValue(propertyName, value);
		}
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
					viewItem.setBook(pricingEnvironment.getBook(security));

					updatedItems.add(viewItem);

					if (viewer != null && updatedItems.size() == 1) {
						try {
							if (!viewer.getControl().isDisposed())
								viewer.getControl().getDisplay().asyncExec(updateRunnable);
						} catch(SWTException e) {
							if (e.code != SWT.ERROR_WIDGET_DISPOSED)
								throw e;
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
					if (!newItemsSet.contains(viewItem.getReference())) {
						for (WatchListViewColumn viewColumn : columns) {
							String propertyName = viewColumn.getDataProviderFactory().getId();
							viewItem.getDataProvider(propertyName).dispose();
							viewItem.clearDataProvider(propertyName);
							viewItem.clearValue(propertyName);
						}
						viewItemIterator.remove();
					}
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
					viewItem.setBook(pricingEnvironment.getBook(newItem.getSecurity()));
				}

				for (WatchListViewColumn viewColumn : columns) {
					String propertyName = viewColumn.getDataProviderFactory().getId();
					IDataProvider dataProvider = viewColumn.getDataProviderFactory().createProvider();
					viewItem.setDataProvider(propertyName, dataProvider);

					dataProvider.init(viewItem);

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

		if (viewer != null)
			viewer.refresh();
	}

	protected void addItem(ISecurity security) {
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

			dataProvider.init(viewItem);

			IAdaptable value = dataProvider.getValue(viewItem);
			viewItem.setValue(propertyName, value);
		}

		set.add(viewItem);

		if (viewer != null)
			viewer.refresh();
	}

	protected void removeItem(WatchListViewItem viewItem) {
		Set<WatchListViewItem> set = items.get(viewItem.getSecurity());
		if (set != null) {
			set.remove(viewItem);
			if (set.isEmpty()) {
				pricingEnvironment.removeSecurity(viewItem.getSecurity());
				items.remove(viewItem.getSecurity());

				PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) viewItem.getSecurity().getAdapter(PropertyChangeSupport.class);
				if (propertyChangeSupport != null)
					propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);

				for (WatchListViewColumn viewColumn : columns) {
					String propertyName = viewColumn.getDataProviderFactory().getId();
					viewItem.getDataProvider(propertyName).dispose();
				}
			}
		}

		if (viewer != null)
			viewer.refresh();
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(IWatchList.class))
    		return watchList;

    	if (watchList != null) {
        	Object obj = watchList.getAdapter(adapter);
        	if (obj != null)
        		return obj;
    	}

    	return super.getAdapter(adapter);
    }

	public String getName() {
    	return name;
    }

	public void setName(String name) {
		if (!this.name.equals(name)) {
			this.name = name;
			setPartName(name);
			setDirty();
		}
    }


	public WatchListViewColumn[] getColumns() {
    	return columns.toArray(new WatchListViewColumn[columns.size()]);
    }

	public void setColumns(WatchListViewColumn[] columns) {
		boolean doUpdate = columns.length != this.columns.size();
		if (!doUpdate) {
			for (int i = 0; i < columns.length; i++) {
				if (!columns[i].equals(this.columns.get(i))) {
					doUpdate = true;
					break;
				}
			}
		}

		if (doUpdate) {
			Set<WatchListViewColumn> newColumnsSet = new HashSet<WatchListViewColumn>(Arrays.asList(columns));

			for (Iterator<WatchListViewColumn> iter = this.columns.iterator(); iter.hasNext(); ) {
				WatchListViewColumn column = iter.next();
				if (!newColumnsSet.contains(column)) {
					String propertyName = column.getDataProviderFactory().getId();
					for (Set<WatchListViewItem> set : items.values()) {
						for (WatchListViewItem viewItem : set) {
							viewItem.getDataProvider(propertyName).dispose();
							viewItem.clearDataProvider(propertyName);
							viewItem.clearValue(propertyName);
						}
					}
					iter.remove();
				}
			}

			for (WatchListViewColumn column : columns) {
				if (!this.columns.contains(column)) {
					String propertyName = column.getDataProviderFactory().getId();

					for (Set<WatchListViewItem> set : items.values()) {
						for (WatchListViewItem viewItem : set) {
							IDataProvider dataProvider = column.getDataProviderFactory().createProvider();
							viewItem.setDataProvider(propertyName, dataProvider);

							dataProvider.init(viewItem);

							IAdaptable value = dataProvider.getValue(viewItem);
							viewItem.setValue(propertyName, value);
						}
					}

					this.columns.add(column);
				}
			}

			updateColumns();
			setDirty();

			if (viewer != null)
				viewer.refresh();
		}
	}

	protected void setTickBackground(RGB color) {
		int steps = 100 / (tickEvenRowsFade.length + 1);
		for (int i = 0, ratio = 100 - steps; i < tickEvenRowsFade.length; i++, ratio -= steps) {
			RGB rgb = blend(tickBackgroundColor.getRGB(), evenRowsColor.getRGB(), ratio);
			tickEvenRowsFade[i] = new Color(Display.getDefault(), rgb);
		}

		steps = 100 / (tickOddRowsFade.length + 1);
		for (int i = 0, ratio = 100 - steps; i < tickOddRowsFade.length; i++, ratio -= steps) {
			RGB rgb = blend(tickBackgroundColor.getRGB(), oddRowsColor.getRGB(), ratio);
			tickOddRowsFade[i] = new Color(Display.getDefault(), rgb);
		}
	}

	private RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

    private int blend(int v1, int v2, int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
	}

	Set<WatchListViewItem> getUpdatedItems() {
    	return updatedItems;
    }

	Map<ISecurity, Set<WatchListViewItem>> getItemsMap() {
		return items;
	}
}
