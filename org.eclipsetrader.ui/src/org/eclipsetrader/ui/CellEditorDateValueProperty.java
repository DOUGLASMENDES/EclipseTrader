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

package org.eclipsetrader.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;

public class CellEditorDateValueProperty extends ValueProperty {

    DateFormat dateFormat = Util.getDateFormat();

    private class CellEditorObservable extends AbstractObservableValue {

        final CellEditor cellEditor;

        Object oldValue;

        final ICellEditorListener cellEditorListener = new ICellEditorListener() {

            @Override
            public void applyEditorValue() {
            }

            @Override
            public void cancelEditor() {
            }

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                fireChangeEvent();
            }
        };

        public CellEditorObservable(Realm realm, CellEditor cellEditor) {
            this.cellEditor = cellEditor;
            cellEditor.addListener(cellEditorListener);
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.databinding.observable.AbstractObservable#dispose()
         */
        @Override
        public synchronized void dispose() {
            cellEditor.removeListener(cellEditorListener);
            super.dispose();
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
         */
        @Override
        public Object getValueType() {
            return Date.class;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doSetValue(java.lang.Object)
         */
        @Override
        protected void doSetValue(Object value) {
            if (value instanceof Date) {
                cellEditor.setValue(dateFormat.format(value));
                oldValue = value;
            }
            else {
                cellEditor.setValue("");
                oldValue = null;
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doGetValue()
         */
        @Override
        protected Object doGetValue() {
            String text = (String) cellEditor.getValue();
            try {
                if (text != null && !"".equals(text)) {
                    return dateFormat.parse(text);
                }
            } catch (ParseException e) {
                // Ignore
            }
            return null;
        }

        private void fireChangeEvent() {
            Object newValue = doGetValue();
            if (!equals(oldValue, newValue)) {
                fireValueChange(Diffs.createValueDiff(oldValue, newValue));
                oldValue = newValue;
            }
        }

        private boolean equals(final Object left, final Object right) {
            return left == null ? right == null : ((right != null) && left.equals(right));
        }
    }

    public CellEditorDateValueProperty() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.property.value.IValueProperty#getValueType()
     */
    @Override
    public Object getValueType() {
        return Date.class;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.property.value.IValueProperty#observe(org.eclipse.core.databinding.observable.Realm, java.lang.Object)
     */
    @Override
    public IObservableValue observe(Realm realm, Object source) {
        return new CellEditorObservable(realm, (CellEditor) source);
    }
}
