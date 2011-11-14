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
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.charts.repository.IChartTemplate;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.charts.repository.ChartTemplate;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.ui.charts.BaseChartViewer;
import org.eclipsetrader.ui.charts.ChartCanvas;
import org.eclipsetrader.ui.charts.ChartObjectFactoryTransfer;
import org.eclipsetrader.ui.charts.ChartRowViewItem;
import org.eclipsetrader.ui.charts.ChartView;
import org.eclipsetrader.ui.charts.ChartViewItem;
import org.eclipsetrader.ui.charts.IChartEditorListener;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.charts.DataImportJob;
import org.eclipsetrader.ui.internal.charts.ImportDataPage;
import org.eclipsetrader.ui.internal.charts.Period;
import org.eclipsetrader.ui.internal.charts.PeriodList;

public class ChartViewPart extends ViewPart implements ISaveablePart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.chart"; //$NON-NLS-1$

    public static final String K_VIEWS = "Views"; //$NON-NLS-1$
    public static final String K_URI = "uri"; //$NON-NLS-1$
    public static final String K_TEMPLATE = "template"; //$NON-NLS-1$
    public static final String K_PRIVATE_TEMPLATE = "private-template"; //$NON-NLS-1$

    public static final String K_PERIOD = "period"; //$NON-NLS-1$
    public static final String K_RESOLUTION = "resolution"; //$NON-NLS-1$
    public static final String K_SHOW_TOOLTIPS = "show-tooltips"; //$NON-NLS-1$
    public static final String K_ZOOM_FACTOR = "zoom-factor"; //$NON-NLS-1$
    public static final String K_SHOW_CURRENT_PRICE = "show-current-price"; //$NON-NLS-1$
    public static final String K_SHOW_CURRENT_BOOK = "show-current-book"; //$NON-NLS-1$

    private URI uri;
    private ISecurity security;
    private IChartTemplate template;

    private BaseChartViewer viewer;
    private ChartView view;
    private IHistory history;
    private IHistory subsetHistory;
    private ChartViewDropTarget dropListener;
    private boolean dirty;

    private IDialogSettings dialogSettings;
    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;
    private Action deleteAction;
    private Action propertiesAction;
    private Action zoomOutAction;
    private Action zoomInAction;
    private Action zoomResetAction;
    private Action updateAction;
    private Action periodAllAction;
    private ContributionItem[] periodActions;

    private Action currentPriceLineAction;
    private CurrentPriceLineFactory currentPriceLineFactory;
    private Action currentBookAction;
    private CurrentBookFactory currentBookFactory;

    IMemento memento;
    IPreferenceStore preferenceStore;

    private class ContributionItem extends ActionContributionItem {

        private final Period period;

        public ContributionItem(final Period period) {
            super(new Action(period.getDescription(), Action.AS_RADIO_BUTTON) {

                @Override
                public void run() {
                    setPeriod(period.getPeriod(), period.getResolution());
                }
            });
            this.period = period;
        }

        public Period getPeriod() {
            return period;
        }
    }

    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (IPropertyConstants.BARS.equals(evt.getPropertyName())) {
                TimeSpan resolution = TimeSpan.fromString(dialogSettings.get(K_RESOLUTION));
                view.setRootDataSeries(new OHLCDataSeries(security.getName(), subsetHistory.getAdjustedOHLC(), resolution));
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!viewer.getControl().isDisposed()) {
                            refreshChart();
                        }
                    }
                });
            }
        }
    };

    private IViewChangeListener viewChangeListener = new IViewChangeListener() {

        @Override
        public void viewChanged(ViewEvent event) {
            scheduleLoadJob();
            setDirty();
        }
    };

    private IPropertyChangeListener preferenceChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            IPreferenceStore preferences = (IPreferenceStore) event.getSource();
            if (UIActivator.PREFS_SHOW_TOOLTIPS.equals(event.getProperty())) {
                viewer.setShowTooltips(preferences.getBoolean(UIActivator.PREFS_SHOW_TOOLTIPS));
            }
            if (UIActivator.PREFS_SHOW_SCALE_TOOLTIPS.equals(event.getProperty())) {
                viewer.setShowScaleTooltips(preferences.getBoolean(UIActivator.PREFS_SHOW_SCALE_TOOLTIPS));
            }
            if (UIActivator.PREFS_CROSSHAIR_ACTIVATION.equals(event.getProperty())) {
                viewer.setCrosshairMode(preferences.getInt(UIActivator.PREFS_CROSSHAIR_ACTIVATION));
            }
            if (UIActivator.PREFS_CROSSHAIR_SUMMARY_TOOLTIP.equals(event.getProperty())) {
                viewer.setDecoratorSummaryTooltips(preferences.getBoolean(UIActivator.PREFS_CROSSHAIR_SUMMARY_TOOLTIP));
            }
            if (UIActivator.PREFS_CHART_PERIODS.equals(event.getProperty())) {
                updatePeriodActions();
            }
        }
    };

    private Action printAction = new Action(Messages.ChartViewPart_PrintAction) {

        @Override
        public void run() {
            PrintDialog dialog = new PrintDialog(getViewSite().getShell(), SWT.NONE);
            PrinterData data = dialog.open();
            if (data == null) {
                return;
            }
            if (data.printToFile) {
                data.fileName = "print.out"; // TODO you probably want to ask the user for a filename //$NON-NLS-1$
            }

            Printer printer = new Printer(data);
            try {
                Rectangle printerBounds = printer.getClientArea();
                Rectangle trimBounds = printer.computeTrim(printerBounds.x, printerBounds.y, printerBounds.width, printerBounds.height);
                System.out.println(printerBounds + ", " + trimBounds); //$NON-NLS-1$

                if (printer.startJob(getPartName())) {
                    viewer.print(printer);
                    printer.endJob();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                printer.dispose();
            }
        }
    };

    public ChartViewPart() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        this.memento = memento;
        this.preferenceStore = UIActivator.getDefault().getPreferenceStore();

        try {
            dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(K_VIEWS).getSection(site.getSecondaryId());
            uri = new URI(dialogSettings.get(K_URI));

            IRepositoryService repositoryService = UIActivator.getDefault().getRepositoryService();
            security = repositoryService.getSecurityFromURI(uri);

            String privateTemplate = dialogSettings.get(K_PRIVATE_TEMPLATE);
            if (privateTemplate != null) {
                template = unmarshal(privateTemplate);
            }

            if (template == null) {
                IPath templatePath = new Path("data"); //$NON-NLS-1$
                if (dialogSettings.get(K_TEMPLATE) != null) {
                    templatePath = templatePath.append(dialogSettings.get(K_TEMPLATE));
                }
                else {
                    templatePath = templatePath.append("basic-template.xml");
                }
                InputStream stream = FileLocator.openStream(UIActivator.getDefault().getBundle(), templatePath, false);
                template = unmarshal(stream);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, Messages.ChartViewPart_LoadingErrorMessage + site.getSecondaryId(), e);
            UIActivator.getDefault().getLog().log(status);
        }

        site.setSelectionProvider(new SelectionProvider());

        createActions();
        createPeriodActions();

        IActionBars actionBars = site.getActionBars();

        IMenuManager menuManager = actionBars.getMenuManager();
        menuManager.add(new Separator("periods.top")); //$NON-NLS-1$
        menuManager.add(new Separator("periods")); //$NON-NLS-1$
        menuManager.add(new Separator("periods.bottom")); //$NON-NLS-1$
        menuManager.add(currentPriceLineAction);
        menuManager.add(currentBookAction);

        menuManager.appendToGroup("periods.top", periodAllAction); //$NON-NLS-1$
        if (periodActions != null) {
            for (int i = 0; i < periodActions.length; i++) {
                menuManager.appendToGroup("periods", periodActions[i]);
            }
        }

        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
        toolBarManager.add(updateAction);

        TimeSpan periodTimeSpan = TimeSpan.fromString(dialogSettings.get(K_PERIOD));
        TimeSpan resolutionTimeSpan = TimeSpan.fromString(dialogSettings.get(K_RESOLUTION));
        setPeriodActionSelection(periodTimeSpan, resolutionTimeSpan);

        actionBars.setGlobalActionHandler(cutAction.getId(), cutAction);
        actionBars.setGlobalActionHandler(copyAction.getId(), copyAction);
        actionBars.setGlobalActionHandler(pasteAction.getId(), pasteAction);
        actionBars.setGlobalActionHandler(deleteAction.getId(), deleteAction);

        actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(), printAction);

        ToolAction toolAction = new ToolAction(Messages.ChartViewPart_LineAction, this, "org.eclipsetrader.ui.charts.tools.line");
        actionBars.setGlobalActionHandler(toolAction.getId(), toolAction);
        toolAction = new ToolAction(Messages.ChartViewPart_FiboLineAction, this, "org.eclipsetrader.ui.charts.tools.fiboline");
        actionBars.setGlobalActionHandler(toolAction.getId(), toolAction);
        toolAction = new ToolAction(Messages.ChartViewPart_FanLineAction, this, "org.eclipsetrader.ui.charts.tools.fanline");
        actionBars.setGlobalActionHandler(toolAction.getId(), toolAction);
        toolAction = new ToolAction(Messages.ChartViewPart_FiboArcAction, this, "org.eclipsetrader.ui.charts.tools.fiboarc");
        actionBars.setGlobalActionHandler(toolAction.getId(), toolAction);

        actionBars.setGlobalActionHandler(zoomInAction.getActionDefinitionId(), zoomInAction);
        actionBars.setGlobalActionHandler(zoomOutAction.getActionDefinitionId(), zoomOutAction);
        actionBars.setGlobalActionHandler(zoomResetAction.getActionDefinitionId(), zoomResetAction);
        actionBars.setGlobalActionHandler(propertiesAction.getId(), propertiesAction);
        actionBars.updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento memento) {
        memento.putInteger(K_ZOOM_FACTOR, viewer.getZoomFactor());

        int[] weights = viewer.getWeights();
        if (weights.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < weights.length; i++) {
                if (i != 0) {
                    sb.append(";"); //$NON-NLS-1$
                }
                sb.append(weights[i]);
            }
            memento.putString("weights", sb.toString()); //$NON-NLS-1$
        }

        super.saveState(memento);
    }

    protected void createActions() {
        ISharedImages sharedImages = getViewSite().getWorkbenchWindow().getWorkbench().getSharedImages();

        zoomInAction = new Action(Messages.ChartViewPart_ZoomInAction) {

            @Override
            public void run() {
                int factor = viewer.getZoomFactor();
                viewer.setZoomFactor(factor + 1);
                zoomOutAction.setEnabled(true);
                zoomResetAction.setEnabled(true);
            }
        };
        zoomInAction.setId("zoomIn"); //$NON-NLS-1$
        zoomInAction.setActionDefinitionId("org.eclipsetrader.ui.charts.zoomIn"); //$NON-NLS-1$

        zoomOutAction = new Action(Messages.ChartViewPart_ZoomOutAction) {

            @Override
            public void run() {
                int factor = viewer.getZoomFactor();
                if (factor > 0) {
                    viewer.setZoomFactor(factor - 1);
                }
                zoomOutAction.setEnabled(factor != 1);
                zoomResetAction.setEnabled(factor != 1);
            }
        };
        zoomOutAction.setId("zoomOut"); //$NON-NLS-1$
        zoomOutAction.setActionDefinitionId("org.eclipsetrader.ui.charts.zoomOut"); //$NON-NLS-1$

        zoomResetAction = new Action(Messages.ChartViewPart_NormalSizeAction) {

            @Override
            public void run() {
                viewer.setZoomFactor(0);
                zoomOutAction.setEnabled(false);
                zoomResetAction.setEnabled(false);
            }
        };
        zoomResetAction.setId("zoomReset"); //$NON-NLS-1$
        zoomResetAction.setActionDefinitionId("org.eclipsetrader.ui.charts.zoomReset"); //$NON-NLS-1$

        zoomOutAction.setEnabled(false);
        zoomResetAction.setEnabled(false);

        periodAllAction = new Action(Messages.ChartViewPart_AllPeriodAction, IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                setPeriod(null, TimeSpan.days(1));
            }
        };

        cutAction = new Action(Messages.ChartViewPart_CutAction) {

            @Override
            public void run() {
            }
        };
        cutAction.setId("cut"); //$NON-NLS-1$
        cutAction.setActionDefinitionId("org.eclipse.ui.edit.cut"); //$NON-NLS-1$
        cutAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        cutAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        cutAction.setEnabled(false);

        copyAction = new Action(Messages.ChartViewPart_CopyAction) {

            @Override
            public void run() {
            }
        };
        copyAction.setId("copy"); //$NON-NLS-1$
        copyAction.setActionDefinitionId("org.eclipse.ui.edit.copy"); //$NON-NLS-1$
        copyAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        copyAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        copyAction.setEnabled(false);

        pasteAction = new Action(Messages.ChartViewPart_PasteAction) {

            @Override
            public void run() {
            }
        };
        pasteAction.setId("copy"); //$NON-NLS-1$
        pasteAction.setActionDefinitionId("org.eclipse.ui.edit.paste"); //$NON-NLS-1$
        pasteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        pasteAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        pasteAction.setEnabled(false);

        deleteAction = new Action(Messages.ChartViewPart_DeleteAction) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
                if (!selection.isEmpty()) {
                    if (MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), Messages.ChartViewPart_DeleteConfirmMessage)) {
                        ChartViewItem viewItem = (ChartViewItem) selection.getFirstElement();
                        ChartRowViewItem rowViewItem = (ChartRowViewItem) viewItem.getParent();
                        if (rowViewItem.getItemCount() == 1) {
                            rowViewItem.getParentView().removeRow(rowViewItem);
                        }
                        else {
                            rowViewItem.removeChildItem(viewItem);
                        }
                    }
                }
            }
        };
        deleteAction.setId("delete"); //$NON-NLS-1$
        deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
        deleteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        deleteAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        deleteAction.setEnabled(false);

        updateAction = new Action(Messages.ChartViewPart_UpdateAction) {

            @Override
            public void run() {
                doUpdate();
            }
        };
        updateAction.setId("update"); //$NON-NLS-1$
        updateAction.setImageDescriptor(UIActivator.getImageDescriptor("icons/etool16/refresh.gif")); //$NON-NLS-1$
        updateAction.setEnabled(true);

        propertiesAction = new PropertyDialogAction(new SameShellProvider(getViewSite().getShell()), getSite().getSelectionProvider()) {

            @Override
            public void run() {
                PreferenceDialog dialog = createDialog();
                if (dialog != null) {
                    if (dialog.open() == Window.OK) {
                        IStructuredSelection selection = (IStructuredSelection) getSite().getSelectionProvider().getSelection();

                        ChartViewItem viewItem = (ChartViewItem) selection.getFirstElement();
                        ((ChartRowViewItem) viewItem.getParent()).refresh();

                        refreshChart();
                        setDirty();
                    }
                }
            }
        };
        propertiesAction.setId(ActionFactory.PROPERTIES.getId());
        propertiesAction.setActionDefinitionId("org.eclipse.ui.file.properties"); //$NON-NLS-1$
        propertiesAction.setEnabled(false);

        currentPriceLineAction = new Action(Messages.ChartViewPart_ShowCurrentPriceAction, IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                currentPriceLineFactory.setEnable(isChecked());
                dialogSettings.put(K_SHOW_CURRENT_PRICE, isChecked());
            }
        };

        currentBookAction = new Action(Messages.ChartViewPart_ShowBookAction, IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                currentBookFactory.setEnable(isChecked());
                dialogSettings.put(K_SHOW_CURRENT_BOOK, isChecked());
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new BaseChartViewer(parent, SWT.NONE);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged((IStructuredSelection) event.getSelection());
                handleActionsEnablement();
            }
        });
        viewer.getEditor().addListener(new IChartEditorListener() {

            @Override
            public void applyEditorValue() {
                refreshChart();
                setDirty();
            }

            @Override
            public void cancelEditor() {
            }
        });

        Transfer[] transferTypes = new Transfer[] {
            ChartObjectFactoryTransfer.getInstance(),
        };
        DropTarget dropTarget = new DropTarget(viewer.getControl(), DND.DROP_COPY | DND.DROP_MOVE);
        dropTarget.setTransfer(transferTypes);
        dropTarget.addDropListener(dropListener = new ChartViewDropTarget(viewer));

        viewer.setShowTooltips(preferenceStore.getBoolean(UIActivator.PREFS_SHOW_TOOLTIPS));
        viewer.setShowScaleTooltips(preferenceStore.getBoolean(UIActivator.PREFS_SHOW_SCALE_TOOLTIPS));
        viewer.setCrosshairMode(preferenceStore.getInt(UIActivator.PREFS_CROSSHAIR_ACTIVATION));
        viewer.setDecoratorSummaryTooltips(preferenceStore.getBoolean(UIActivator.PREFS_CROSSHAIR_SUMMARY_TOOLTIP));
        preferenceStore.addPropertyChangeListener(preferenceChangeListener);

        if (memento != null) {
            if (memento.getString(K_ZOOM_FACTOR) != null) {
                int factor = memento.getInteger(K_ZOOM_FACTOR);
                viewer.setZoomFactor(factor);
                zoomOutAction.setEnabled(factor != 0);
                zoomResetAction.setEnabled(factor != 0);
            }
        }

        createContextMenu();

        currentPriceLineFactory = new CurrentPriceLineFactory();
        currentPriceLineFactory.setSecurity(security);
        currentPriceLineFactory.setEnable(dialogSettings.getBoolean(K_SHOW_CURRENT_PRICE));
        currentPriceLineAction.setChecked(dialogSettings.getBoolean(K_SHOW_CURRENT_PRICE));

        currentBookFactory = new CurrentBookFactory();
        currentBookFactory.setSecurity(security);
        currentBookFactory.setEnable(dialogSettings.getBoolean(K_SHOW_CURRENT_BOOK));
        currentBookAction.setChecked(dialogSettings.getBoolean(K_SHOW_CURRENT_BOOK));

        if (security != null && template != null) {
            setPartName(NLS.bind("{0} - {1}", new Object[] { //$NON-NLS-1$
                security.getName(), template.getName(),
            }));

            view = new ChartView(template);

            ChartRowViewItem rowItem = (ChartRowViewItem) view.getItems()[0];

            ChartViewItem viewItem = new ChartViewItem(rowItem, currentPriceLineFactory);
            rowItem.addChildItem(0, viewItem);

            viewItem = new ChartViewItem(rowItem, currentBookFactory);
            rowItem.addChildItem(0, viewItem);

            view.addViewChangeListener(viewChangeListener);

            scheduleLoadJob();
        }
    }

    void createContextMenu() {
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager menuManager) {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(cutAction);
                menuManager.add(copyAction);
                menuManager.add(pasteAction);
                menuManager.add(new Separator());
                menuManager.add(deleteAction);
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(propertiesAction);
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
    }

    void scheduleLoadJob() {
        final Display display = viewer.getControl().getDisplay();

        if (subsetHistory != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subsetHistory.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            }
        }

        ChartLoadJob job = new ChartLoadJob(security);
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                final ChartLoadJob job = (ChartLoadJob) event.getJob();

                history = job.getHistory();
                subsetHistory = job.getSubsetHistory();
                view.setRootDataSeries(new OHLCDataSeries(security.getName(), subsetHistory.getAdjustedOHLC(), job.getResolutionTimeSpan()));

                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (viewer.getControl().isDisposed()) {
                            return;
                        }

                        if (memento == null) {
                            memento = XMLMemento.createWriteRoot("root"); //$NON-NLS-1$
                        }
                        saveState(memento);

                        TimeSpan resolutionTimeSpan = TimeSpan.fromString(dialogSettings.get(K_RESOLUTION));
                        if (resolutionTimeSpan == null) {
                            resolutionTimeSpan = TimeSpan.days(1);
                        }
                        viewer.setResolutionTimeSpan(resolutionTimeSpan);

                        dropListener.setView(view);

                        PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subsetHistory.getAdapter(PropertyChangeSupport.class);
                        if (propertyChangeSupport != null) {
                            propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
                        }

                        refreshChart();
                    }
                });
            }
        });
        job.setTimeSpan(TimeSpan.fromString(dialogSettings.get(K_PERIOD)));
        job.setResolutionTimeSpan(TimeSpan.fromString(dialogSettings.get(K_RESOLUTION)));
        job.setName(Messages.ChartViewPart_LoadingText + getPartName());
        job.setUser(true);
        job.schedule();
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
        view.removeViewChangeListener(viewChangeListener);

        if (subsetHistory != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subsetHistory.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            }
        }

        if (UIActivator.getDefault() != null) {
            IPreferenceStore preferences = UIActivator.getDefault().getPreferenceStore();
            preferences.removePropertyChangeListener(preferenceChangeListener);
        }

        super.dispose();
    }

    protected void handleSelectionChanged(IStructuredSelection selection) {
        if (selection.size() != 1 || !(selection.getFirstElement() instanceof IChartObject)) {
            getViewSite().getSelectionProvider().setSelection(StructuredSelection.EMPTY);
            return;
        }
        ChartViewItemFinder finder = new ChartViewItemFinder((IChartObject) selection.getFirstElement());
        view.accept(finder);
        if (finder.getViewItem() == null) {
            getViewSite().getSelectionProvider().setSelection(new StructuredSelection(view));
        }
        else {
            getViewSite().getSelectionProvider().setSelection(new StructuredSelection(finder.getViewItem()));
        }
    }

    protected void handleActionsEnablement() {
        IStructuredSelection selection = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
        IViewItem viewItem = (IViewItem) selection.getFirstElement();

        cutAction.setEnabled(!selection.isEmpty() && viewItem != null && viewItem.getAdapter(MainChartFactory.class) == null);
        copyAction.setEnabled(!selection.isEmpty() && viewItem != null && viewItem.getAdapter(MainChartFactory.class) == null);

        deleteAction.setEnabled(!selection.isEmpty() && viewItem != null && viewItem.getAdapter(MainChartFactory.class) == null);

        propertiesAction.setEnabled(!selection.isEmpty());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            String privateTemplate = marshal(view.getTemplate());
            dialogSettings.put(K_PRIVATE_TEMPLATE, privateTemplate);
            clearDirty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    @Override
    public void doSaveAs() {
    }

    protected void setDirty() {
        if (!dirty) {
            dirty = true;
            firePropertyChange(PROP_DIRTY);
        }
    }

    protected void clearDirty() {
        if (dirty) {
            dirty = false;
            firePropertyChange(PROP_DIRTY);
        }
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(BaseChartViewer.class)) {
            return viewer;
        }
        if (adapter.isAssignableFrom(ChartCanvas.class)) {
            return viewer.getSelectedChartCanvas();
        }
        if (adapter.isAssignableFrom(ChartView.class)) {
            return view;
        }
        if (adapter.isAssignableFrom(IChartTemplate.class)) {
            return template;
        }
        if (adapter.isAssignableFrom(ISecurity.class)) {
            return security;
        }
        if (adapter.isAssignableFrom(IDialogSettings.class)) {
            return dialogSettings;
        }
        return super.getAdapter(adapter);
    }

    private String marshal(IChartTemplate object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartTemplate.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private ChartTemplate unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartTemplate.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ChartTemplate) unmarshaller.unmarshal(new StringReader(string));
    }

    private ChartTemplate unmarshal(InputStream stream) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartTemplate.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ChartTemplate) unmarshaller.unmarshal(stream);
    }

    protected void refreshChart() {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            @Override
            public void run() {
                ChartRowViewItem[] rowViewItem = (ChartRowViewItem[]) view.getAdapter(ChartRowViewItem[].class);
                if (rowViewItem != null) {
                    IChartObject[][] input = new IChartObject[rowViewItem.length][];
                    for (int i = 0; i < input.length; i++) {
                        input[i] = (IChartObject[]) rowViewItem[i].getAdapter(IChartObject[].class);
                    }
                    viewer.setInput(input);

                    if (memento != null && memento.getString("weights") != null) { //$NON-NLS-1$
                        String[] s = memento.getString("weights").split(";"); //$NON-NLS-1$ //$NON-NLS-2$
                        int[] weights = viewer.getWeights();
                        for (int i = 0; i < weights.length && i < s.length; i++) {
                            weights[i] = Integer.valueOf(s[i]);
                        }
                        viewer.setWeights(weights);
                    }
                }
            }
        });
    }

    public void setPeriod(TimeSpan period, TimeSpan resolution) {
        dialogSettings.put(K_PERIOD, period != null ? period.toString() : (String) null);
        dialogSettings.put(K_RESOLUTION, resolution != null ? resolution.toString() : (String) null);

        periodAllAction.setChecked(period == null);
        setPeriodActionSelection(period, resolution);

        scheduleLoadJob();
    }

    protected void doUpdate() {
        TimeSpan[] aggregation = new TimeSpan[] {
            TimeSpan.days(1),
            TimeSpan.minutes(1),
        };

        IDialogSettings dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(ImportDataPage.class.getName());
        if (dialogSettings != null) {
            String[] s = dialogSettings.getArray("AGGREGATION");
            if (s != null && s.length != 0) {
                aggregation = new TimeSpan[s.length];
                for (int i = 0; i < aggregation.length; i++) {
                    aggregation[i] = TimeSpan.fromString(s[i]);
                }
            }
        }

        DataImportJob job = new DataImportJob(security, DataImportJob.INCREMENTAL, null, null, aggregation);
        job.setUser(true);
        job.schedule();
    }

    protected void updatePeriodActions() {
        IActionBars actionBars = getViewSite().getActionBars();

        IMenuManager menuManager = actionBars.getMenuManager();
        if (periodActions != null) {
            for (int i = 0; i < periodActions.length; i++) {
                menuManager.remove(periodActions[i]);
                periodActions[i].dispose();
            }
            periodActions = null;
        }

        createPeriodActions();

        if (periodActions != null) {
            for (int i = 0; i < periodActions.length; i++) {
                menuManager.appendToGroup("periods", periodActions[i]);
            }
        }

        TimeSpan periodTimeSpan = TimeSpan.fromString(dialogSettings.get(K_PERIOD));
        TimeSpan resolutionTimeSpan = TimeSpan.fromString(dialogSettings.get(K_RESOLUTION));
        setPeriodActionSelection(periodTimeSpan, resolutionTimeSpan);

        actionBars.updateActionBars();
    }

    public void createPeriodActions() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PeriodList.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader stream = new StringReader(UIActivator.getDefault().getPreferenceStore().getString(UIActivator.PREFS_CHART_PERIODS));

            PeriodList list = (PeriodList) unmarshaller.unmarshal(stream);
            Collections.sort(list, new Comparator<Period>() {

                @Override
                public int compare(Period o1, Period o2) {
                    if (o1.getPeriod().higherThan(o2.getPeriod())) {
                        return -1;
                    }
                    if (o2.getPeriod().higherThan(o1.getPeriod())) {
                        return 1;
                    }
                    return 0;
                }
            });

            periodActions = new ContributionItem[list.size()];
            for (int i = 0; i < periodActions.length; i++) {
                periodActions[i] = new ContributionItem(list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPeriodActionSelection(TimeSpan period, TimeSpan resolution) {
        if (periodActions != null) {
            for (int i = 0; i < periodActions.length; i++) {
                periodActions[i].getAction().setChecked(periodActions[i].getPeriod().equalsTo(period, resolution));
            }
        }
    }
}
