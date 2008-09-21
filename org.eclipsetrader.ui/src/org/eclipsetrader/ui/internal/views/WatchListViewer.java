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
import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.WatchList;
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
	private WatchList view;
	private WatchListView activeView;

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

        	WatchListViewColumn column = activeView.getColumn(sortColumn);
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
        	/*try {
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
        	}*/
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
	        IWatchList watchList = UIActivator.getDefault().getRepositoryService().getWatchListFromURI(uri);
	        if (watchList instanceof WatchList)
	        	view = (WatchList) watchList;
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
						activeView.removeItem((WatchListViewItem) s[i]);
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

		setTickBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB());

		createContents(parent);

		activeView = new WatchListView(view, this);
    	setPartName(activeView.getName());
		updateColumns(activeView.getColumns());
		viewer.setInput(activeView);

		settingsAction = new SettingsAction(getViewSite().getShell(), activeView);
		getViewSite().getActionBars().setGlobalActionHandler(settingsAction.getId(), settingsAction);
		getViewSite().getActionBars().updateActionBars();

		getRepositoryService().addRepositoryResourceListener(repositoryListener);

		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) view.getAdapter(PropertyChangeSupport.class);
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
						for (int i = 0; i < contents.length; i++) {
							ISecurity security = (ISecurity) contents[i].getAdapter(ISecurity.class);
							if (security != null) {
								activeView.addItem(security);
								setDirty();
							}
						}
						return true;
                    }
				});

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

	protected TableViewer getViewer() {
    	return viewer;
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
    	activeView.doSave(monitor);
    	dirty = false;
    	firePropertyChange(PROP_DIRTY);
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

		viewer.setContentProvider(new WatchListViewContentProvider());
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	if (sortColumn < 0 || sortColumn >= activeView.getColumns().length)
            		return 0;
            	String propertyName = activeView.getColumns()[sortColumn].getDataProviderFactory().getId();
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

	protected void updateColumns(WatchListViewColumn[] columns) {
		String[] properties = createColumns(viewer, columns);
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
				}

				TableViewerColumn viewerColumn = new TableViewerColumn(viewer, alignment);
				viewerColumn.getColumn().setText(column.getName() != null ? column.getName() : column.getDataProviderFactory().getName());

				WatchListColumnLabelProvider labelProvider = new WatchListColumnLabelProvider(column);
				labelProvider.setColors(evenRowsColor, oddRowsColor, tickBackgroundColor, tickEvenRowsFade, tickOddRowsFade);
				viewerColumn.setLabelProvider(labelProvider);

				properties[index] = column.getDataProviderFactory().getId();

				if ("org.eclipsetrader.ui.providers.SecurityName".equals(column.getDataProviderFactory().getId()))
					tableLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(100));
				else {
					int width = columnsSection != null && columnsSection.get(viewerColumn.getColumn().getText()) != null ? columnsSection.getInt(viewerColumn.getColumn().getText()) : 100;
					tableLayout.setColumnData(viewerColumn.getColumn(), new ColumnPixelData(width));
				}

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

	public void setTickBackground(RGB color) {
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
}
