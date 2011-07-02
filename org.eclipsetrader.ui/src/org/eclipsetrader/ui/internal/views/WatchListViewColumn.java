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

package org.eclipsetrader.ui.internal.views;

import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IWatchListColumn;

public class WatchListViewColumn {

    private IWatchListColumn reference;

    private String name;
    private IDataProviderFactory dataProviderFactory;

    public WatchListViewColumn(IWatchListColumn reference) {
        this.reference = reference;
        this.name = reference.getName();
        this.dataProviderFactory = reference.getDataProviderFactory();
    }

    public WatchListViewColumn(IColumn column) {
        this.name = column.getName();
        this.dataProviderFactory = column.getDataProviderFactory();
    }

    public WatchListViewColumn(String name, IDataProviderFactory dataProviderFactory) {
        this.name = name;
        this.dataProviderFactory = dataProviderFactory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IDataProviderFactory getDataProviderFactory() {
        return dataProviderFactory;
    }

    public IWatchListColumn getReference() {
        return reference;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WatchListViewColumn)) {
            return false;
        }
        if (!dataProviderFactory.getId().equals(((WatchListViewColumn) obj).dataProviderFactory.getId())) {
            return false;
        }
        if (name != null && !name.equals(((WatchListViewColumn) obj).name)) {
            return false;
        }
        if (name != ((WatchListViewColumn) obj).name) {
            return false;
        }
        return true;
    }
}
