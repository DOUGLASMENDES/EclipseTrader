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

package org.eclipsetrader.ui.internal.navigator;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.views.WatchList;
import org.eclipsetrader.core.views.IWatchList;

public class NavigatorViewItemAdapterFactory implements IAdapterFactory {

	public NavigatorViewItemAdapterFactory() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
    	if (adaptableObject instanceof NavigatorViewItem)
    		return ((NavigatorViewItem) adaptableObject).getAdapter(adapterType);
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
	    return new Class[] {
	    		IWatchList.class,
	    		WatchList.class,
	    		Security.class,
	    		Market.class,
	    	};
    }
}
