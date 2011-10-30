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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Color;

public class NumberValue implements IAdaptable {

    private final Number value;
    private final String text;
    private Color color;

    public NumberValue(Number value, String text) {
        this.value = value;
        this.text = text;
    }

    public NumberValue(Number value, String text, Color color) {
        this.value = value;
        this.text = text;
        this.color = color;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NumberValue)) {
            return false;
        }
        Number s = (Number) ((NumberValue) obj).getAdapter(Number.class);
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
        if (adapter.isAssignableFrom(Color.class)) {
            return color;
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
        return text;
    }
}
