/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.repositories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.SelectionProvider;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.views.WatchListView;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RepositoryExplorer extends ViewPart {

    private static final String K_EXPANDED = "expanded";

    private TreeViewer viewer;
    private IMemento memento;

    private Action refreshAction;
    private Action collapseAllAction;
    private Action expandAllAction;

    private Action copyAction;
    private Action pasteAction;
    private Action deleteAction;

    private Job refreshJob = new Job(Messages.RepositoryExplorer_RefreshJobName) {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

            final RepositoryTree tree = viewer.getInput() != null ? (RepositoryTree) viewer.getInput() : new RepositoryTree(getRepositoryService());
            tree.refresh();

            if (!viewer.getControl().isDisposed()) {
                try {
                    viewer.getControl().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                if (viewer.getInput() == null) {
                                    viewer.setInput(tree);
                                    if (memento != null) {
                                        String s = memento.getString("expanded");
                                        if (s != null) {
                                            String[] sr = s.split(";");
                                            final Set<Integer> itemHash = new HashSet<Integer>();
                                            for (int i = 0; i < sr.length; i++) {
                                                try {
                                                    itemHash.add(Integer.parseInt(sr[i]));
                                                } catch (Exception e) {
                                                    // Do nothing
                                                }
                                            }
                                            tree.accept(new IViewVisitor() {

                                                @Override
                                                public boolean visit(IView view) {
                                                    return true;
                                                }

                                                @Override
                                                public boolean visit(IViewItem viewItem) {
                                                    if (itemHash.contains(viewItem.hashCode())) {
                                                        viewer.setExpandedState(viewItem, true);
                                                    }
                                                    return true;
                                                }
                                            });
                                        }
                                    }
                                }
                                else {
                                    viewer.refresh();
                                }
                            }
                        }
                    });
                } catch (SWTException e) {
                    if (e.code != SWT.ERROR_DEVICE_DISPOSED) {
                        throw e;
                    }
                }
            }

            monitor.done();

            return Status.OK_STATUS;
        }
    };

    private IRepositoryChangeListener resourceListener = new IRepositoryChangeListener() {

        @Override
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
            try {
                refreshJob.join();
            } catch (Exception e) {
            }
            refreshJob.schedule();
        }
    };

    public RepositoryExplorer() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;

        ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();

        site.setSelectionProvider(new SelectionProvider());

        refreshAction = new Action(Messages.RepositoryExplorer_Refresh, imageRegistry.getDescriptor(UIConstants.REFRESH_ICON)) {

            @Override
            public void run() {
                refreshJob.setUser(true);
                refreshJob.schedule();
            }
        };

        collapseAllAction = new Action(Messages.RepositoryExplorer_CollapseAll, imageRegistry.getDescriptor(UIConstants.COLLAPSEALL_ICON)) {

            @Override
            public void run() {
                viewer.collapseAll();
            }
        };

        expandAllAction = new Action(Messages.RepositoryExplorer_ExpandAll, imageRegistry.getDescriptor(UIConstants.EXPANDALL_ICON)) {

            @Override
            public void run() {
                viewer.expandAll();
            }
        };

        copyAction = new Action(Messages.RepositoryExplorer_Copy) {

            @Override
            public void run() {
                IAdaptable[] objects = getSelectedObject(viewer.getSelection());
                if (objects.length != 0) {
                    Transfer[] transfer = new Transfer[objects.length];
                    for (int i = 0; i < transfer.length; i++) {
                        transfer[i] = RepositoryObjectTransfer.getInstance();
                    }

                    Clipboard clipboard = new Clipboard(Display.getDefault());
                    try {
                        clipboard.setContents(new Object[] {
                            objects
                        }, new Transfer[] {
                            RepositoryObjectTransfer.getInstance()
                        });
                    } finally {
                        clipboard.dispose();
                    }
                }
            }
        };
        copyAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.COPY_EDIT_ICON));
        copyAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.COPY_EDIT_DISABLED_ICON));
        copyAction.setId(ActionFactory.COPY.getId());
        copyAction.setActionDefinitionId("org.eclipse.ui.edit.copy"); //$NON-NLS-1$
        copyAction.setEnabled(false);

        pasteAction = new Action(Messages.RepositoryExplorer_Paste) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (!selection.isEmpty()) {
                    RepositoryViewItem element = (RepositoryViewItem) selection.getFirstElement();
                    while (element.getParent() != null && !(element.getObject() instanceof IRepository)) {
                        element = (RepositoryViewItem) element.getParent();
                    }
                    IRepository destinationRepository = (IRepository) element.getObject();

                    Clipboard clipboard = new Clipboard(Display.getDefault());
                    try {
                        IAdaptable[] contents = (IAdaptable[]) clipboard.getContents(RepositoryObjectTransfer.getInstance());
                        if (contents.length != 0) {
                            IRepositoryService service = getRepositoryService();
                            RepositoryMoveJob job = new RepositoryMoveJob(service, contents, destinationRepository);
                            job.schedule();
                        }
                    } finally {
                        clipboard.dispose();
                    }
                }
            }
        };
        pasteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.PASTE_EDIT_ICON));
        pasteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.PASTE_EDIT_DISABLED_ICON));
        pasteAction.setId(ActionFactory.PASTE.getId());
        pasteAction.setActionDefinitionId("org.eclipse.ui.edit.paste"); //$NON-NLS-1$
        pasteAction.setEnabled(false);

        deleteAction = new Action(Messages.RepositoryExplorer_Delete) {

            @Override
            public void run() {
                final IAdaptable[] objects = getSelectedObject(viewer.getSelection());
                if (objects.length != 0) {
                    if (!MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), Messages.RepositoryExplorer_DeleteConfirmMessage)) {
                        return;
                    }
                    final IRepositoryService service = getRepositoryService();
                    service.runInService(new IRepositoryRunnable() {

                        @Override
                        public IStatus run(IProgressMonitor monitor) throws Exception {
                            service.deleteAdaptable(objects);
                            return Status.OK_STATUS;
                        }
                    }, null);
                }
            }
        };
        deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_ICON));
        deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_DISABLED_ICON));
        deleteAction.setId(ActionFactory.DELETE.getId());
        deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
        deleteAction.setEnabled(false);

        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(expandAllAction);
        toolBarManager.add(collapseAllAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(refreshAction);

        site.getActionBars().setGlobalActionHandler(copyAction.getId(), copyAction);
        site.getActionBars().setGlobalActionHandler(pasteAction.getId(), pasteAction);
        site.getActionBars().setGlobalActionHandler(deleteAction.getId(), deleteAction);

        site.getActionBars().updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI);
        viewer.getTree().setHeaderVisible(false);
        viewer.getTree().setLinesVisible(false);
        viewer.setLabelProvider(new RepositoryLabelProvider());
        viewer.setContentProvider(new RepositoryContentProvider());
        viewer.setSorter(new ViewerSorter());

        viewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (selection.size() == 1) {
                    RepositoryViewItem element = (RepositoryViewItem) selection.getFirstElement();

                    if (element.getObject() instanceof IWatchList) {
                        try {
                            IStoreObject storeObject = (IStoreObject) ((IWatchList) element.getObject()).getAdapter(IStoreObject.class);
                            IDialogSettings dialogSettings = UIActivator.getDefault().getDialogSettingsForView(storeObject.getStore().toURI());
                            getViewSite().getPage().showView(WatchListView.VIEW_ID, dialogSettings.getName(), IWorkbenchPage.VIEW_ACTIVATE);
                        } catch (PartInitException e) {
                            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error opening watchlist view", e); //$NON-NLS-1$
                            UIActivator.getDefault().getLog().log(status);
                        }
                        return;
                    }

                    if (viewer.getExpandedState(element)) {
                        viewer.collapseToLevel(element, 1);
                    }
                    else {
                        viewer.expandToLevel(element, 1);
                    }
                }
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IAdaptable[] objects = getSelectedObject(event.getSelection());
                copyAction.setEnabled(objects.length != 0);
                deleteAction.setEnabled(objects.length != 0);
                getViewSite().getSelectionProvider().setSelection(event.getSelection());
            }
        });

        viewer.addOpenListener(new IOpenListener() {

            @Override
            public void open(OpenEvent event) {
                try {
                    IHandlerService service = (IHandlerService) getSite().getService(IHandlerService.class);
                    service.executeCommand("org.eclipse.ui.file.open", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        DragSource dragSource = new DragSource(viewer.getControl(), DND.DROP_COPY | DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] {
            RepositoryObjectTransfer.getInstance()
        });
        dragSource.addDragListener(new DragSourceListener() {

            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = getSelectedObject(viewer.getSelection()).length != 0;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                if (RepositoryObjectTransfer.getInstance().isSupportedType(event.dataType)) {
                    event.data = getSelectedObject(viewer.getSelection());
                }
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
            }
        });

        viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {
            RepositoryObjectTransfer.getInstance()
        }, new ViewerDropAdapter(viewer) {

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
             */
            @Override
            public boolean validateDrop(Object target, int operation, TransferData transferType) {
                return RepositoryObjectTransfer.getInstance().isSupportedType(transferType);
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
             */
            @Override
            public boolean performDrop(Object data) {
                RepositoryViewItem element = (RepositoryViewItem) getCurrentTarget();
                while (element.getParent() != null && !(element.getObject() instanceof IRepository)) {
                    element = (RepositoryViewItem) element.getParent();
                }
                IRepository destinationRepository = (IRepository) element.getObject();

                IAdaptable[] contents = (IAdaptable[]) data;
                if (contents.length != 0) {
                    IRepositoryService service = getRepositoryService();
                    RepositoryMoveJob job = new RepositoryMoveJob(service, contents, destinationRepository);
                    job.schedule();
                }

                return true;
            }
        });

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
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

                menuManager.appendToGroup("group.show", new Action(Messages.RepositoryExplorer_ExpandAll) {

                    @Override
                    public void run() {
                        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                        for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                            viewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
                        }
                    }
                });
                menuManager.appendToGroup("group.edit", copyAction);
                menuManager.appendToGroup("group.edit", pasteAction);
                menuManager.appendToGroup("group.reorganize", deleteAction);

                boolean enablePaste = false;
                Clipboard clipboard = new Clipboard(Display.getDefault());
                try {
                    TransferData[] transfers = clipboard.getAvailableTypes();
                    for (int i = 0; i < transfers.length; i++) {
                        if (RepositoryObjectTransfer.getInstance().isSupportedType(transfers[i])) {
                            enablePaste = true;
                        }
                    }
                } finally {
                    clipboard.dispose();
                }
                pasteAction.setEnabled(enablePaste);
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        refreshJob.setUser(true);
        refreshJob.schedule();
        getRepositoryService().addRepositoryResourceListener(resourceListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento memento) {
        Object[] o = viewer.getExpandedElements();
        if (o != null && o.length != 0) {
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < o.length; i++) {
                if (i != 0) {
                    s.append(";");
                }
                s.append(o[i].hashCode());
            }
            memento.putString(K_EXPANDED, s.toString());
        }

        super.saveState(memento);
    }

    @Override
    public void dispose() {
        getRepositoryService().removeRepositoryResourceListener(resourceListener);
        super.dispose();
    }

    protected IRepositoryService getRepositoryService() {
        try {
            BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
            IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
            context.ungetService(serviceReference);
            return service;
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading repository service", e); //$NON-NLS-1$
            UIActivator.getDefault().getLog().log(status);
        }
        return null;
    }

    protected IAdaptable[] getSelectedObject(ISelection selection) {
        List<IAdaptable> list = new ArrayList<IAdaptable>();

        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            for (Object o : ((IStructuredSelection) selection).toArray()) {
                if (!(((RepositoryViewItem) o).getObject() instanceof IAdaptable)) {
                    return new IAdaptable[0];
                }
                IAdaptable adaptable = (IAdaptable) ((RepositoryViewItem) o).getObject();
                if (RepositoryObjectTransfer.checkMyType(adaptable)) {
                    list.add(adaptable);
                }
            }
        }

        return list.toArray(new IAdaptable[list.size()]);
    }
}
