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

package org.eclipsetrader.ui.internal.trading.portfolio;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.ui.SelectionProvider;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.trading.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class PortfolioViewPart extends ViewPart {

    TreeViewer viewer;
    PortfolioView view;

    BundleContext context;
    ServiceReference tradingServiceReference;
    ITradingService tradingService;
    ServiceReference marketServiceReference;
    IMarketService marketService;

    Action expandAllAction;
    Action collapseAllAction;
    Action refreshAllAction;

    public PortfolioViewPart() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        context = Activator.getDefault().getBundle().getBundleContext();

        tradingServiceReference = context.getServiceReference(ITradingService.class.getName());
        tradingService = (ITradingService) context.getService(tradingServiceReference);

        marketServiceReference = context.getServiceReference(IMarketService.class.getName());
        marketService = (IMarketService) context.getService(marketServiceReference);

        site.setSelectionProvider(new SelectionProvider());
        site.getActionBars().setGlobalActionHandler("properties", new PropertyDialogAction(site, site.getSelectionProvider())); //$NON-NLS-1$

        initializeActions();

        IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
        toolbarManager.add(new Separator("group.new")); //$NON-NLS-1$
        toolbarManager.add(new GroupMarker("group.goto")); //$NON-NLS-1$
        toolbarManager.add(new Separator("group.open")); //$NON-NLS-1$
        toolbarManager.add(new GroupMarker("group.openWith")); //$NON-NLS-1$
        toolbarManager.add(new Separator("group.show")); //$NON-NLS-1$
        toolbarManager.add(expandAllAction);
        toolbarManager.add(collapseAllAction);
        toolbarManager.add(new Separator("group.edit")); //$NON-NLS-1$
        toolbarManager.add(new GroupMarker("group.reorganize")); //$NON-NLS-1$
        toolbarManager.add(new GroupMarker("group.port")); //$NON-NLS-1$
        toolbarManager.add(new Separator("group.generate")); //$NON-NLS-1$
        toolbarManager.add(new Separator("group.search")); //$NON-NLS-1$
        toolbarManager.add(new Separator("group.build")); //$NON-NLS-1$
        toolbarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolbarManager.add(new Separator("group.properties")); //$NON-NLS-1$

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

        site.getActionBars().updateActionBars();
    }

    void initializeActions() {
        ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();

        collapseAllAction = new Action(Messages.PortfolioViewPart_CollapseAll, imageRegistry.getDescriptor(UIConstants.COLLAPSEALL_ICON)) {

            @Override
            public void run() {
                viewer.collapseAll();
            }
        };

        expandAllAction = new Action(Messages.PortfolioViewPart_ExpandAll, imageRegistry.getDescriptor(UIConstants.EXPANDALL_ICON)) {

            @Override
            public void run() {
                viewer.expandAll();
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        createViewer(parent);
        createContextMenu();

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                getViewSite().getSelectionProvider().setSelection(event.getSelection());
            }
        });

        updateView();
    }

    protected void createViewer(Composite parent) {
        viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(false);
        viewer.setContentProvider(new PortfolioContentProvider());
        viewer.setSorter(new ViewerSorter());

        viewer.setInput(new PortfolioView());

        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 40));
        viewerColumn.setLabelProvider(new ElementLabelProvider());

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText(Messages.PortfolioViewPart_Position);
        viewerColumn.getColumn().setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 15));
        viewerColumn.setLabelProvider(new PositionLabelProvider());

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText(Messages.PortfolioViewPart_Price);
        viewerColumn.getColumn().setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 15));
        viewerColumn.setLabelProvider(new PriceLabelProvider());

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText(Messages.PortfolioViewPart_PL);
        viewerColumn.getColumn().setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 30));
        viewerColumn.setLabelProvider(new GainLabelProvider());
    }

    protected void createContextMenu() {
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager menuManager) {
                menuManager.add(new Separator("group.new")); //$NON-NLS-1$
                menuManager.add(new GroupMarker("group.goto")); //$NON-NLS-1$
                menuManager.add(new Separator("group.open")); //$NON-NLS-1$
                menuManager.add(new GroupMarker("group.openWith")); //$NON-NLS-1$
                menuManager.add(new Separator("group.trade")); //$NON-NLS-1$
                menuManager.add(new GroupMarker("group.tradeWith")); //$NON-NLS-1$
                menuManager.add(new Separator("group.show")); //$NON-NLS-1$
                menuManager.add(new Separator("group.edit")); //$NON-NLS-1$
                menuManager.add(new GroupMarker("group.reorganize")); //$NON-NLS-1$
                menuManager.add(new GroupMarker("group.port")); //$NON-NLS-1$
                menuManager.add(new Separator("group.generate")); //$NON-NLS-1$
                menuManager.add(new Separator("group.search")); //$NON-NLS-1$
                menuManager.add(new Separator("group.build")); //$NON-NLS-1$
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("group.properties")); //$NON-NLS-1$
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
    }

    protected void updateView() {
        Job job = new Job(Messages.PortfolioViewPart_LoadingPortfolio) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                view = new PortfolioView(tradingService, marketService);
                return Status.OK_STATUS;
            }
        };

        final Display display = viewer.getControl().getDisplay();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                if (view != null) {
                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                viewer.setInput(view);
                                viewer.expandAll();
                            }
                        }
                    });
                }
            }
        });

        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if (context != null) {
            if (tradingServiceReference != null) {
                context.ungetService(tradingServiceReference);
            }
            if (marketServiceReference != null) {
                context.ungetService(marketServiceReference);
            }
        }
        super.dispose();
    }
}
