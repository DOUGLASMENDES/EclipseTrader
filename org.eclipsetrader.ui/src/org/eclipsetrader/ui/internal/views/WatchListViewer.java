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
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
	public static final String K_PRESENTATION = "presentation";

	private URI uri;
	private IWatchList view;
	private WatchListView activeView;

	private Action deleteAction;
	private Action tableLayoutAction;
	private Action ribbonLayoutAction;
	private Action settingsAction;

	private Composite parent;
	private IWatchListViewerPresentation presentation;
	private StructuredViewer viewer;
	private boolean dirty = false;

	private IDialogSettings dialogSettings;

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
                	        		presentation.updateColumns(activeView.getColumns());
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

		tableLayoutAction = new Action("Table", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				dialogSettings.put(K_PRESENTATION, TablePresentation.class.getName());
				presentation.dispose();
				createContents(parent);
				parent.layout();
			}
		};
		ribbonLayoutAction = new Action("Ribbon", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				dialogSettings.put(K_PRESENTATION, RibbonPresentation.class.getName());
				presentation.dispose();
				createContents(parent);
				parent.layout();
			}
		};

		IMenuManager menuManager = site.getActionBars().getMenuManager();
        IMenuManager layoutMenu = new MenuManager("Layout", "layout");
        layoutMenu.add(tableLayoutAction);
        layoutMenu.add(ribbonLayoutAction);
        menuManager.add(layoutMenu);

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
		this.parent = parent;

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
		try {
	        String clazz = dialogSettings.get(K_PRESENTATION);
	        Constructor<?> c = Class.forName(clazz).getConstructor(Composite.class, IDialogSettings.class);
	        presentation = (IWatchListViewerPresentation) c.newInstance(parent, dialogSettings);
        } catch (Exception e) {
   			presentation = new TablePresentation(parent, dialogSettings);
        }

        tableLayoutAction.setChecked(presentation instanceof TablePresentation);
        ribbonLayoutAction.setChecked(presentation instanceof RibbonPresentation);

		viewer = presentation.getViewer();
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

		presentation.updateColumns(activeView.getColumns());

		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(deleteAction);
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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

	protected IRepositoryService getRepositoryService() {
		BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
		context.ungetService(serviceReference);
		return service;
	}
}
