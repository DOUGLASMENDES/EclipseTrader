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
import java.util.Date;
import java.util.List;

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
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.views.WatchList;
import org.eclipsetrader.core.internal.views.WatchListView;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.repositories.RepositoryObjectTransfer;
import org.eclipsetrader.ui.internal.securities.SecurityObjectTransfer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WatchListViewer extends ViewPart implements ISaveablePart {
	public static final String VIEW_ID = "org.eclipsetrader.ui.views.watchlist";
	public static final String K_VIEWS = "Views";
	public static final String K_URI = "uri";

	private URI uri;
	private IWatchList view;
	private WatchListView activeView;

	private Action deleteAction;
	private Action settingsAction;

	private TableViewer viewer;
	private boolean dirty = false;

	private IDialogSettings dialogSettings;
	private int sortColumn = 0;
	private int sortDirection = SWT.UP;

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

        	IColumn column = (IColumn) tableColumn.getData();
            dialogSettings.put("sortColumn", column.getDataProviderFactory().getId());
            dialogSettings.put("sortDirection", sortDirection == SWT.UP ? 1 : -1);
            viewer.refresh();
        }
	};

	private IRepositoryChangeListener repositoryListener = new IRepositoryChangeListener() {
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
        	for (RepositoryResourceDelta delta : event.getDeltas()) {
        		if (delta.getResource() == view) {
        		    IStoreObject objectStore = (IStoreObject) ((IAdaptable) delta.getResource()).getAdapter(IStoreObject.class);
        		    dialogSettings.put(K_URI, objectStore.getStore().toURI().toString());
        			break;
        		}
        	}
        }
	};

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
        	try {
            	if (!viewer.getControl().isDisposed()) {
            		viewer.getControl().getDisplay().asyncExec(new Runnable() {
            			public void run() {
                        	if (!viewer.getControl().isDisposed()) {
                	        	if (IWatchList.NAME.equals(evt.getPropertyName())) {
                	        		setPartName(activeView.getName());
                	        		if (evt.getSource() == activeView)
                	        			setDirty();
                	        	}
                	        	else if (IWatchList.HOLDINGS.equals(evt.getPropertyName())) {
                	        		viewer.refresh();
                	        		if (evt.getSource() == activeView)
                	        			setDirty();
                	        	}
                	        	else if (IWatchList.COLUMNS.equals(evt.getPropertyName())) {
                	        		updateColumns(activeView.getColumns());
                	        		viewer.refresh();
                	        		if (evt.getSource() == activeView)
                	        			setDirty();
                	        	}
                        	}
            			}
            		});
            	}
        	} catch(SWTException e) {
        		// Do nothing, viewer may be disposed
        	}
        }
	};

	public WatchListViewer() {
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
	        view = UIActivator.getDefault().getRepositoryService().getWatchListFromURI(uri);
        } catch (URISyntaxException e) {
        	Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, "Error loading view " + site.getSecondaryId(), e);
	        UIActivator.getDefault().getLog().log(status);
        }

		deleteAction = new Action("Delete") {
			@Override
			public void run() {
				Object[] s = ((IStructuredSelection) viewer.getSelection()).toArray();
				if (s.length != 0) {
					IWatchListElement[] selection = new IWatchListElement[s.length];
					for (int i = 0; i < selection.length; i++)
						selection[i] = (IWatchListElement) ((IViewItem) s[i]).getAdapter(IWatchListElement.class);
					activeView.removeElements(selection);
					setDirty();
				}
			}
		};
		deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_ICON));
		deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_DISABLED_ICON));
		deleteAction.setId(ActionFactory.DELETE.getId());
		deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
		deleteAction.setEnabled(false);

		//IMenuManager menuManager = site.getActionBars().getMenuManager();

		site.getActionBars().setGlobalActionHandler(deleteAction.getId(), deleteAction);
		site.getActionBars().updateActionBars();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
    @Override
	public void createPartControl(Composite parent) {
    	if (view == null) {
    		Composite composite = new Composite(parent, SWT.NONE);
    		composite.setLayout(new GridLayout(1, false));
    		Label label = new Label(composite, SWT.NONE);
    		label.setText(NLS.bind("Unable to load view {0}", new Object[] { uri != null ? uri.toString() : getViewSite().getSecondaryId() }));
    		return;
    	}

    	setPartName(view.getName());

		activeView = (WatchListView) ((WatchList) view).getView();
		createContents(parent);

		settingsAction = new SettingsAction(getViewSite().getShell(), activeView);
		getViewSite().getActionBars().setGlobalActionHandler(settingsAction.getId(), settingsAction);
		getViewSite().getActionBars().updateActionBars();

		getRepositoryService().addRepositoryResourceListener(repositoryListener);

		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) activeView.getAdapter(PropertyChangeSupport.class);
		if (propertyChangeSupport != null)
			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);

		propertyChangeSupport = (PropertyChangeSupport) view.getAdapter(PropertyChangeSupport.class);
		if (propertyChangeSupport != null)
			propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
	}

    protected void createContents(Composite parent) {
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
						List<ISecurity> list = new ArrayList<ISecurity>();
						for (int i = 0; i < contents.length; i++) {
							ISecurity security = (ISecurity) contents[i].getAdapter(ISecurity.class);
							if (security != null)
								list.add(security);
						}
						if (list.size() != 0) {
							activeView.addSecurities(list.toArray(new ISecurity[list.size()]));
							setDirty();
						}
						return true;
                    }
				});

		updateColumns(activeView.getColumns());

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

		getViewSite().setSelectionProvider(viewer);

		viewer.setInput(activeView);
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
    	if (activeView != null) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) activeView.getAdapter(PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    	}

    	if (view != null) {
    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) view.getAdapter(PropertyChangeSupport.class);
    		if (propertyChangeSupport != null)
    			propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    	}

		getRepositoryService().removeRepositoryResourceListener(repositoryListener);

		if (activeView != null)
    		activeView.dispose();

		super.dispose();
    }

	protected StructuredViewer getViewer() {
    	return viewer;
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
    	final IRepositoryService service = getRepositoryService();
		service.runInService(new IRepositoryRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
            	activeView.synchronize();
            	service.saveAdaptable(new IWatchList[] { view });
            	dirty = false;
            	firePropertyChange(PROP_DIRTY);
	            return Status.OK_STATUS;
            }
		}, monitor);
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
	    		getTable().setSortDirection(sortDirection);
	    		getTable().setSortColumn(getTable().getColumn(sortColumn));
            }
		};
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		viewer.setUseHashlookup(true);

		viewer.setLabelProvider(new ViewItemLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	IAdaptable[] v1 = ((IViewItem) e1).getValues();
            	IAdaptable[] v2 = ((IViewItem) e2).getValues();
            	if (sortDirection == SWT.DOWN) {
                	v1 = ((IViewItem) e2).getValues();
                	v2 = ((IViewItem) e1).getValues();
            	}
            	return compareValues(v1, v2, sortColumn);
            }
		});

		return viewer;
	}

    @SuppressWarnings("unchecked")
    protected int compareValues(IAdaptable[] v1, IAdaptable[] v2, int sortColumn) {
    	if (sortColumn < 0 || sortColumn >= v1.length || sortColumn >= v2.length)
    		return 0;
    	if (v1[sortColumn] == null || v2[sortColumn] == null)
    		return 0;

    	Object o1 = v1[sortColumn].getAdapter(Comparable.class);
    	Object o2 = v2[sortColumn].getAdapter(Comparable.class);
    	if (o1 != null && o2 != null)
    		return ((Comparable) o1).compareTo(o2);

    	o1 = v1[sortColumn].getAdapter(Number.class);
    	o2 = v2[sortColumn].getAdapter(Number.class);
    	if (o1 != null && o2 != null) {
    		if (((Number) o1).doubleValue() < ((Number) o2).doubleValue())
    			return -1;
    		if (((Number) o1).doubleValue() > ((Number) o2).doubleValue())
    			return 1;
    		return 0;
    	}

    	return 0;
    }

	public void updateColumns(IColumn[] columns) {
		String[] properties = createColumns(viewer.getTable(), columns);
		viewer.setColumnProperties(properties);
	}

    @SuppressWarnings("unchecked")
	protected String[] createColumns(Table table, IColumn[] columns) {
		TableColumnLayout tableLayout = (TableColumnLayout) table.getParent().getLayout();
		IDialogSettings columnsSection = dialogSettings != null ? dialogSettings.getSection("columns") : null;

		String[] properties = new String[columns.length];

		int index = 0;
		for (IColumn column : columns) {
			int alignment = SWT.LEFT;
			if (column.getDataProviderFactory().getType() != null && column.getDataProviderFactory().getType().length != 0) {
				Class type = column.getDataProviderFactory().getType()[0];
				if (type == Long.class || type == Double.class || type == Date.class)
					alignment = SWT.RIGHT;
			}
			TableColumn tableColumn = index < table.getColumnCount() ? table.getColumn(index) : new TableColumn(table, SWT.NONE);
			tableColumn.setAlignment(alignment);
			tableColumn.setText(column.getName() != null ? column.getName() : column.getDataProviderFactory().getName());
			tableColumn.setData(column);

			properties[index] = String.valueOf(index);

			if ("org.eclipsetrader.ui.providers.SecurityName".equals(column.getDataProviderFactory().getId()))
				tableLayout.setColumnData(tableColumn, new ColumnWeightData(100));
			else {
				int width = columnsSection != null && columnsSection.get(tableColumn.getText()) != null ? columnsSection.getInt(tableColumn.getText()) : 100;
				tableLayout.setColumnData(tableColumn, new ColumnPixelData(width));
			}

			tableColumn.addControlListener(columnControlListener);
			tableColumn.addSelectionListener(columnSelectionAdapter);

			if (dialogSettings != null) {
				if (column.getDataProviderFactory().getId().equals(dialogSettings.get("sortColumn")))
					sortColumn = index;
			}

			index++;
		}

		while(table.getColumnCount() > index)
			table.getColumn(table.getColumnCount() - 1).dispose();

		return properties;
	}

	protected IRepositoryService getRepositoryService() {
		BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
		context.ungetService(serviceReference);
		return service;
	}

    Table getTable() {
    	return viewer.getTable();
    }
}
