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
import java.util.Date;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.CellEditorValueProperty;
import org.eclipsetrader.ui.Util;

public class HistoryDataDetailEditor {

    public static final String PROP_DIRTY = "dirty";

    private final HistoryDataDetailEditorModel model;
    private final DataBindingContext dbc;

    private TreeViewer viewer;

    private boolean dirty;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final PropertyChangeListener dirtyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
        }
    };

    public HistoryDataDetailEditor(Composite parent) {
        dbc = new DataBindingContext();

        model = new HistoryDataDetailEditorModel();
        model.addPropertyChangeListener(dirtyChangeListener);

        createViewer(parent);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    void createViewer(Composite parent) {
        Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.MULTI);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        viewer = new TreeViewer(tree);

        IObservableFactory observableFactory = new IObservableFactory() {

            @Override
            public IObservable createObservable(Object target) {
                if (target instanceof HistoryDataDetailEditorModel) {
                    return Observables.unmodifiableObservableList(((HistoryDataDetailEditorModel) target).getList());
                }
                if (target instanceof HistoryDataEditorModel) {
                    return Observables.unmodifiableObservableList(((HistoryDataEditorModel) target).getList());
                }
                return null;
            }
        };

        TreeStructureAdvisor treeStructureAdvisor = new TreeStructureAdvisor() {

            @Override
            public Boolean hasChildren(Object element) {
                if (element instanceof HistoryDataEditorModel) {
                    return true;
                }
                return false;
            }
        };

        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(observableFactory, treeStructureAdvisor);
        viewer.setContentProvider(contentProvider);

        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
        viewerColumn.getColumn().setText("Time");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 70));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(tree), CellEditorValueProperty.timeValue(), BeanProperties.value(HistoryDataElement.PROP_DATE)));

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Open");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(tree), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_OPEN)));

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("High");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(tree), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_HIGH)));

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Low");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(tree), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_LOW)));

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Close");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(tree), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_CLOSE)));

        viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Volume");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 70));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(tree), CellEditorValueProperty.longValue(), BeanProperties.value(HistoryDataElement.PROP_VOLUME)));

        if (Platform.WS_GTK.equals(Platform.getWS())) {
            viewerColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
            viewerColumn.getColumn().setWidth(1);
        }

        final String[] properties = new String[] {
            HistoryDataElement.PROP_DATE,
            HistoryDataElement.PROP_OPEN,
            HistoryDataElement.PROP_HIGH,
            HistoryDataElement.PROP_LOW,
            HistoryDataElement.PROP_CLOSE,
            HistoryDataElement.PROP_VOLUME,
            ""
        };

        DataViewerLabelProvider labelProvider = new DataViewerLabelProvider(BeansObservables.observeMaps(contentProvider.getKnownElements(), properties));
        labelProvider.setDateFormat(Util.getTimeFormat());
        viewer.setLabelProvider(labelProvider);

        viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                if ((o1 instanceof HistoryDataEditorModel) && (o2 instanceof HistoryDataEditorModel)) {
                    TimeSpan ts1 = ((HistoryDataEditorModel) o1).getBarSize();
                    TimeSpan ts2 = ((HistoryDataEditorModel) o2).getBarSize();
                    if (ts1.higherThan(ts2)) {
                        return 1;
                    }
                    if (ts2.higherThan(ts1)) {
                        return -1;
                    }
                    return 0;
                }
                else if ((o1 instanceof HistoryDataElement) && (o2 instanceof HistoryDataElement)) {
                    HistoryDataElement e1 = (HistoryDataElement) o1;
                    HistoryDataElement e2 = (HistoryDataElement) o2;
                    if (e1.getDate() != null && e2.getDate() != null) {
                        return e1.getDate().compareTo(e2.getDate());
                    }
                    if (e1.getDate() != null && e2.getDate() == null) {
                        return -1;
                    }
                    if (e1.getDate() == null && e2.getDate() != null) {
                        return 1;
                    }
                }
                return 0;
            }
        });

        CellNavigationStrategy naviStrat = new CellNavigationStrategy() {

            @Override
            public ViewerCell findSelectedCell(ColumnViewer viewer, ViewerCell currentSelectedCell, Event event) {
                ViewerCell cell = super.findSelectedCell(viewer, currentSelectedCell, event);
                if (cell != null) {
                    if (cell.getColumnIndex() == properties.length - 1) {
                        return null;
                    }
                    Tree tree = ((TreeViewer) viewer).getTree();
                    tree.showColumn(tree.getColumn(cell.getColumnIndex()));
                }
                return cell;
            }
        };

        TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer), naviStrat);

        ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {

            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR) || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;

            }
        };
        TreeViewerEditor.create(viewer, focusCellManager, activationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        tree.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_ESCAPE) {
                    e.doit = false;
                }
            }
        });
        tree.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (viewer.isCellEditorActive()) {
                    return;
                }
                if (e.keyCode == SWT.INSERT) {
                    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

                    HistoryDataEditorModel parent = null;

                    int index = 0;
                    if (!selection.isEmpty()) {
                        parent = model.getParent(selection.getFirstElement());
                        if (parent == null) {
                            return;
                        }
                        index = parent.getList().indexOf(selection.getFirstElement());
                        if (index == -1) {
                            index = parent.getList().size();
                        }
                    }

                    final HistoryDataElement element = new HistoryDataElement();
                    parent.getList().add(index, element);

                    e.doit = false;

                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                viewer.editElement(element, 0);
                            }
                        }
                    });
                }
                else if (e.character == SWT.DEL) {
                    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                    for (Object element : selection.toList()) {
                        HistoryDataEditorModel parent = model.getParent(selection.getFirstElement());
                        if (parent != null) {
                            if (parent.getList().remove(element)) {
                                changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
                            }
                        }
                    }
                }
            }
        });

        viewer.setInput(model);
    }

    public void load(IHistory history, Date date) {
        IHistory[] childHistory = history.getDay(date);
        model.set(childHistory);
    }

    public Control getControl() {
        return viewer.getControl();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public HistoryDataDetailEditorModel getModel() {
        return model;
    }

    public TreeViewer getViewer() {
        return viewer;
    }

    public void dispose() {
        PropertyChangeListener[] listeners = changeSupport.getPropertyChangeListeners();
        for (int i = 0; i < listeners.length; i++) {
            changeSupport.removePropertyChangeListener(listeners[i]);
        }

        viewer.getControl().dispose();

        model.dispose();

        dbc.dispose();
    }
}
