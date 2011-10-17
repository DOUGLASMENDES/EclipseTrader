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

package org.eclipsetrader.ui.internal.trading.portfolio;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipsetrader.core.trading.IBroker;

public class BrokerElementAdapterFactory implements IAdapterFactory {

    public BrokerElementAdapterFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof BrokerElement) {
            return ((BrokerElement) adaptableObject).getAdapter(adapterType);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[] {
            IBroker.class
        };
    }
}
