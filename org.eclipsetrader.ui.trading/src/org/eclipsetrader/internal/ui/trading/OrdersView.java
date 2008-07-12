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

package org.eclipsetrader.internal.ui.trading;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.core.trading.OrderStatus;
import org.eclipsetrader.ui.trading.AveragePriceColumn;
import org.eclipsetrader.ui.trading.DateTimeColumn;
import org.eclipsetrader.ui.trading.FilledQuantityColumn;
import org.eclipsetrader.ui.trading.OrderIdColumn;
import org.eclipsetrader.ui.trading.OrdersLabelProviderWrapper;
import org.eclipsetrader.ui.trading.PriceColumn;
import org.eclipsetrader.ui.trading.QuantityColumn;
import org.eclipsetrader.ui.trading.SecurityNameColumn;
import org.eclipsetrader.ui.trading.SideColumn;
import org.eclipsetrader.ui.trading.StatusColumn;
import org.eclipsetrader.ui.trading.TypeColumn;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class OrdersView extends ViewPart {
    public static final String VIEW_ID = "org.eclipsetrader.ui.views.orders";

    private CTabFolder tabFolder;

    private TableViewer all;
    private TableViewer pending;
    private TableViewer filled;
    private TableViewer canceled;
    private TableViewer rejected;
    private ProxySelectionProvider selectionProvider;

    private IMemento memento;

    private ServiceReference serviceReference;
    private ITradingService service;

    private IOrderChangeListener orderChangedListener = new IOrderChangeListener() {
        public void orderChanged(OrderChangeEvent event) {
        	IAdapterManager adapterManager = Platform.getAdapterManager();

        	for (OrderDelta delta : event.deltas) {
        		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) adapterManager.getAdapter(delta.getOrder(), PropertyChangeSupport.class);
        		if (propertyChangeSupport != null) {
            		if (delta.getKind() == OrderDelta.KIND_ADDED) {
            			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
            		}
            		else if (delta.getKind() == OrderDelta.KIND_REMOVED)
            			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
        		}
        	}

        	try {
    			tabFolder.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                    	if (!tabFolder.isDisposed()) {
                			all.refresh();
                		    pending.refresh();
                		    filled.refresh();
                		    canceled.refresh();
                		    rejected.refresh();
                    	}
                    }
    			});
    		} catch(SWTException e) {
    			// Do nothing
    		}
        }
    };

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
    		try {
    			tabFolder.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                    	if (!tabFolder.isDisposed()) {
                			all.refresh();
                		    pending.refresh();
                		    filled.refresh();
                		    canceled.refresh();
                		    rejected.refresh();
                    	}
                    }
    			});
    		} catch(SWTException e) {
    			// Do nothing
    		}
        }
	};

	private Action cancelAction = new Action("Cancel") {
        @Override
        public void run() {
	        IStructuredSelection selection = (IStructuredSelection) getSite().getSelectionProvider().getSelection();
	        if (!selection.isEmpty()) {
	        	for (Object o : selection.toList()) {
	        		try {
	                    ((IOrderMonitor) o).cancel();
                    } catch (BrokerException e) {
	                    e.printStackTrace();
                    }
	        	}
	        }
        }
	};

	public OrdersView() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);
	    this.memento = memento;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
	    createTabFolder(parent);

	    tabFolder.setSelection(tabFolder.getItem(0));
	    tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	Control c = ((CTabItem) e.item).getControl();
	            TableViewer viewer = (TableViewer) c.getData("viewer");
	            if (viewer != null)
	        	    selectionProvider.setSelectionProvider(viewer);
            }
	    });

	    selectionProvider = new ProxySelectionProvider();
	    selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	cancelAction.setEnabled(!event.getSelection().isEmpty());
            }
	    });
	    selectionProvider.setSelectionProvider(all);
	    getSite().setSelectionProvider(selectionProvider);

		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(new Separator("group.new"));
				menuManager.add(new GroupMarker("group.goto"));
				menuManager.add(new Separator("group.open"));
				menuManager.add(new GroupMarker("group.openWith"));
				menuManager.add(new Separator("group.show"));
				menuManager.add(new Separator("group.edit"));
				menuManager.add(new GroupMarker("group.reorganize"));
				menuManager.add(new GroupMarker("group.port"));
				menuManager.add(new Separator("group.generate"));
				menuManager.add(new Separator("group.search"));
				menuManager.add(new Separator("group.build"));
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				menuManager.add(new Separator("group.properties"));

				menuManager.appendToGroup("group.build", cancelAction);
			}
		});

		all.getControl().setMenu(menuMgr.createContextMenu(all.getControl()));
		pending.getControl().setMenu(menuMgr.createContextMenu(pending.getControl()));
		filled.getControl().setMenu(menuMgr.createContextMenu(filled.getControl()));
		canceled.getControl().setMenu(menuMgr.createContextMenu(canceled.getControl()));
		rejected.getControl().setMenu(menuMgr.createContextMenu(rejected.getControl()));

		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

	    if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			serviceReference = context.getServiceReference(ITradingService.class.getName());
			if (serviceReference != null) {
				service = (ITradingService) context.getService(serviceReference);

				all.setInput(service);
			    pending.setInput(service);
			    filled.setInput(service);
			    canceled.setInput(service);
			    rejected.setInput(service);

			    hookListeners(service.getOrders());
			    service.addOrderChangeListener(orderChangedListener);
			}
	    }
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
    	if (service != null) {
    		service.removeOrderChangeListener(orderChangedListener);
    		unhookListeners(service.getOrders());
    	}

    	if (serviceReference != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			context.ungetService(serviceReference);
    	}

    	super.dispose();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		tabFolder.setFocus();
	}

	protected void createTabFolder(Composite parent) {
        tabFolder = new CTabFolder(parent, SWT.BOTTOM);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setText("All");
		all = createViewer(tabFolder, true);
        tabItem.setControl(all.getControl().getParent());

        tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setText("Pending");
		pending = createViewer(tabFolder, false);
		pending.setFilters(new ViewerFilter[] {
				new ViewerFilter() {
                    @Override
                    public boolean select(Viewer viewer, Object parentElement, Object element) {
                    	IOrderMonitor order = (IOrderMonitor) element;
	                    return order.getStatus() == OrderStatus.PendingCancel ||
	                           order.getStatus() == OrderStatus.PendingNew;
                    }
				}
			});
        tabItem.setControl(pending.getControl().getParent());

        tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setText("Filled");
		filled = createViewer(tabFolder, false);
		filled.setFilters(new ViewerFilter[] {
				new ViewerFilter() {
                    @Override
                    public boolean select(Viewer viewer, Object parentElement, Object element) {
                    	IOrderMonitor order = (IOrderMonitor) element;
	                    return order.getStatus() == OrderStatus.Filled;
                    }
				}
			});
        tabItem.setControl(filled.getControl().getParent());

        tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setText("Canceled");
		canceled = createViewer(tabFolder, false);
		canceled.setFilters(new ViewerFilter[] {
				new ViewerFilter() {
                    @Override
                    public boolean select(Viewer viewer, Object parentElement, Object element) {
                    	IOrderMonitor order = (IOrderMonitor) element;
	                    return order.getStatus() == OrderStatus.Canceled ||
	                           order.getStatus() == OrderStatus.Expired;
                    }
				}
			});
        tabItem.setControl(canceled.getControl().getParent());

        tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setText("Rejected");
		rejected = createViewer(tabFolder, false);
		rejected.setFilters(new ViewerFilter[] {
				new ViewerFilter() {
                    @Override
                    public boolean select(Viewer viewer, Object parentElement, Object element) {
                    	IOrderMonitor order = (IOrderMonitor) element;
	                    return order.getStatus() == OrderStatus.Rejected;
                    }
				}
			});
        tabItem.setControl(rejected.getControl().getParent());
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento memento) {
	    super.saveState(memento);
    }

	protected TableViewer createViewer(Composite parent, boolean wrapLabelProviders) {
		Composite content = new Composite(parent, SWT.NONE);
		TableColumnLayout layout = new TableColumnLayout();
		content.setLayout(layout);

		TableViewer viewer = new TableViewer(content, SWT.FULL_SELECTION | SWT.MULTI);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
	            return ((ITradingService) inputElement).getOrders();
            }
		});
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	IOrder o1 = ((IOrderMonitor) e1).getOrder();
            	IOrder o2 = ((IOrderMonitor) e2).getOrder();
	            return o2.getDate().compareTo(o1.getDate());
            }
		});
		content.setData("viewer", viewer);

		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Id");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("ID") != null ? memento.getInteger("ID") : 115));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new OrderIdColumn()) : new OrderIdColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Date");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("DATE") != null ? memento.getInteger("DATE") : 140));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new DateTimeColumn()) : new DateTimeColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Security");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("INSTRUMENT") != null ? memento.getInteger("INSTRUMENT") : 150));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new SecurityNameColumn()) : new SecurityNameColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Side");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("SIDE") != null ? memento.getInteger("SIDE") : 60));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new SideColumn()) : new SideColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Type");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("TYPE") != null ? memento.getInteger("TYPE") : 60));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new TypeColumn()) : new TypeColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
		viewerColumn.getColumn().setText("Q.ty");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("QTY") != null ? memento.getInteger("QTY") : 70));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new QuantityColumn()) : new QuantityColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
		viewerColumn.getColumn().setText("Price");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("PRICE") != null ? memento.getInteger("PRICE") : 70));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new PriceColumn()) : new PriceColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
		viewerColumn.getColumn().setText("Filled Q.ty");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("F-QTY") != null ? memento.getInteger("F-QTY") : 70));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new FilledQuantityColumn()) : new FilledQuantityColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
		viewerColumn.getColumn().setText("Avg. Price");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(memento != null && memento.getString("AVG-PRICE") != null ? memento.getInteger("AVG-PRICE") : 70));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new AveragePriceColumn()) : new AveragePriceColumn());

		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Status");
		layout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(100));
		viewerColumn.setLabelProvider(wrapLabelProviders ? new OrdersLabelProviderWrapper(new StatusColumn()) : new StatusColumn());

		return viewer;
	}

    protected void hookListeners(IOrderMonitor[] order) {
    	IAdapterManager adapterManager = Platform.getAdapterManager();
    	for (int i = 0; i < order.length; i++) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) adapterManager.getAdapter(order[i], PropertyChangeSupport.class);
    		if (propertyChangeSupport != null) {
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    		}
    	}
    }

    protected void unhookListeners(IOrderMonitor[] order) {
    	IAdapterManager adapterManager = Platform.getAdapterManager();
    	for (int i = 0; i < order.length; i++) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) adapterManager.getAdapter(order[i], PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    	}
    }
}
