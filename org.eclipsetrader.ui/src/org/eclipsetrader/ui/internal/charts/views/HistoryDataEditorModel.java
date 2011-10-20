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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public class HistoryDataEditorModel {

    private final TimeSpan barSize;
    private final WritableList list = new WritableList();

    private HistoryDataElement lastElement;

    private final PropertyChangeListener changeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final HistoryDataElement element = (HistoryDataElement) evt.getSource();
            if (element.isEmpty()) {
                if (element != lastElement) {
                    list.remove(element);
                }
            }
            else {
                if (element == lastElement) {
                    list.add(lastElement = new HistoryDataElement());
                }
            }
        }
    };

    private final IListChangeListener listChangeListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            event.diff.accept(new ListDiffVisitor() {

                @Override
                public void handleRemove(int index, Object element) {
                    ((HistoryDataElement) element).removePropertyChangeListener(changeListener);
                }

                @Override
                public void handleAdd(int index, Object element) {
                    ((HistoryDataElement) element).addPropertyChangeListener(changeListener);
                }
            });
        }
    };

    public HistoryDataEditorModel(TimeSpan barSize) {
        this.barSize = barSize;
        list.addListChangeListener(listChangeListener);
        list.add(lastElement = new HistoryDataElement());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // Do nothing
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // Do nothing
    }

    public void dispose() {
        list.removeListChangeListener(listChangeListener);
    }

    public void set(IOHLC[] ohlc) {
        list.clear();

        List<HistoryDataElement> l = new ArrayList<HistoryDataElement>();
        for (int i = 0; i < ohlc.length; i++) {
            l.add(new HistoryDataElement(ohlc[i]));
        }
        list.addAll(l);

        list.add(lastElement = new HistoryDataElement());
    }

    public void merge(IOHLC[] ohlc) {
        Map<Date, HistoryDataElement> map = new HashMap<Date, HistoryDataElement>();

        for (Object o : list) {
            Date date = ((HistoryDataElement) o).getDate();
            if (date != null) {
                map.put(date, (HistoryDataElement) o);
            }
        }

        for (int i = 0; i < ohlc.length; i++) {
            HistoryDataElement oldElement = map.get(ohlc[i].getDate());
            if (oldElement != null) {
                if (!oldElement.equalsTo(ohlc[i])) {
                    int index = list.indexOf(oldElement);
                    list.set(index, new HistoryDataElement(ohlc[i]));
                }
            }
            else {
                list.add(new HistoryDataElement(ohlc[i]));
            }
        }
    }

    public IOHLC[] toOHLC() {
        List<IOHLC> l = new ArrayList<IOHLC>();
        for (Object o : this.list) {
            HistoryDataElement element = (HistoryDataElement) o;
            if (element.isValid()) {
                l.add(element.toOHLC());
            }
        }
        return l.toArray(new IOHLC[l.size()]);
    }

    public WritableList getList() {
        return list;
    }

    public TimeSpan getBarSize() {
        return barSize;
    }

    public String getDate() {
        return barSize.getDescription();
    }

    public HistoryDataElement getLastElement() {
        return lastElement;
    }
}
