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

package org.eclipsetrader.core.views;

/**
 * Default implementation of the <code>IColumn</code> interface.
 * Clients may subclass.
 *
 * @since 1.0
 */
public class Column implements IColumn {

    private String name;
    private IDataProviderFactory dataProviderFactory;

    protected Column() {
    }

    public Column(String name, IDataProviderFactory dataProviderFactory) {
        this.name = name;
        this.dataProviderFactory = dataProviderFactory;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IColumn#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IColumn#getDataProviderFactory()
     */
    @Override
    public IDataProviderFactory getDataProviderFactory() {
        return dataProviderFactory;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
