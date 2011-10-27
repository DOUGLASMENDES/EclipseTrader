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

package org.eclipsetrader.core.charts;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

public class NumberValue implements IAdaptable {

    private final Date date;
    private final Number value;

    public NumberValue(Date date, Number value) {
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public Number getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (date != null && adapter.isAssignableFrom(date.getClass())) {
            return date;
        }
        if (value != null && adapter.isAssignableFrom(value.getClass())) {
            return value;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NumberValue [D=" + date + " V=" + value + "]";
    }
}
