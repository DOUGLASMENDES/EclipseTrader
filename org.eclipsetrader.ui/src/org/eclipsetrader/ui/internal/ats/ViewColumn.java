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

package org.eclipsetrader.ui.internal.ats;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class ViewColumn {

    public static final String PROP_NAME = "name"; //$NON-NLS-1$

    private final IDataProviderFactory factory;
    private final IDataProvider dataProvider;

    private String name;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public ViewColumn(IDataProviderFactory factory) {
        this.factory = factory;
        this.dataProvider = factory.createProvider();
    }

    public ViewColumn(String name, IDataProviderFactory factory) {
        this.factory = factory;
        this.dataProvider = factory.createProvider();
        this.name = name != null ? name : factory.getName();
    }

    public String getId() {
        return factory.getId();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        changeSupport.firePropertyChange(PROP_NAME, this.name, this.name = name);
    }

    public IDataProviderFactory getDataProviderFactory() {
        return factory;
    }

    public IDataProvider getDataProvider() {
        return dataProvider;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ViewColumn)) {
            return false;
        }
        ViewColumn other = (ViewColumn) obj;
        if (!factory.getId().equals(other.factory.getId())) {
            return false;
        }
        return true;
    }
}
