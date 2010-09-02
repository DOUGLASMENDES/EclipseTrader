/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
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
	public static final String VIEW_ID = "org.eclipsetrader.ui.views.orders"; //$NON-NLS-1$
	public static final String K_VISIBLE_COLUMNS = "VISIBLE_COLUMNS"; //$NON-NLS-1$

	private CTabFolder tabFolder;

	private TableViewer all;
	private TableViewer pending;
	private TableViewer filled;
	private TableViewer canceled;
	private TableViewer rejected;
	private ProxySelectionProvider selectionProvider;

	private IMemento memento;
	private IDialogSettings dialogSettings;

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
			} catch (SWTException e) {
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
			} catch (SWTException e) {
				// Do nothing
			}
		}
	};

	private ControlAdapter columnControlListener = new ControlAdapter() {
		@Override
		public void controlResized(ControlEvent e) {
			TableColumn tableColumn = (TableColumn) e.widget;
			int index = tableColumn.getParent().indexOf(tableColumn);

			String[] enabledId = dialogSettings.getArray(K_VISIBLE_COLUMNS);
			memento.putInteger(enabledId[index], tableColumn.getWidth());
		}
	};

	private Action cancelAction;
	private Action columnsAction;

	public OrdersView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		if (memento == null)
			memento = XMLMemento.createWriteRoot("root"); //$NON-NLS-1$
		this.memento = memento;

		IDialogSettings pluginDialogSettings = Activator.getDefault().getDialogSettings();
		dialogSettings = pluginDialogSettings.getSection(VIEW_ID);
		if (dialogSettings == null) {
			dialogSettings = pluginDialogSettings.addNewSection(VIEW_ID);
			dialogSettings.put("VISIBLE_COLUMNS", new String[] { //$NON-NLS-1$
			    OrderIdColumn.COLUMN_ID,
			    DateTimeColumn.COLUMN_ID,
			    SecurityNameColumn.COLUMN_ID,
			    SideColumn.COLUMN_ID,
			    TypeColumn.COLUMN_ID,
			    QuantityColumn.COLUMN_ID,
			    PriceColumn.COLUMN_ID,
			    FilledQuantityColumn.COLUMN_ID,
			    AveragePriceColumn.COLUMN_ID,
			    StatusColumn.COLUMN_ID
			});
		}

		initializeActions();

		IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
		toolbarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolbarManager.add(cancelAction);

		IMenuManager menuManager = site.getActionBars().getMenuManager();
		menuManager.add(new Separator("group.new")); //$NON-NLS-1$
		menuManager.add(new GroupMarker("group.goto")); //$NON-NLS-1$
		menuManager.add(new Separator("group.open")); //$NON-NLS-1$
		menuManager.add(new GroupMarker("group.openWith")); //$NON-NLS-1$
		menuManager.add(new Separator("group.show")); //$NON-NLS-1$
		menuManager.add(new Separator("group.edit")); //$NON-NLS-1$
		menuManager.add(new GroupMarker("group.reorganize")); //$NON-NLS-1$
		menuManager.add(new GroupMarker("group.port")); //$NON-NLS-1$
		menuManager.add(new Separator("group.generate")); //$NON-NLS-1$
		menuManager.add(new Separator("group.search")); //$NON-NLS-1$
		menuManager.add(new Separator("group.build")); //$NON-NLS-1$
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator("group.properties")); //$NON-NLS-1$

		menuManager.appendToGroup("group.properties", columnsAction); //$NON-NLS-1$

		site.getActionBars().updateActionBars();
	}

	void initializeActions() {
		ISharedImages sharedImages = getViewSite().getWorkbenchWindow().getWorkbench().getSharedImages();

		cancelAction = new Action(Messages.OrdersView_CancelAction) {
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
		cancelAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		cancelAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		cancelAction.setEnabled(false);

		columnsAction = new Action(Messages.OrdersView_ColumnsAction) {
			@Override
			public void run() {
				OrdersColumnsDialog dlg = new OrdersColumnsDialog(getSite().getShell());
				dlg.setVisibleId(dialogSettings.getArray(K_VISIBLE_COLUMNS));
				if (dlg.open() == Dialog.OK) {
					dialogSettings.put(K_VISIBLE_COLUMNS, dlg.getVisibleId());
					updateViewerColumns(all, true);
					updateViewerColumns(pending, false);
					updateViewerColumns(filled, false);
					updateViewerColumns(canceled, false);
					updateViewerColumns(rejected, false);
					all.refresh();
					pending.refresh();
					filled.refresh();
					canceled.refresh();
					rejected.refresh();
				}
			}
		};
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
				TableViewer viewer = (TableViewer) ((CTabItem) e.item).getControl().getData("viewer"); //$NON-NLS-1$
				if (viewer != null)
					selectionProvider.setSelectionProvider(viewer);
			}
		});

		selectionProvider = new ProxySelectionProvider();
		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				boolean enabled = false;
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				for (Object o : selection.toList()) {
					if (((IOrderMonitor) o).getStatus() != IOrderStatus.Canceled)
						enabled = true;
				}
				cancelAction.setEnabled(enabled);
			}
		});
		selectionProvider.setSelectionProvider(all);
		getSite().setSelectionProvider(selectionProvider);

		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(new Separator("group.new")); //$NON-NLS-1$
				menuManager.add(new GroupMarker("group.goto")); //$NON-NLS-1$
				menuManager.add(new Separator("group.open")); //$NON-NLS-1$
				menuManager.add(new GroupMarker("group.openWith")); //$NON-NLS-1$
				menuManager.add(new Separator("group.show")); //$NON-NLS-1$
				menuManager.add(new Separator("group.edit")); //$NON-NLS-1$
				menuManager.add(new GroupMarker("group.reorganize")); //$NON-NLS-1$
				menuManager.add(new GroupMarker("group.port")); //$NON-NLS-1$
				menuManager.add(new Separator("group.generate")); //$NON-NLS-1$
				menuManager.add(new Separator("group.search")); //$NON-NLS-1$
				menuManager.add(new Separator("group.build")); //$NON-NLS-1$
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				menuManager.add(new Separator("group.properties")); //$NON-NLS-1$

				menuManager.appendToGroup("group.build", cancelAction); //$NON-NLS-1$
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
		tabItem.setText(Messages.OrdersView_AllTab);
		all = createViewer(tabFolder, true);
		tabItem.setControl(all.getControl());

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.OrdersView_PendingTab);
		pending = createViewer(tabFolder, false);
		pending.setFilters(new ViewerFilter[] {
			new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					IOrderMonitor order = (IOrderMonitor) element;
					return order.getStatus() == IOrderStatus.PendingCancel || order.getStatus() == IOrderStatus.PendingNew;
				}
			}
		});
		tabItem.setControl(pending.getControl());

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.OrdersView_FilledTab);
		filled = createViewer(tabFolder, false);
		filled.setFilters(new ViewerFilter[] {
			new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					IOrderMonitor order = (IOrderMonitor) element;
					return order.getStatus() == IOrderStatus.Filled;
				}
			}
		});
		tabItem.setControl(filled.getControl());

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.OrdersView_CanceledTab);
		canceled = createViewer(tabFolder, false);
		canceled.setFilters(new ViewerFilter[] {
			new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					IOrderMonitor order = (IOrderMonitor) element;
					return order.getStatus() == IOrderStatus.Canceled || order.getStatus() == IOrderStatus.Expired;
				}
			}
		});
		tabItem.setControl(canceled.getControl());

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.OrdersView_RejectedTab);
		rejected = createViewer(tabFolder, false);
		rejected.setFilters(new ViewerFilter[] {
			new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					IOrderMonitor order = (IOrderMonitor) element;
					return order.getStatus() == IOrderStatus.Rejected;
				}
			}
		});
		tabItem.setControl(rejected.getControl());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putMemento(this.memento);
		super.saveState(memento);
	}

	protected TableViewer createViewer(Composite parent, boolean wrapLabelProviders) {
		TableViewer viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
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

		updateViewerColumns(viewer, wrapLabelProviders);

		return viewer;
	}

	IConfigurationElement getConfigurationElement(String targetID) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("org.eclipsetrader.ui.viewLabelProviders"); //$NON-NLS-1$

		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configElements.length; i++) {
			if ("viewContribution".equals(configElements[i].getName())) { //$NON-NLS-1$
				configElements = configElements[i].getChildren();
				for (int j = 0; j < configElements.length; j++) {
					String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (targetID.equals(strID))
						return configElements[j];
				}
				break;
			}
		}
		return null;
	}

	protected void updateViewerColumns(TableViewer viewer, boolean wrapLabelProviders) {
		viewer.getTable().setRedraw(false);
		try {
			TableColumn[] tableColumn = viewer.getTable().getColumns();
			for (int i = 0; i < tableColumn.length; i++)
				tableColumn[i].dispose();

			String[] enabledId = dialogSettings.getArray(K_VISIBLE_COLUMNS);
			for (int i = 0; i < enabledId.length; i++) {
				IConfigurationElement element = getConfigurationElement(enabledId[i]);
				if (element == null)
					continue;

				int style = SWT.LEFT;
				if ("right".equals(element.getAttribute("orientation"))) //$NON-NLS-1$ //$NON-NLS-2$
					style = SWT.RIGHT;
				else if ("center".equals(element.getAttribute("orientation"))) //$NON-NLS-1$ //$NON-NLS-2$
					style = SWT.CENTER;

				TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
				viewerColumn.getColumn().setText(element.getAttribute("name")); //$NON-NLS-1$
				viewerColumn.getColumn().setWidth(memento != null && memento.getString(enabledId[i]) != null ? memento.getInteger(enabledId[i]) : 64);
				viewerColumn.getColumn().addControlListener(columnControlListener);

				try {
					ColumnLabelProvider labelProvider = (ColumnLabelProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
					if (wrapLabelProviders)
						labelProvider = new OrdersLabelProviderWrapper(labelProvider);
					viewerColumn.setLabelProvider(labelProvider);
				} catch (Exception e) {
					Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, Messages.OrdersView_ErrorCreatingLabelProvider +
					                                                                enabledId[i], e);
					Activator.log(status);
				}
			}
		} finally {
			viewer.getTable().setRedraw(true);
		}
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
