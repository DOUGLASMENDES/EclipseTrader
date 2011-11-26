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

package org.eclipsetrader.ui.internal.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.views.IEditableDataProvider;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.ats.ViewColumn;
import org.eclipsetrader.ui.navigator.RepositoryObjectTransfer;
import org.eclipsetrader.ui.navigator.SecurityObjectTransfer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WatchListView extends ViewPart implements ISaveablePart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.views.watchlist";

    private static final String K_VIEWS = "Views";
    private static final String K_URI = "uri";
    private static final String K_SORT_COLUMN = "sortColumn";
    private static final String K_SORT_DIRECTION = "sortDirection";
    private static final String COLUMNS_SECTION = "columns";

    private URI uri;
    WatchList watchList;

    private TableViewer viewer;
    WatchListViewModel model;
    private WatchListViewTickDecorator tickDecorator;

    private Action deleteAction;
    private Action settingsAction;

    private IDialogSettings dialogSettings;
    private IDialogSettings columnsSection;
    private IRepositoryService repositoryService;
    private MarketPricingEnvironment pricingEnvironment;

    private Color evenRowsColor;
    private Color oddRowsColor;
    private Color positiveTickColor;
    private Color negativeTickColor;

    private int sortColumn = 0;
    private int sortDirection = SWT.UP;
    IPreferenceStore preferenceStore;
    IThemeManager themeManager;

    private ControlAdapter columnControlListener = new ControlAdapter() {

        @Override
        public void controlResized(ControlEvent e) {
            TableColumn tableColumn = (TableColumn) e.widget;
            if (dialogSettings != null) {
                IDialogSettings columnsSection = dialogSettings.getSection(COLUMNS_SECTION);
                if (columnsSection == null) {
                    columnsSection = dialogSettings.addNewSection(COLUMNS_SECTION);
                }
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
            if (table.getSortColumn() == tableColumn) {
                sortDirection = sortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
            }
            else {
                sortDirection = SWT.UP;
                table.setSortColumn(table.getColumn(sortColumn));
            }
            table.setSortDirection(sortDirection);

            WatchListViewColumn column = model.getColumns().get(sortColumn);
            dialogSettings.put(K_SORT_COLUMN, column.getId());
            dialogSettings.put(K_SORT_DIRECTION, sortDirection == SWT.UP ? 1 : -1);
            viewer.refresh();
            updateBackgrounds();
        }
    };

    private final PropertyChangeListener modelChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (WatchListViewModel.PROP_NAME.equals(evt.getPropertyName())) {
                setPartName((String) evt.getNewValue());
            }
            else if (WatchListViewModel.PROP_DIRTY.equals(evt.getPropertyName())) {
                firePropertyChange(PROP_DIRTY);
            }
        }
    };

    private final IPropertyChangeListener preferencesChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND.equals(event.getProperty())) {
                boolean enabled = ((Boolean) event.getNewValue()).booleanValue();
                tickDecorator.setRowColors(evenRowsColor, enabled ? oddRowsColor : evenRowsColor);
                updateBackgrounds();
            }
            else if (UIActivator.PREFS_WATCHLIST_ENABLE_TICK_DECORATORS.equals(event.getProperty())) {
                boolean enabled = ((Boolean) event.getNewValue()).booleanValue();
                tickDecorator.setEnabled(enabled);
            }
            else if (UIActivator.PREFS_WATCHLIST_DRAW_TICK_OUTLINE.equals(event.getProperty())) {
                boolean enabled = ((Boolean) event.getNewValue()).booleanValue();
                tickDecorator.setDrawOutline(enabled);
            }
            else if (UIActivator.PREFS_WATCHLIST_FADE_TO_BACKGROUND.equals(event.getProperty())) {
                boolean enabled = ((Boolean) event.getNewValue()).booleanValue();
                tickDecorator.setFadeEffect(enabled);
            }
            else if (UIActivator.PREFS_WATCHLIST_POSITIVE_TICK_COLOR.equals(event.getProperty())) {
                if (positiveTickColor != null) {
                    positiveTickColor.dispose();
                    positiveTickColor = null;
                }
                RGB rgb = (RGB) event.getNewValue();
                if (rgb != null) {
                    positiveTickColor = new Color(Display.getDefault(), rgb);
                }
                tickDecorator.setTickColors(positiveTickColor, negativeTickColor);
            }
            else if (UIActivator.PREFS_WATCHLIST_NEGATIVE_TICK_COLOR.equals(event.getProperty())) {
                if (negativeTickColor != null) {
                    negativeTickColor.dispose();
                    negativeTickColor = null;
                }
                RGB rgb = (RGB) event.getNewValue();
                if (rgb != null) {
                    negativeTickColor = new Color(Display.getDefault(), rgb);
                }
                tickDecorator.setTickColors(positiveTickColor, negativeTickColor);
            }
            else if (IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) {
                ITheme oldTheme = (ITheme) event.getOldValue();
                if (oldTheme != null) {
                    oldTheme.removePropertyChangeListener(preferencesChangeListener);
                }

                if (positiveTickColor != null) {
                    positiveTickColor.dispose();
                    positiveTickColor = null;
                }
                if (negativeTickColor != null) {
                    negativeTickColor.dispose();
                    negativeTickColor = null;
                }

                ITheme newTheme = (ITheme) event.getOldValue();
                if (newTheme != null) {
                    RGB rgb = newTheme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_POSITIVE_TICK_COLOR);
                    if (rgb != null) {
                        positiveTickColor = new Color(Display.getDefault(), rgb);
                    }
                    rgb = newTheme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_NEGATIVE_TICK_COLOR);
                    if (rgb != null) {
                        negativeTickColor = new Color(Display.getDefault(), rgb);
                    }
                    newTheme.addPropertyChangeListener(preferencesChangeListener);
                }

                tickDecorator.setTickColors(positiveTickColor, negativeTickColor);
            }
        }
    };

    public WatchListView() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();
        BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        repositoryService = bundleContext.getService(serviceReference);

        try {
            dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(K_VIEWS).getSection(site.getSecondaryId());
            uri = new URI(dialogSettings.get(K_URI));
            IWatchList watchList = repositoryService.getWatchListFromURI(uri);
            if (watchList instanceof WatchList) {
                this.watchList = (WatchList) watchList;
            }
        } catch (Exception e) {
            if (uri == null || watchList == null) {
                throw new PartInitException(NLS.bind("Unable to load view {0}", new Object[] {
                    uri != null ? uri.toString() : ""
                }), e);
            }
        }
        if (uri == null || watchList == null) {
            throw new PartInitException(NLS.bind("Unable to load view {0}", new Object[] {
                uri != null ? uri.toString() : ""
            }));
        }

        columnsSection = dialogSettings.getSection(COLUMNS_SECTION);
        if (columnsSection == null) {
            columnsSection = dialogSettings.addNewSection(COLUMNS_SECTION);
        }

        preferenceStore = UIActivator.getDefault().getPreferenceStore();

        pricingEnvironment = new MarketPricingEnvironment(UIActivator.getDefault().getMarketService());

        deleteAction = new Action("Delete") {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (selection.size() != 0) {
                    model.getObservableItems().removeAll(selection.toList());
                }
            }
        };
        deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_ICON));
        deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_DISABLED_ICON));
        deleteAction.setId(ActionFactory.DELETE.getId());
        deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
        deleteAction.setEnabled(false);

        settingsAction = new SettingsAction(site.getShell(), this);

        IActionBars actionBars = site.getActionBars();
        actionBars.setGlobalActionHandler(settingsAction.getId(), settingsAction);
        actionBars.setGlobalActionHandler(deleteAction.getId(), deleteAction);
        actionBars.updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        setPartName(watchList.getName());

        model = new WatchListViewModel(watchList, pricingEnvironment);

        createViewer(parent);
        initializeContextMenu();

        applyPreferences();

        if (sortColumn >= viewer.getTable().getColumnCount()) {
            sortColumn = 0;
            sortDirection = SWT.UP;
        }
        if (sortColumn < viewer.getTable().getColumnCount()) {
            viewer.getTable().setSortDirection(sortDirection);
            viewer.getTable().setSortColumn(viewer.getTable().getColumn(sortColumn));
        }

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                deleteAction.setEnabled(!event.getSelection().isEmpty());
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
                final IAdaptable[] contents = (IAdaptable[]) data;
                for (int i = 0; i < contents.length; i++) {
                    ISecurity security = (ISecurity) contents[i].getAdapter(ISecurity.class);
                    if (security != null) {
                        pricingEnvironment.addSecurity(security);
                        model.add(security);
                    }
                }
                return true;
            }
        });

        model.addPropertyChangeListener(WatchListViewModel.PROP_DIRTY, modelChangeListener);

        preferenceStore.addPropertyChangeListener(preferencesChangeListener);

        getSite().setSelectionProvider(viewer);

        Job job = new Job(watchList.getName() + " Startup") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                Set<ISecurity> list = new HashSet<ISecurity>();
                for (IWatchListElement element : watchList.getItems()) {
                    list.add(element.getSecurity());
                }
                pricingEnvironment.addSecurities(list.toArray(new ISecurity[list.size()]));

                model.init();

                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    TableViewer createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(false);

        final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
        viewer.setContentProvider(contentProvider);

        tickDecorator = new WatchListViewTickDecorator(contentProvider.getKnownElements());

        int index = 0;
        for (WatchListViewColumn column : model.getColumns()) {
            createViewerColumn(column, tickDecorator);
            if (dialogSettings != null) {
                if (column.getId().equals(dialogSettings.get(K_SORT_COLUMN))) {
                    sortColumn = index;
                }
            }
            index++;
        }

        model.getObservableColumns().addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                event.diff.accept(new ListDiffVisitor() {

                    @Override
                    public void handleAdd(int index, Object element) {
                        WatchListViewColumn column = (WatchListViewColumn) element;
                        createViewerColumn(index, column, tickDecorator);
                        if (dialogSettings != null && column.getId().equals(dialogSettings.get(K_SORT_COLUMN))) {
                            sortColumn = index;
                            viewer.getTable().setSortDirection(sortDirection);
                            viewer.getTable().setSortColumn(viewer.getTable().getColumn(sortColumn));
                        }
                    }

                    @Override
                    public void handleRemove(int index, Object element) {
                        WatchListViewColumn column = (WatchListViewColumn) element;
                        if (dialogSettings != null && column.getId().equals(dialogSettings.get(K_SORT_COLUMN))) {
                            sortColumn = -1;
                        }
                        TableColumn tableColumn = viewer.getTable().getColumn(index);
                        tableColumn.dispose();
                    }
                });

                viewer.refresh();

                updateBackgrounds();
            }
        });

        viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (sortColumn < 0 || sortColumn >= model.getColumns().size()) {
                    return 0;
                }
                String propertyName = model.getColumns().get(sortColumn).getId();
                IAdaptable v1 = (IAdaptable) ((WatchListViewItem) e1).getValue(propertyName);
                IAdaptable v2 = (IAdaptable) ((WatchListViewItem) e2).getValue(propertyName);
                if (sortDirection == SWT.DOWN) {
                    v1 = (IAdaptable) ((WatchListViewItem) e2).getValue(propertyName);
                    v2 = (IAdaptable) ((WatchListViewItem) e1).getValue(propertyName);
                }
                return compareValues(v1, v2);
            }
        });

        viewer.setInput(model.getObservableItems());

        model.getObservableItems().addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        updateBackgrounds();
                    }
                });
            }
        });

        viewer.getControl().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                tickDecorator.dispose();
            }
        });

        return viewer;
    }

    private void createViewerColumn(WatchListViewColumn column, WatchListViewTickDecorator tickDecorator) {
        createViewerColumn(-1, column, tickDecorator);
    }

    private void createViewerColumn(int index, final WatchListViewColumn column, WatchListViewTickDecorator tickDecorator) {
        int alignment = SWT.LEFT;

        Class<?>[] type = column.getDataProviderFactory().getType();
        if (type != null && type.length != 0) {
            if (type[0] == Long.class || type[0] == Double.class || type[0] == Date.class) {
                alignment = SWT.RIGHT;
            }
            if (type[0] == Image.class || type[0] == ImageData.class) {
                alignment = SWT.CENTER;
            }
        }

        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, alignment, index);
        viewerColumn.getColumn().setText(column.getName() != null ? column.getName() : column.getDataProviderFactory().getName());

        int width = columnsSection != null && columnsSection.get(viewerColumn.getColumn().getText()) != null ? columnsSection.getInt(viewerColumn.getColumn().getText()) : 100;
        viewerColumn.getColumn().setWidth(width);

        viewerColumn.getColumn().addControlListener(columnControlListener);
        viewerColumn.getColumn().addSelectionListener(columnSelectionAdapter);

        viewerColumn.setLabelProvider(tickDecorator.createCellLabelProvider(column.getId()));

        if (column.getDataProvider() instanceof IEditableDataProvider) {
            viewerColumn.setEditingSupport(new WatchListColumEditingSupport(viewer, (IEditableDataProvider) column.getDataProvider(), column.getId()));
        }

        final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String text = (String) evt.getNewValue();
                if (text == null) {
                    WatchListViewColumn source = (WatchListViewColumn) evt.getSource();
                    text = source.getDataProviderFactory().getName();
                }
                viewerColumn.getColumn().setText(text);
            }
        };
        column.addPropertyChangeListener(ViewColumn.PROP_NAME, propertyChangeListener);

        viewerColumn.getColumn().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                column.removePropertyChangeListener(ViewColumn.PROP_NAME, propertyChangeListener);
            }
        });
    }

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    protected int compareValues(IAdaptable v1, IAdaptable v2) {
        Number n1 = (Number) (v1 != null ? v1.getAdapter(Number.class) : null);
        Number n2 = (Number) (v2 != null ? v2.getAdapter(Number.class) : null);
        if (n1 != null && n2 != null) {
            if (n1.doubleValue() < n2.doubleValue()) {
                return -1;
            }
            if (n1.doubleValue() > n2.doubleValue()) {
                return 1;
            }
            return 0;
        }
        if (n1 != null && n2 == null) {
            return 1;
        }
        if (n1 == null && n2 != null) {
            return -1;
        }

        Comparable c1 = (Comparable) (v1 != null ? v1.getAdapter(Comparable.class) : null);
        Comparable c2 = (Comparable) (v2 != null ? v2.getAdapter(Comparable.class) : null);
        if (c1 != null && c2 != null) {
            return c1.compareTo(c2);
        }
        if (c1 != null && c2 == null) {
            return 1;
        }
        if (c1 == null && c2 != null) {
            return -1;
        }

        return 0;
    }

    private void initializeContextMenu() {
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

                menuManager.appendToGroup("group.edit", deleteAction);
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
    }

    private void applyPreferences() {
        RGB rgb = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
        if (rgb.red > 0) {
            rgb.red--;
        }
        if (rgb.green > 0) {
            rgb.green--;
        }
        if (rgb.blue > 0) {
            rgb.blue--;
        }
        evenRowsColor = new Color(Display.getDefault(), rgb);

        rgb = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW).getRGB();
        if (rgb.red > 0) {
            rgb.red--;
        }
        if (rgb.green > 0) {
            rgb.green--;
        }
        if (rgb.blue > 0) {
            rgb.blue--;
        }
        oddRowsColor = new Color(Display.getDefault(), rgb);

        themeManager = PlatformUI.getWorkbench().getThemeManager();
        themeManager.addPropertyChangeListener(preferencesChangeListener);

        ITheme theme = themeManager.getCurrentTheme();
        if (theme != null) {
            rgb = theme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_POSITIVE_TICK_COLOR);
            if (rgb != null) {
                positiveTickColor = new Color(Display.getDefault(), rgb);
            }
            rgb = theme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_NEGATIVE_TICK_COLOR);
            if (rgb != null) {
                negativeTickColor = new Color(Display.getDefault(), rgb);
            }
            theme.addPropertyChangeListener(preferencesChangeListener);
        }

        tickDecorator.setRowColors(evenRowsColor, preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND) ? oddRowsColor : evenRowsColor);
        tickDecorator.setTickColors(positiveTickColor, negativeTickColor);
        tickDecorator.setDrawOutline(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_DRAW_TICK_OUTLINE));
        tickDecorator.setFadeEffect(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_FADE_TO_BACKGROUND));
        tickDecorator.setEnabled(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_ENABLE_TICK_DECORATORS));

        updateBackgrounds();
    }

    private void updateBackgrounds() {
        TableItem[] tableItem = viewer.getTable().getItems();
        if (preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND)) {
            for (int i = 0; i < tableItem.length; i++) {
                tableItem[i].setBackground((i & 1) != 0 ? oddRowsColor : evenRowsColor);
            }
        }
        else {
            for (int i = 0; i < tableItem.length; i++) {
                tableItem[i].setBackground(evenRowsColor);
            }
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
        BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();
        bundleContext.ungetService(bundleContext.getServiceReference(IRepositoryService.class));

        preferenceStore.removePropertyChangeListener(preferencesChangeListener);

        pricingEnvironment.dispose();

        tickDecorator.dispose();

        evenRowsColor.dispose();
        oddRowsColor.dispose();
        if (positiveTickColor != null) {
            positiveTickColor.dispose();
        }
        if (negativeTickColor != null) {
            negativeTickColor.dispose();
        }

        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        model.commit();

        final IRepositoryService repositoryService = UIActivator.getDefault().getRepositoryService();
        IStatus status = repositoryService.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryService.saveAdaptable(new IAdaptable[] {
                    watchList
                });
                return Status.OK_STATUS;
            }
        }, monitor);

        if (status == Status.OK_STATUS) {
            model.setDirty(false);
        }
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
        return model.isDirty();
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
        return model.isDirty();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IWatchList.class)) {
            return watchList;
        }

        if (adapter.isAssignableFrom(model.getClass())) {
            return model;
        }

        if (watchList != null) {
            Object obj = watchList.getAdapter(adapter);
            if (obj != null) {
                return obj;
            }
        }

        return super.getAdapter(adapter);
    }
}
