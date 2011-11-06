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

package org.eclipsetrader.news.internal.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.news.core.HeadLineStatus;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceListener;
import org.eclipsetrader.news.core.NewsEvent;
import org.eclipsetrader.news.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class HeadLineViewer extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.views.headlines";

    private static final String K_VIEWS = "Views";
    private static final String K_URI = "uri";
    private static final String K_COLUMNS = "columns";

    private Action refreshAction;
    private Action showPreviousAction;
    private Action showNextAction;

    private Action openAction;
    private Action openNewWindowAction;
    private Action markAsReadAction;
    private Action markAllAsReadAction;

    private TableViewer viewer;
    private IDialogSettings dialogSettings;

    private INewsService service;
    private List<IHeadLine> input;
    private ISecurity security;

    private ControlAdapter controlListener = new ControlAdapter() {

        @Override
        public void controlResized(ControlEvent e) {
            TableColumn tableColumn = (TableColumn) e.widget;
            int index = viewer.getTable().indexOf(tableColumn);

            if (dialogSettings != null) {
                IDialogSettings columnsSection = dialogSettings.getSection(K_COLUMNS);
                if (columnsSection == null) {
                    columnsSection = dialogSettings.addNewSection(K_COLUMNS);
                }
                columnsSection.put(String.valueOf(index), tableColumn.getWidth());
            }
        }
    };

    private INewsServiceListener newsListener = new INewsServiceListener() {

        @Override
        public void newsServiceUpdate(NewsEvent event) {
            for (HeadLineStatus status : event.getStatus()) {
                if (security != null && !status.getHeadLine().contains(security)) {
                    continue;
                }
                if (status.getKind() == HeadLineStatus.ADDED) {
                    input.add(status.getHeadLine());
                }
                else if (status.getKind() == HeadLineStatus.REMOVED) {
                    input.remove(status.getHeadLine());
                }
            }
            if (!viewer.getControl().isDisposed()) {
                try {
                    viewer.getControl().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                viewer.refresh();
                                updateTitleImage();
                            }
                        }
                    });
                } catch (SWTException e) {
                    if (e.code != SWT.ERROR_WIDGET_DISPOSED) {
                        throw e;
                    }
                }
            }
        }
    };

    private ISelectionListener selectionListener = new ISelectionListener() {

        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            viewer.setSelection(selection, true);
        }
    };

    public HeadLineViewer() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        IDialogSettings bundleDialogSettings = Activator.getDefault().getDialogSettings();
        if (site.getSecondaryId() != null) {
            try {
                dialogSettings = bundleDialogSettings.getSection(K_VIEWS).getSection(site.getSecondaryId());
                URI uri = new URI(dialogSettings.get(K_URI));

                IRepositoryService repositoryService = Activator.getDefault().getRepositoryService();
                security = repositoryService.getSecurityFromURI(uri);
                if (security != null) {
                    setPartName(NLS.bind("{0} - {1}", new Object[] {
                        security.getName(), getPartName()
                    }));
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error loading view " + site.getSecondaryId(), e);
                Activator.getDefault().getLog().log(status);
            }
        }

        if (dialogSettings == null) {
            dialogSettings = bundleDialogSettings.getSection(getClass().getName());
            if (dialogSettings == null) {
                dialogSettings = bundleDialogSettings.addNewSection(getClass().getName());
            }
        }

        refreshAction = new Action("Refresh") {

            @Override
            public void run() {
                IPreferenceStore store = Activator.getDefault().getPreferenceStore();

                INewsProvider[] providers = service.getProviders();
                for (int i = 0; i < providers.length; i++) {
                    if (store.getBoolean(providers[i].getId())) {
                        providers[i].refresh();
                    }
                }
            }
        };
        refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/elcl16/refresh.gif")); //$NON-NLS-1$
        refreshAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/dlcl16/refresh.gif")); //$NON-NLS-1$

        showPreviousAction = new Action("Previous") {

            @Override
            public void run() {
                IHeadLine headLine = getPreviousHeadLine();
                if (headLine != null) {
                    viewer.setSelection(new StructuredSelection(headLine), true);
                    doOpenHeadLine(headLine, false);
                }
            }
        };
        showPreviousAction.setImageDescriptor(Activator.getImageDescriptor("icons/elcl16/prev_nav.gif")); //$NON-NLS-1$
        showPreviousAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/dlcl16/prev_nav.gif")); //$NON-NLS-1$

        showNextAction = new Action("Next") {

            @Override
            public void run() {
                IHeadLine headLine = getNextHeadLine();
                if (headLine != null) {
                    viewer.setSelection(new StructuredSelection(headLine), true);
                    doOpenHeadLine(headLine, false);
                }
            }
        };
        showNextAction.setImageDescriptor(Activator.getImageDescriptor("icons/elcl16/next_nav.gif")); //$NON-NLS-1$
        showNextAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/dlcl16/next_nav.gif")); //$NON-NLS-1$

        openAction = new Action("Open") {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (!selection.isEmpty()) {
                    IHeadLine headLine = (IHeadLine) selection.getFirstElement();
                    doOpenHeadLine(headLine, false);
                }
            }
        };
        openNewWindowAction = new Action("Open in New Browser") {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (!selection.isEmpty()) {
                    IHeadLine headLine = (IHeadLine) selection.getFirstElement();
                    doOpenHeadLine(headLine, true);
                }
            }
        };
        markAsReadAction = new Action("Mark as Read") {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                List<?> list = selection.toList();
                final IHeadLine[] headLines = list.toArray(new IHeadLine[list.size()]);
                for (int i = 0; i < headLines.length; i++) {
                    headLines[i].setReaded(true);
                }
                service.updateHeadLines(headLines);
            }
        };
        markAllAsReadAction = new Action("Mark All as Read") {

            @Override
            public void run() {
                final IHeadLine[] headLines = input.toArray(new IHeadLine[input.size()]);
                for (int i = 0; i < headLines.length; i++) {
                    headLines[i].setReaded(true);
                }
                service.updateHeadLines(headLines);
            }
        };

        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(showPreviousAction);
        toolBarManager.add(showNextAction);
        toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
        toolBarManager.add(refreshAction);
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        createViewer(parent);

        input = new ArrayList<IHeadLine>(Arrays.asList(getHeadLines()));
        viewer.setInput(input);

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

                menuManager.appendToGroup("group.open", openAction);
                menuManager.appendToGroup("group.open", openNewWindowAction);
                menuManager.appendToGroup("group.edit", markAsReadAction);
                menuManager.appendToGroup("group.edit", markAllAsReadAction);
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));

        if (getSite() != null) {
            getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

            getSite().getPage().addSelectionListener(selectionListener);
            viewer.setSelection(getSite().getPage().getSelection(), true);
        }
    }

    protected void updateTitleImage() {
        if (hasUnreadedHeadlines()) {
            setTitleImage(Activator.getDefault().getImageRegistry().get("new_headlines_icon"));
        }
        else {
            setTitleImage(Activator.getDefault().getImageRegistry().get("normal_icon"));
        }
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
        if (service != null) {
            service.removeNewsServiceListener(newsListener);
        }

        getSite().getPage().removeSelectionListener(selectionListener);

        super.dispose();
    }

    protected void createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(false);

        IDialogSettings columnsSection = dialogSettings != null ? dialogSettings.getSection(K_COLUMNS) : null;

        TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
        tableColumn.setText("Date");
        tableColumn.setWidth(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 160);
        tableColumn.addControlListener(controlListener);

        tableColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
        tableColumn.setText("Title");
        tableColumn.setWidth(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 300);
        tableColumn.addControlListener(controlListener);

        tableColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
        tableColumn.setText("Security");
        tableColumn.setWidth(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 250);
        tableColumn.addControlListener(controlListener);

        tableColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
        tableColumn.setText("Source");
        tableColumn.setWidth(columnsSection != null && columnsSection.get(String.valueOf(viewer.getTable().indexOf(tableColumn))) != null ? columnsSection.getInt(String.valueOf(viewer.getTable().indexOf(tableColumn))) : 200);
        tableColumn.addControlListener(controlListener);

        viewer.setLabelProvider(new HeadLineLabelProvider());
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                int cat1 = category(e1);
                int cat2 = category(e2);
                if (cat1 != cat2) {
                    return cat1 - cat2;
                }
                return ((IHeadLine) e2).getDate().compareTo(((IHeadLine) e1).getDate());
            }

            @Override
            public int category(Object element) {
                IHeadLine headLine = (IHeadLine) element;
                return headLine.isRecent() ? 0 : 1;
            }
        });

        viewer.addOpenListener(new IOpenListener() {

            @Override
            public void open(OpenEvent event) {
                if (!event.getSelection().isEmpty()) {
                    IHeadLine headLine = (IHeadLine) ((IStructuredSelection) event.getSelection()).getFirstElement();
                    doOpenHeadLine(headLine, false);
                }
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                openAction.setEnabled(!event.getSelection().isEmpty());
                markAsReadAction.setEnabled(!event.getSelection().isEmpty());
                markAllAsReadAction.setEnabled(input.size() != 0);
            }
        });
    }

    protected void doOpenHeadLine(IHeadLine headLine, boolean newWindow) {
        IWorkbenchPage page = getSite().getPage();

        IViewReference[] refs = page.getViewReferences();
        for (int i = 0; i < refs.length; i++) {
            if (refs[i].getId().equals(NewsViewer.VIEW_ID)) {
                NewsViewer viewer = (NewsViewer) refs[i].getPart(true);
                if (headLine.equals(viewer.getHeadLine())) {
                    if (!page.isPartVisible(viewer)) {
                        page.bringToTop(viewer);
                    }
                    return;
                }
            }
        }

        try {
            IViewPart viewPart = newWindow ? page.showView(NewsViewer.VIEW_ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE) : page.showView(NewsViewer.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
            ((NewsViewer) viewPart).setHeadLine(headLine);
            headLine.setReaded(true);
            if (service != null) {
                service.updateHeadLines(new IHeadLine[] {
                    headLine
                });
            }
        } catch (PartInitException e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Unexpected error activating browser", null);
            Activator.log(status);
        }
    }

    protected int indexOf(IHeadLine headLine) {
        TableItem[] tableItem = viewer.getTable().getItems();
        for (int i = 0; i < tableItem.length; i++) {
            if (headLine.equals(tableItem[i].getData())) {
                return i;
            }
        }
        return -1;
    }

    protected IHeadLine getNextHeadLine() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        if (!selection.isEmpty()) {
            int index = indexOf((IHeadLine) selection.getFirstElement());
            if (index == -1) {
                if (viewer.getTable().getItemCount() == 0) {
                    return null;
                }
                return (IHeadLine) viewer.getTable().getItem(0).getData();
            }

            index++;
            if (index < viewer.getTable().getItemCount()) {
                return (IHeadLine) viewer.getTable().getItem(index).getData();
            }
        }

        if (viewer.getTable().getItemCount() == 0) {
            return null;
        }
        return (IHeadLine) viewer.getTable().getItem(0).getData();
    }

    protected IHeadLine getPreviousHeadLine() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        if (!selection.isEmpty()) {
            int index = indexOf((IHeadLine) selection.getFirstElement());
            if (index == -1) {
                if (viewer.getTable().getItemCount() == 0) {
                    return null;
                }
                return (IHeadLine) viewer.getTable().getItem(viewer.getTable().getItemCount() - 1).getData();
            }

            index--;
            if (index >= 0) {
                return (IHeadLine) viewer.getTable().getItem(index).getData();
            }
        }

        if (viewer.getTable().getItemCount() == 0) {
            return null;
        }
        return (IHeadLine) viewer.getTable().getItem(viewer.getTable().getItemCount() - 1).getData();
    }

    TableViewer getViewer() {
        return viewer;
    }

    protected IHeadLine[] getHeadLines() {
        IHeadLine[] result = new IHeadLine[0];

        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(INewsService.class.getName());
        if (serviceReference != null) {
            service = (INewsService) context.getService(serviceReference);
            if (service != null) {
                if (security != null) {
                    result = service.getHeadLinesFor(security);
                }
                else {
                    result = service.getHeadLines();
                }
                service.addNewsServiceListener(newsListener);
            }
            context.ungetService(serviceReference);
        }

        return result;
    }

    protected boolean hasUnreadedHeadlines() {
        for (IHeadLine headLine : input) {
            if (!headLine.isReaded()) {
                return true;
            }
        }
        return false;
    }
}
