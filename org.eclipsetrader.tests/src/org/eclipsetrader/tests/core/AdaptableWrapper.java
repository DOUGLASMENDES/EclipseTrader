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

package org.eclipsetrader.tests.core;

import org.eclipse.core.runtime.IAdaptable;

public class AdaptableWrapper implements IAdaptable {

    private Object element;

    public AdaptableWrapper(Object element) {
        this.element = element;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (element != null && adapter.isAssignableFrom(element.getClass())) {
            return element;
        }
        return null;
    }
}
