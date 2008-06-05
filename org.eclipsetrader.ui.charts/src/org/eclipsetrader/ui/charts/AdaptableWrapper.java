/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.core.runtime.IAdaptable;

public class AdaptableWrapper implements IAdaptable {
	private Object obj;

	public AdaptableWrapper(Object obj) {
	    this.obj = obj;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (obj != null && adapter.isAssignableFrom(obj.getClass()))
			return obj;
		return null;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	    return obj.hashCode();
    }

	/* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof IAdaptable) {
    		Object other = ((IAdaptable) obj).getAdapter(this.obj.getClass());
    		return this.obj.equals(other);
    	}
	    return this.obj.equals(obj);
    }
}
