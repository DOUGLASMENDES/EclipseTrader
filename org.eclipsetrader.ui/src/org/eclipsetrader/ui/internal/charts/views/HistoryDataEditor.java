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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Table;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.ui.CellEditorValueProperty;
import org.eclipsetrader.ui.Util;

public class HistoryDataEditor {

    public static final String PROP_DIRTY = "dirty";

    private final HistoryDataEditorModel model;
    private final DataBindingContext dbc;

    private TableViewer viewer;

    private IHistory history;
    private boolean dirty;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final PropertyChangeListener dirtyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
        }
    };

    private final IListChangeListener listChangeListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            event.diff.accept(new ListDiffVisitor() {

                @Override
                public void handleRemove(int index, Object element) {
                    ((HistoryDataElement) element).removePropertyChangeListener(dirtyChangeListener);
                }

                @Override
                public void handleAdd(int index, Object element) {
                    ((HistoryDataElement) element).addPropertyChangeListener(dirtyChangeListener);
                }
            });
        }
    };

    private final PropertyChangeListener dataChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (IPropertyConstants.BARS.equals(evt.getPropertyName())) {
                final IOHLC[] newBars = (IOHLC[]) evt.getNewValue();
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (viewer.getControl().isDisposed()) {
                            return;
                        }
                        model.merge(newBars);
                    }
                });
            }
        }
    };

    public HistoryDataEditor(Composite parent) {
        dbc = new DataBindingContext();

        model = new HistoryDataEditorModel(TimeSpan.days(1));
        model.getList().addListChangeListener(listChangeListener);

        createViewer(parent);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    void createViewer(Composite parent) {
        Table table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer = new TableViewer(table);

        ObservableListContentProvider contentProvider = new ObservableListContentProvider();
        viewer.setContentProvider(contentProvider);

        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        viewerColumn.getColumn().setText("Date");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 70));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(table), CellEditorValueProperty.dateValue(), BeanProperties.value(HistoryDataElement.PROP_DATE)));

        viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Open");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(table), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_OPEN)));

        viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("High");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(table), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_HIGH)));

        viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Low");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(table), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_LOW)));

        viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Close");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 55));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(table), CellEditorValueProperty.doubleValue(), BeanProperties.value(HistoryDataElement.PROP_CLOSE)));

        viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
        viewerColumn.getColumn().setText("Volume");
        viewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 70));
        viewerColumn.setEditingSupport(ObservableValueEditingSupport.create(viewer, dbc,
            new TextCellEditor(table), CellEditorValueProperty.longValue(), BeanProperties.value(HistoryDataElement.PROP_VOLUME)));

        if (Platform.WS_GTK.equals(Platform.getWS())) {
            viewerColumn = new TableViewerColumn(viewer, SWT.RIGHT);
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
        labelProvider.setDateFormat(Util.getDateFormat());
        viewer.setLabelProvider(labelProvider);

        viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
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
                    Table table = ((TableViewer) viewer).getTable();
                    table.showColumn(table.getColumn(cell.getColumnIndex()));
                }
                return cell;
            }
        };

        TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer), naviStrat);

        ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {

            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR) || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;

            }
        };
        TableViewerEditor.create(viewer, focusCellManager, activationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        table.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_ESCAPE) {
                    e.doit = false;
                }
            }
        });
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (viewer.isCellEditorActive()) {
                    return;
                }
                if (e.keyCode == SWT.INSERT) {
                    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

                    int index = 0;
                    if (!selection.isEmpty()) {
                        index = model.getList().indexOf(selection.getFirstElement());
                        if (index == -1) {
                            index = model.getList().size();
                        }
                    }

                    final HistoryDataElement element = new HistoryDataElement();
                    model.getList().add(index, element);

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
                    if (model.getList().removeAll(selection.toList())) {
                        changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
                    }
                }
            }
        });

        viewer.setInput(model.getList());
    }

    public void load(ISecurity security) {
        if (history != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(dataChangeListener);
            }
        }

        ChartLoadJob job = new ChartLoadJob(security);
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                ChartLoadJob job = (ChartLoadJob) event.getJob();

                history = job.getHistory();

                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        model.set(history.getOHLC());

                        PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
                        if (propertyChangeSupport != null) {
                            propertyChangeSupport.addPropertyChangeListener(dataChangeListener);
                        }
                    }
                });
            }
        });
        job.setResolutionTimeSpan(TimeSpan.days(1));
        job.setName(NLS.bind("Loading {0} History...", new Object[] {
            security.getName()
        }));
        job.setUser(true);
        job.schedule();
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

    public HistoryDataEditorModel getModel() {
        return model;
    }

    public TableViewer getViewer() {
        return viewer;
    }

    public IHistory getHistory() {
        return history;
    }

    public void dispose() {
        if (history != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(dataChangeListener);
            }
        }

        PropertyChangeListener[] listeners = changeSupport.getPropertyChangeListeners();
        for (int i = 0; i < listeners.length; i++) {
            changeSupport.removePropertyChangeListener(listeners[i]);
        }

        viewer.getControl().dispose();

        model.getList().removeListChangeListener(listChangeListener);
        model.dispose();

        dbc.dispose();
    }
}
