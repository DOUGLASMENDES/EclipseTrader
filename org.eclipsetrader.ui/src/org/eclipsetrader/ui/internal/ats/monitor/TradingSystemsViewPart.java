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

package org.eclipsetrader.ui.internal.ats.monitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.ats.TradingSystemProperties;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.ats.ViewColumn;
import org.eclipsetrader.ui.internal.ats.ViewerObservableMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradingSystemsViewPart extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.views.ats.monitor"; //$NON-NLS-1$

    static final String COLUMNS = "COLUMNS"; //$NON-NLS-1$
    static final String COLUMN_NAMES = "COLUMN_NAMES"; //$NON-NLS-1$
    static final String COLUMN_WIDTHS = "COLUMN_WIDTHS"; //$NON-NLS-1$
    static final String LABEL_COLUMN = "_label_"; //$NON-NLS-1$

    CoreActivator activator;
    ITradingSystemService tradingSystemService;
    TreeViewer viewer;
    TradingSystemsViewModel model;

    private Action settingsAction;

    IDialogSettings dialogSettings;

    private PropertyChangeListener columnPropertiesChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            int index = model.getDataProviders().indexOf(evt.getSource());
            if (index != -1) {
                viewer.getTree().getColumn(index + 1).setText((String) evt.getNewValue());
            }
        }
    };

    private ControlListener columnControlListener = new ControlAdapter() {

        @Override
        public void controlResized(ControlEvent e) {
            IDialogSettings settings = dialogSettings.getSection(COLUMN_WIDTHS);
            TreeColumn treeColumn = (TreeColumn) e.widget;
            int index = treeColumn.getParent().indexOf(treeColumn);
            if (index == 0) {
                settings.put(LABEL_COLUMN, treeColumn.getWidth());
            }
            else {
                ViewColumn viewColumn = model.getDataProviders().get(index - 1);
                settings.put(viewColumn.getDataProviderFactory().getId(), treeColumn.getWidth());
            }
        }
    };

    public TradingSystemsViewPart() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();

        ServiceReference<ITradingSystemService> serviceReference = bundleContext.getServiceReference(ITradingSystemService.class);
        tradingSystemService = bundleContext.getService(serviceReference);

        activator = CoreActivator.getDefault();

        IDialogSettings rootDialogSettings = UIActivator.getDefault().getDialogSettings();
        dialogSettings = rootDialogSettings.getSection(VIEW_ID);
        if (dialogSettings == null) {
            dialogSettings = rootDialogSettings.addNewSection(VIEW_ID);
            dialogSettings.put(COLUMNS, new String[] {
                "org.eclipsetrader.ui.providers.LastTrade", //$NON-NLS-1$
                "org.eclipsetrader.ui.providers.BidPrice", //$NON-NLS-1$
                "org.eclipsetrader.ui.providers.AskPrice", //$NON-NLS-1$
                "org.eclipsetrader.ui.providers.Position", //$NON-NLS-1$
                "org.eclipsetrader.ui.providers.LastTradeDateTime", //$NON-NLS-1$
                "org.eclipsetrader.ui.providers.gain", //$NON-NLS-1$
            });
            IDialogSettings section = dialogSettings.addNewSection(COLUMN_NAMES);
            section.put("org.eclipsetrader.ui.providers.LastTrade", "Last"); //$NON-NLS-1$
            section.put("org.eclipsetrader.ui.providers.BidPrice", "Bid"); //$NON-NLS-1$
            section.put("org.eclipsetrader.ui.providers.AskPrice", "Ask"); //$NON-NLS-1$
            section.put("org.eclipsetrader.ui.providers.Position", "Position"); //$NON-NLS-1$
            section.put("org.eclipsetrader.ui.providers.LastTradeDateTime", "Date / Time"); //$NON-NLS-1$
            section.put("org.eclipsetrader.ui.providers.gain", "Gain"); //$NON-NLS-1$
            dialogSettings.addNewSection(COLUMN_WIDTHS);
        }

        settingsAction = new SettingsAction(site.getShell(), this);

        IActionBars actionBars = site.getActionBars();
        actionBars.setGlobalActionHandler(settingsAction.getId(), settingsAction);
        actionBars.updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(false);

        createContextMenu();

        model = new TradingSystemsViewModel(tradingSystemService);

        String[] columns = dialogSettings.getArray(COLUMNS);
        IDialogSettings namesSection = dialogSettings.getSection(COLUMN_NAMES);

        List<ViewColumn> list = new ArrayList<ViewColumn>();
        for (int i = 0; i < columns.length; i++) {
            String name = namesSection.get(columns[i]);
            list.add(new ViewColumn(name, activator.getDataProviderFactory(columns[i])));
        }
        model.setDataProviders(list);

        for (final TradingSystemItem item : model.getList()) {
            PropertyChangeSupport changeSupport = (PropertyChangeSupport) item.getTradingSystem().getAdapter(PropertyChangeSupport.class);
            if (changeSupport != null) {
                changeSupport.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                if (viewer.getControl().isDisposed()) {
                                    return;
                                }
                                viewer.refresh(item);
                            }
                        });
                    }
                });
            }
        }

        final ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(model, model);
        viewer.setContentProvider(contentProvider);

        IDialogSettings widthSettings = dialogSettings.getSection(COLUMN_WIDTHS);

        TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);

        int width = 300;
        if (widthSettings.get(LABEL_COLUMN) != null) {
            width = widthSettings.getInt(LABEL_COLUMN);
        }
        viewerColumn.getColumn().setWidth(width);

        viewerColumn.getColumn().addControlListener(columnControlListener);

        final List<IObservableMap> properties = new ArrayList<IObservableMap>();
        properties.add(new ViewerObservableMap(contentProvider.getKnownElements(), LABEL_COLUMN));
        for (ViewColumn column : model.getDataProviders()) {
            viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
            viewerColumn.getColumn().setText(column.getName());

            width = 100;
            if (widthSettings.get(column.getDataProviderFactory().getId()) != null) {
                width = widthSettings.getInt(column.getDataProviderFactory().getId());
            }
            viewerColumn.getColumn().setWidth(width);

            viewerColumn.getColumn().addControlListener(columnControlListener);
            column.addPropertyChangeListener(ViewColumn.PROP_NAME, columnPropertiesChangeListener);
            properties.add(new ViewerObservableMap(contentProvider.getKnownElements(), column.getDataProviderFactory().getId()));
        }
        createLabelProvider(properties.toArray(new IObservableMap[properties.size()]));

        model.getObservableDataProviders().addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                event.diff.accept(new ListDiffVisitor() {

                    @Override
                    public void handleRemove(int index, Object element) {
                        TreeColumn treeColumn = viewer.getTree().getColumn(index + 1);
                        treeColumn.dispose();

                        ViewColumn column = (ViewColumn) element;
                        column.removePropertyChangeListener(ViewColumn.PROP_NAME, columnPropertiesChangeListener);

                        IObservableMap observableMap = properties.remove(index + 1);
                        if (observableMap != null) {
                            observableMap.dispose();
                        }
                    }

                    @Override
                    public void handleAdd(int index, Object element) {
                        ViewColumn column = (ViewColumn) element;
                        TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT, index + 1);
                        viewerColumn.getColumn().setText(column.getName());

                        int width = 100;
                        IDialogSettings widthSettings = dialogSettings.getSection(COLUMN_WIDTHS);
                        if (widthSettings.get(column.getDataProviderFactory().getId()) != null) {
                            width = widthSettings.getInt(column.getDataProviderFactory().getId());
                        }
                        viewerColumn.getColumn().setWidth(width);

                        viewerColumn.getColumn().addControlListener(columnControlListener);
                        column.addPropertyChangeListener(ViewColumn.PROP_NAME, columnPropertiesChangeListener);
                        properties.add(index + 1, new ViewerObservableMap(contentProvider.getKnownElements(), column.getDataProviderFactory().getId()));
                    }
                });

                createLabelProvider(properties.toArray(new IObservableMap[properties.size()]));

                IDialogSettings namesSection = dialogSettings.getSection(COLUMN_NAMES);

                List<String> list = new ArrayList<String>();
                for (ViewColumn column : model.getDataProviders()) {
                    list.add(column.getDataProviderFactory().getId());
                    namesSection.put(column.getDataProviderFactory().getId(), column.getName());
                }
                dialogSettings.put(COLUMNS, list.toArray(new String[list.size()]));
            }
        });

        viewer.setComparator(new ViewerComparator());

        getSite().setSelectionProvider(viewer);

        viewer.setInput(model);
        viewer.expandAll();
    }

    private void createLabelProvider(IObservableMap[] properties) {
        ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(properties) {

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                if (columnIndex == 0) {
                    if (element instanceof TradingSystemItem) {
                        return UIActivator.getImageFromRegistry(UIActivator.IMG_TRADING_SYSTEM);
                    }
                    if (element instanceof TradingSystemInstrumentItem) {
                        return UIActivator.getImageFromRegistry(UIActivator.IMG_INSTRUMENT);
                    }
                }
                return super.getColumnImage(element, columnIndex);
            }
        };

        ILabelDecorator labelDecorator = new ILabelDecorator() {

            private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

            @Override
            public void addListener(ILabelProviderListener listener) {
                listeners.add(listener);
            }

            @Override
            public void removeListener(ILabelProviderListener listener) {
                listeners.remove(listener);
            }

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return LABEL_COLUMN.equals(property);
            }

            @Override
            public void dispose() {
                listeners.clear();
            }

            @Override
            public String decorateText(String text, Object element) {
                if (element instanceof TradingSystemItem) {
                    StringBuilder sb = new StringBuilder();

                    TradingSystemItem item = (TradingSystemItem) element;

                    switch (item.getTradingSystem().getStatus()) {
                        case ITradingSystem.STATUS_STARTING:
                            sb.append("starting");
                            break;
                        case ITradingSystem.STATUS_STARTED:
                            sb.append("started");
                            break;
                        case ITradingSystem.STATUS_STOPPING:
                            sb.append("stopping");
                            break;
                        case ITradingSystem.STATUS_STOPPED:
                            sb.append("stopped");
                            break;
                    }

                    TradingSystemProperties properties = (TradingSystemProperties) item.getTradingSystem().getAdapter(TradingSystemProperties.class);
                    if (properties != null) {
                        if (properties.isAutostart()) {
                            if (sb.length() != 0) {
                                sb.append(", ");
                            }
                            sb.append("autostart");
                        }
                    }

                    if (sb.length() != 0) {
                        sb.insert(0, " [");
                        sb.append("]");
                    }

                    return text + sb.toString();
                }
                return text;
            }

            @Override
            public Image decorateImage(Image image, Object element) {
                return null;
            }
        };

        viewer.setLabelProvider(new TableDecoratingLabelProvider(labelProvider, labelDecorator));
    }

    private void createContextMenu() {
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

                menuManager.appendToGroup("group.show", new Action("Expand All") { //$NON-NLS-1$

                    @Override
                    public void run() {
                        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                        for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                            viewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
                        }
                    }
                });
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
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(model.getClass())) {
            return model;
        }
        return super.getAdapter(adapter);
    }
}
