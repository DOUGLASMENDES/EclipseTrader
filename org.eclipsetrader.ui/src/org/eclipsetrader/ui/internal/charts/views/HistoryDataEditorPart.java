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

package org.eclipsetrader.ui.internal.charts.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.HistoryDay;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.Util;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.charts.DataImportJob;

public class HistoryDataEditorPart extends ViewPart implements ISaveablePart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.editors.history";

    public static final String K_VIEWS = "Views"; //$NON-NLS-1$
    public static final String K_URI = "uri"; //$NON-NLS-1$

    private URI uri;
    private ISecurity security;

    private Composite stackContainer;
    private StackLayout stackLayout;
    private MenuManager contextMenuManager;

    private HistoryDataEditor editor;
    private final Map<Date, HistoryDataDetailEditor> editorMap = new HashMap<Date, HistoryDataDetailEditor>();

    private Action updateAction;
    private Action editAction;
    private Action goIntoAction;
    private Action backUpAction;

    private IDialogSettings dialogSettings;
    private boolean dirty;

    private final PropertyChangeListener dirtyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!dirty) {
                dirty = true;
                firePropertyChange(PROP_DIRTY);
            }
        }
    };

    public HistoryDataEditorPart() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        try {
            dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(K_VIEWS).getSection(site.getSecondaryId());
            uri = new URI(dialogSettings.get(K_URI));

            IRepositoryService repositoryService = UIActivator.getDefault().getRepositoryService();
            security = repositoryService.getSecurityFromURI(uri);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, Messages.ChartViewPart_LoadingErrorMessage + site.getSecondaryId(), e);
            UIActivator.log(status);
        }

        createActions();

        IActionBars actionBars = site.getActionBars();

        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(goIntoAction);
        toolBarManager.add(backUpAction);
        toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
        toolBarManager.add(updateAction);
    }

    protected void createActions() {
        updateAction = new Action(Messages.ChartViewPart_UpdateAction) {

            @Override
            public void run() {
                DataImportJob job = new DataImportJob(security, DataImportJob.INCREMENTAL, null, null, new TimeSpan[] {
                    TimeSpan.days(1),
                    TimeSpan.minutes(1),
                    TimeSpan.minutes(2),
                    TimeSpan.minutes(3),
                    TimeSpan.minutes(5),
                    TimeSpan.minutes(10),
                    TimeSpan.minutes(15),
                    TimeSpan.minutes(30),
                });
                job.setUser(true);
                job.schedule();
            }
        };
        updateAction.setId("update"); //$NON-NLS-1$
        updateAction.setImageDescriptor(UIActivator.imageDescriptorFromPlugin("icons/etool16/refresh.gif")); //$NON-NLS-1$
        updateAction.setEnabled(true);

        editAction = new Action("Edit") {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) editor.getViewer().getSelection();
                if (selection.size() != 1) {
                    return;
                }
                final Object element = selection.getFirstElement();
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!editor.getControl().isDisposed()) {
                            editor.getViewer().editElement(element, 0);
                        }
                    }
                });
            }
        };
        editAction.setEnabled(true);

        goIntoAction = new Action("Zoom-In") {

            @Override
            public void run() {
                if (stackLayout.topControl == editor.getControl()) {
                    goInto();
                }
            }
        };
        goIntoAction.setImageDescriptor(UIActivator.getImageDescriptor("icons/elcl16/zoom_in.png"));
        goIntoAction.setEnabled(false);

        backUpAction = new Action("Zoom-Out") {

            @Override
            public void run() {
                if (stackLayout.topControl != editor.getControl()) {
                    setContentDescription("");
                    stackLayout.topControl = editor.getControl();
                    stackContainer.layout();
                }
                goIntoAction.setEnabled(stackLayout.topControl == editor.getControl());
                backUpAction.setEnabled(stackLayout.topControl != editor.getControl());
            }
        };
        backUpAction.setImageDescriptor(UIActivator.getImageDescriptor("icons/elcl16/zoom_out.png"));
        backUpAction.setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        stackContainer = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        stackLayout.marginWidth = stackLayout.marginHeight = 0;
        stackContainer.setLayout(stackLayout);

        editor = new HistoryDataEditor(stackContainer);
        editor.addPropertyChangeListener(dirtyChangeListener);

        stackLayout.topControl = editor.getControl();

        createContextMenu();

        editor.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                goIntoAction.setEnabled(!event.getSelection().isEmpty());
            }
        });
        editor.getViewer().addOpenListener(new IOpenListener() {

            @Override
            public void open(OpenEvent event) {
                if (stackLayout.topControl == editor.getControl()) {
                    goInto();
                }
            }
        });

        if (security != null) {
            setPartName(NLS.bind("{0} - {1}", new Object[] { //$NON-NLS-1$
                security.getName(), "History",
            }));

            editor.load(security);
        }
    }

    void createContextMenu() {
        contextMenuManager = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        contextMenuManager.setRemoveAllWhenShown(true);
        contextMenuManager.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager menuManager) {
                menuManager.add(new Separator("group.new"));
                menuManager.add(new GroupMarker("group.goto"));
                menuManager.add(new Separator("group.show"));
                menuManager.add(new Separator("group.edit"));
                menuManager.add(new GroupMarker("group.reorganize"));
                menuManager.add(new GroupMarker("group.port"));
                menuManager.add(new Separator("group.generate"));
                menuManager.add(new Separator("group.search"));
                menuManager.add(new Separator("group.build"));
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("group.properties"));

                menuManager.appendToGroup("group.goto", goIntoAction);
                menuManager.appendToGroup("group.goto", backUpAction);
                menuManager.appendToGroup("group.edit", editAction);
            }
        });
        editor.getViewer().getControl().setMenu(contextMenuManager.createContextMenu(editor.getViewer().getControl()));
        getSite().registerContextMenu(contextMenuManager, getSite().getSelectionProvider());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (stackContainer != null) {
            stackContainer.setFocus();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        editor.dispose();
        for (HistoryDataDetailEditor treeEditor : editorMap.values()) {
            treeEditor.dispose();
        }
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        final IRepositoryService repository = UIActivator.getDefault().getRepositoryService();
        repository.runInService(new IRepositoryRunnable() {

            @Override
            @SuppressWarnings("rawtypes")
            public IStatus run(IProgressMonitor monitor) throws Exception {
                for (HistoryDataDetailEditor treeEditor : editorMap.values()) {
                    if (!treeEditor.isDirty()) {
                        continue;
                    }
                    HistoryDataDetailEditorModel treeModel = treeEditor.getModel();
                    for (Entry<IHistory, HistoryDataEditorModel> entry : treeModel.getMap().entrySet()) {
                        IHistory history = (IHistory) ((Entry) entry).getKey();
                        HistoryDataEditorModel model = (HistoryDataEditorModel) ((Entry) entry).getValue();

                        HistoryDay saveableHistory = (HistoryDay) history.getAdapter(HistoryDay.class);
                        if (saveableHistory != null) {
                            saveableHistory.setOHLC(model.toOHLC());
                            repository.saveAdaptable(new IAdaptable[] {
                                history
                            });
                            treeEditor.setDirty(false);
                        }
                    }
                }

                if (editor.isDirty()) {
                    History history = (History) editor.getHistory().getAdapter(History.class);
                    if (history != null) {
                        history.setOHLC(editor.getModel().toOHLC());
                        repository.saveAdaptable(new IAdaptable[] {
                            history
                        });
                    }
                    editor.setDirty(false);
                }

                return Status.OK_STATUS;
            }
        }, monitor);

        dirty = false;
        firePropertyChange(PROP_DIRTY);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    @Override
    public void doSaveAs() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    @Override
    public boolean isSaveOnCloseNeeded() {
        return dirty;
    }

    private void goInto() {
        IStructuredSelection selection = (IStructuredSelection) editor.getViewer().getSelection();
        if (selection.size() != 1) {
            return;
        }
        final HistoryDataElement element = (HistoryDataElement) selection.getFirstElement();
        if (element.getDate() == null) {
            return;
        }

        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

            @Override
            public void run() {
                HistoryDataDetailEditor treeEditor = editorMap.get(element.getDate());
                if (treeEditor == null) {
                    treeEditor = new HistoryDataDetailEditor(stackContainer);
                    treeEditor.addPropertyChangeListener(dirtyChangeListener);
                    treeEditor.getViewer().getControl().setMenu(contextMenuManager.createContextMenu(treeEditor.getViewer().getControl()));
                    treeEditor.load(editor.getHistory(), element.getDate());
                    editorMap.put(element.getDate(), treeEditor);
                }

                setContentDescription(" " + Util.getDateFormat().format(element.getDate())); //$NON-NLS-1$

                stackLayout.topControl = treeEditor.getControl();
                stackContainer.layout();

                goIntoAction.setEnabled(stackLayout.topControl == editor.getControl());
                backUpAction.setEnabled(stackLayout.topControl != editor.getControl());
            }
        });
    }
}
