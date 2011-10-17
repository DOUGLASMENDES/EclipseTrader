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

package org.eclipsetrader.ui.internal.ats.explorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.ats.explorer.ExplorerViewModel.InstrumentRootItem;
import org.eclipsetrader.ui.internal.ats.explorer.ExplorerViewModel.ScriptRootItem;
import org.eclipsetrader.ui.navigator.RepositoryObjectTransfer;
import org.eclipsetrader.ui.navigator.SecurityObjectTransfer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ExplorerViewPart extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.ats.views.explorer";

    private IRepositoryService repositoryService;
    private TreeViewer viewer;
    private ExplorerViewModel model;

    private Action collapseAllAction;
    private Action expandAllAction;
    private Action deleteAction;
    private Action removeAction;

    public ExplorerViewPart() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        repositoryService = bundleContext.getService(serviceReference);

        ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();

        collapseAllAction = new Action("Collapse All", imageRegistry.getDescriptor(UIConstants.COLLAPSEALL_ICON)) {

            @Override
            public void run() {
                viewer.collapseAll();
            }
        };

        expandAllAction = new Action("Expand All", imageRegistry.getDescriptor(UIConstants.EXPANDALL_ICON)) {

            @Override
            public void run() {
                viewer.expandAll();
            }
        };

        deleteAction = new Action("Delete") {

            @Override
            public void run() {
                if (viewer.getSelection().isEmpty()) {
                    return;
                }
                if (!MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), "Do you really want to delete the selected item(s) ?")) {
                    return;
                }
                doDeleteAction();
            }
        };
        deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIActivator.IMG_DELETE_ICON));
        deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIActivator.IMG_DELETE_DISABLED_ICON));
        deleteAction.setId(ActionFactory.DELETE.getId());
        deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
        deleteAction.setEnabled(false);

        removeAction = new Action("Remove") {

            @Override
            public void run() {
                if (viewer.getSelection().isEmpty()) {
                    return;
                }
                if (!MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), "Do you really want to remove the selected item(s) ?")) {
                    return;
                }
                doRemoveAction();
            }
        };
        removeAction.setImageDescriptor(imageRegistry.getDescriptor(UIActivator.IMG_REMOVE_ICON));
        removeAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIActivator.IMG_REMOVE_DISABLED_ICON));
        removeAction.setEnabled(false);

        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(expandAllAction);
        toolBarManager.add(collapseAllAction);

        site.getActionBars().setGlobalActionHandler(deleteAction.getId(), deleteAction);

        site.getActionBars().updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        viewer.getTree().setHeaderVisible(false);
        viewer.getTree().setLinesVisible(false);

        getSite().setSelectionProvider(viewer);

        createContextMenu();

        model = new ExplorerViewModel(repositoryService);

        viewer.setContentProvider(new ObservableListTreeContentProvider(model, model));
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public Image getImage(Object element) {
                if ((element instanceof StrategyItem) || (element instanceof InstrumentRootItem)) {
                    return UIActivator.getImageFromRegistry(UIActivator.IMG_FOLDER);
                }
                if (element instanceof InstrumentItem) {
                    return UIActivator.getImageFromRegistry(UIActivator.IMG_INSTRUMENT);
                }
                if (element instanceof ScriptRootItem) {
                    return UIActivator.getImageFromRegistry(UIActivator.IMG_SCRIPT_FOLDER);
                }
                if (element instanceof ScriptItem) {
                    return UIActivator.getImageFromRegistry(UIActivator.IMG_SCRIPT_INCLUDE);
                }
                if (element instanceof MainScriptItem) {
                    return UIActivator.getImageFromRegistry(UIActivator.IMG_MAIN_SCRIPT);
                }
                return super.getImage(element);
            }
        });

        viewer.setComparator(new ViewerComparator() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if ((e1 instanceof ScriptItem) && (e2 instanceof ScriptItem)) {
                    return 0;
                }
                return super.compare(viewer, e1, e2);
            }
        });

        viewer.setInput(model);
        viewer.expandAll();

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
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                deleteAction.setEnabled(canEnableDeleteAction(selection));
                removeAction.setEnabled(canEnableRemoveAction(selection));
            }
        });

        viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {
            SecurityObjectTransfer.getInstance(),
            RepositoryObjectTransfer.getInstance(),
        }, new ViewerDropAdapter(viewer) {

            @Override
            public boolean validateDrop(Object target, int operation, TransferData transferType) {
                return SecurityObjectTransfer.getInstance().isSupportedType(transferType) || RepositoryObjectTransfer.getInstance().isSupportedType(transferType);
            }

            @Override
            public boolean performDrop(Object data) {
                ScriptStrategy strategy = null;

                ExplorerViewItem target = (ExplorerViewItem) getCurrentTarget();
                while (strategy == null && target != null) {
                    strategy = (ScriptStrategy) target.getAdapter(ScriptStrategy.class);
                    if (strategy == null) {
                        target = target.getParent();
                    }
                }

                if (strategy != null) {
                    final ScriptStrategy repositoryObject = strategy;
                    final IAdaptable[] contents = (IAdaptable[]) data;
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            doAddDroppedObjects(repositoryObject, contents);
                        }
                    });
                }

                return true;
            }
        });
    }

    private void createContextMenu() {
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
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

                menuManager.appendToGroup("group.show", new Action("Expand All") {

                    @Override
                    public void run() {
                        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                        for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                            viewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
                        }
                    }
                });
                menuManager.appendToGroup("group.reorganize", removeAction);
                menuManager.appendToGroup("group.reorganize", deleteAction);
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
        viewer.getControl().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        if (serviceReference != null && repositoryService != null) {
            bundleContext.ungetService(serviceReference);
        }

        model.dispose();

        super.dispose();
    }

    private void doDeleteAction() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        for (final Object object : selection.toList()) {
            if (object instanceof StrategyItem) {
                final IStrategy repositoryObject = ((StrategyItem) object).getStrategy();
                repositoryService.runInService(new IRepositoryRunnable() {

                    @Override
                    public IStatus run(IProgressMonitor monitor) throws Exception {
                        repositoryService.deleteAdaptable(new IAdaptable[] {
                            repositoryObject
                        });
                        return Status.OK_STATUS;
                    }
                }, null);
            }
        }
    }

    private void doRemoveAction() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        for (final Object object : selection.toList()) {
            if (object instanceof InstrumentItem) {
                InstrumentItem instrumentItem = (InstrumentItem) object;
                final ScriptStrategy repositoryObject = (ScriptStrategy) instrumentItem.getAdapter(ScriptStrategy.class);
                if (repositoryObject != null) {
                    List<ISecurity> list = new ArrayList<ISecurity>(Arrays.asList(repositoryObject.getInstruments()));
                    list.remove(instrumentItem.getInstrument());
                    repositoryObject.setInstruments(list.toArray(new ISecurity[list.size()]));
                    repositoryService.runInService(new IRepositoryRunnable() {

                        @Override
                        public IStatus run(IProgressMonitor monitor) throws Exception {
                            repositoryService.saveAdaptable(new IAdaptable[] {
                                repositoryObject
                            });
                            return Status.OK_STATUS;
                        }
                    }, null);
                }
            }
            else if (object instanceof ScriptItem) {
                ScriptItem instrumentItem = (ScriptItem) object;
                final ScriptStrategy repositoryObject = (ScriptStrategy) instrumentItem.getAdapter(ScriptStrategy.class);
                if (repositoryObject != null) {
                    List<IScript> list = new ArrayList<IScript>(Arrays.asList(repositoryObject.getIncludes()));
                    list.remove(instrumentItem.getScript());
                    repositoryObject.setIncludes(list.toArray(new IScript[list.size()]));
                    repositoryService.runInService(new IRepositoryRunnable() {

                        @Override
                        public IStatus run(IProgressMonitor monitor) throws Exception {
                            repositoryService.saveAdaptable(new IAdaptable[] {
                                repositoryObject
                            });
                            return Status.OK_STATUS;
                        }
                    }, null);
                }
            }
        }
    }

    private boolean canEnableDeleteAction(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        for (final Object object : selection.toList()) {
            if (!(object instanceof StrategyItem)) {
                return false;
            }
        }
        return true;
    }

    private boolean canEnableRemoveAction(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        for (final Object object : selection.toList()) {
            if (!(object instanceof ScriptItem) && !(object instanceof InstrumentItem)) {
                return false;
            }
        }
        return true;
    }

    private void doAddDroppedObjects(ScriptStrategy strategy, IAdaptable[] contents) {
        boolean dirty = false;

        List<ISecurity> instruments = new ArrayList<ISecurity>(Arrays.asList(strategy.getInstruments()));
        List<IScript> scripts = new ArrayList<IScript>(Arrays.asList(strategy.getIncludes()));

        for (int i = 0; i < contents.length; i++) {
            ISecurity security = (ISecurity) contents[i].getAdapter(ISecurity.class);
            if (security != null && !instruments.contains(security)) {
                instruments.add(security);
                dirty = true;
            }
            IScript script = (IScript) contents[i].getAdapter(IScript.class);
            if (script != null && !scripts.contains(script)) {
                scripts.add(script);
                dirty = true;
            }
        }

        if (dirty) {
            strategy.setInstruments(instruments.toArray(new ISecurity[instruments.size()]));
            strategy.setIncludes(scripts.toArray(new IScript[scripts.size()]));

            final IAdaptable repositoryObject = strategy;
            repositoryService.runInService(new IRepositoryRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    repositoryService.saveAdaptable(new IAdaptable[] {
                        repositoryObject
                    });
                    return Status.OK_STATUS;
                }
            }, null);
        }
    }
}
