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
import java.util.Date;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

public class DataViewerLabelProvider extends ObservableMapLabelProvider {

    private DateFormat dateFormat;

    public DataViewerLabelProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public DataViewerLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (columnIndex < attributeMaps.length) {
            Object result = attributeMaps[columnIndex].get(element);
            if (result instanceof Date) {
                return result == null ? "" : dateFormat.format(result);
            }
            return result == null ? "" : result.toString(); //$NON-NLS-1$
        }
        return null;
    }
}
