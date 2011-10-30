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

package org.eclipsetrader.ui.internal.providers;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

public class DateValue implements IAdaptable {

    private final Date value;
    private final String text;

    public DateValue(Date value, String text) {
        this.value = value;
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DateValue)) {
            return false;
        }
        Date s = (Date) ((DateValue) obj).getAdapter(Date.class);
        return s == value || value != null && value.equals(s);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(String.class)) {
            return text;
        }
        if (adapter.isAssignableFrom(value.getClass())) {
            return value;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
