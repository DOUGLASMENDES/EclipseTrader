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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

public class DataViewerLabelProvider extends ObservableMapLabelProvider {

    private DateFormat dateFormat;
    private NumberFormat decimalNumberFormat = NumberFormat.getInstance();
    private NumberFormat integerNumberFormat = NumberFormat.getInstance();

    public DataViewerLabelProvider(IObservableMap attributeMap) {
        this(new IObservableMap[] {
            attributeMap
        });
    }

    public DataViewerLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);

        decimalNumberFormat.setGroupingUsed(true);
        decimalNumberFormat.setMinimumIntegerDigits(1);
        decimalNumberFormat.setMinimumFractionDigits(1);
        decimalNumberFormat.setMaximumFractionDigits(4);

        integerNumberFormat.setGroupingUsed(true);
        integerNumberFormat.setMinimumIntegerDigits(1);
        integerNumberFormat.setMinimumFractionDigits(0);
        integerNumberFormat.setMaximumFractionDigits(0);
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        String text = "";
        if (columnIndex < attributeMaps.length) {
            Object result = attributeMaps[columnIndex].get(element);
            if (result instanceof Date) {
                text = dateFormat.format(result);
            }
            else if ((result instanceof Long) || (result instanceof Integer)) {
                text = integerNumberFormat.format(result);
            }
            else if (result instanceof Number) {
                text = decimalNumberFormat.format(result);
            }
            else if (result != null) {
                text = result.toString();
            }
        }
        return text;
    }
}
