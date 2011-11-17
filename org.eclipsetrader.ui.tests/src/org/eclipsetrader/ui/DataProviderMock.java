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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;


public class DataProviderMock implements IDataProvider {

    public DataProviderMock() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public void init(IAdaptable adaptable) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public IAdaptable getValue(IAdaptable adaptable) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
     */
    @Override
    public IDataProviderFactory getFactory() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProvider#dispose()
     */
    @Override
    public void dispose() {
    }

}
