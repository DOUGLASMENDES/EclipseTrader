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
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.core.internal.ats.TradingSystemProperties;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.ats.ViewColumn;
import org.eclipsetrader.ui.internal.ats.ViewerObservableMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradingSystemsViewPart extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.views.ats.monitor";

    private ITradingSystemService tradingSystemService;
    private TreeViewer viewer;
    private TradingSystemsViewModel model;

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
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(false);

        getSite().setSelectionProvider(viewer);
        createContextMenu();

        model = new TradingSystemsViewModel(tradingSystemService);
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
                                viewer.update(item, new String[] {
                                    "_label_"
                                });
                            }
                        });
                    }
                });
            }
        }

        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(model, model);
        viewer.setContentProvider(contentProvider);

        TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
        viewerColumn.getColumn().setWidth(300);

        List<IObservableMap> properties = new ArrayList<IObservableMap>();
        properties.add(new ViewerObservableMap(contentProvider.getKnownElements(), "_label_"));
        for (ViewColumn column : model.getDataProviders()) {
            viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
            viewerColumn.getColumn().setText(column.getName());
            viewerColumn.getColumn().setWidth(100);
            properties.add(new ViewerObservableMap(contentProvider.getKnownElements(), column.getDataProviderFactory().getId()));
        }

        ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(properties.toArray(new IObservableMap[properties.size()])) {

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
                return "_label_".equals(property);
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

        viewer.setComparator(new ViewerComparator());

        viewer.setInput(model);
        viewer.expandAll();
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
}
