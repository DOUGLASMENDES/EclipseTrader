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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.repositories.IPropertyConstants;

public class HistoryDataDetailEditorModel {

    private IHistory[] history;
    private PropertyChangeListener[] dataChangeListener;

    private final Map<IHistory, HistoryDataEditorModel> map = new HashMap<IHistory, HistoryDataEditorModel>();
    private final WritableList list = new WritableList();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final IListChangeListener listChangeListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            event.diff.accept(new ListDiffVisitor() {

                @Override
                public void handleRemove(int index, Object element) {
                    PropertyChangeListener[] listeners = changeSupport.getPropertyChangeListeners();
                    for (int i = 0; i < listeners.length; i++) {
                        ((HistoryDataElement) element).removePropertyChangeListener(listeners[i]);
                    }
                }

                @Override
                public void handleAdd(int index, Object element) {
                    PropertyChangeListener[] listeners = changeSupport.getPropertyChangeListeners();
                    for (int i = 0; i < listeners.length; i++) {
                        ((HistoryDataElement) element).addPropertyChangeListener(listeners[i]);
                    }
                }
            });
        }
    };

    public HistoryDataDetailEditorModel() {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void set(IHistory[] history) {
        this.history = history;

        dataChangeListener = new PropertyChangeListener[history.length];

        for (int i = 0; i < history.length; i++) {
            final HistoryDataEditorModel model = new HistoryDataEditorModel(history[i].getTimeSpan());
            model.set(history[i].getOHLC());

            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history[i].getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                dataChangeListener[i] = new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (IPropertyConstants.BARS.equals(evt.getPropertyName())) {
                            final IOHLC[] newBars = (IOHLC[]) evt.getNewValue();
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    model.merge(newBars);
                                }
                            });
                        }
                    }
                };
                propertyChangeSupport.addPropertyChangeListener(dataChangeListener[i]);
            }

            model.getList().addListChangeListener(listChangeListener);

            map.put(history[i], model);
        }

        list.addAll(map.values());
    }

    public WritableList getList() {
        return list;
    }

    public Map<IHistory, HistoryDataEditorModel> getMap() {
        return map;
    }

    public void dispose() {
        for (int i = 0; i < history.length; i++) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history[i].getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(dataChangeListener[i]);
            }
        }
    }

    public HistoryDataEditorModel getParent(Object element) {
        for (HistoryDataEditorModel model : map.values()) {
            if (model.getList().contains(element)) {
                return model;
            }
        }
        return null;
    }
}
