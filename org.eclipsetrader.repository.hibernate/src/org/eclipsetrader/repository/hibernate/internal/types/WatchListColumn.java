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

package org.eclipsetrader.repository.hibernate.internal.types;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.repository.hibernate.internal.stores.WatchListStore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "watchlists_columns")
public class WatchListColumn implements IColumn {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "provider")
    @Target(DataProviderFactoryAdapter.class)
    private IDataProviderFactory dataProviderFactory;

    @Column(name = "index")
    @SuppressWarnings("unused")
    private int index;

    @ManyToOne
    @SuppressWarnings("unused")
    private WatchListStore watchlist;

    public WatchListColumn() {
    }

    public WatchListColumn(IColumn column, WatchListStore watchlist, int index) {
        this.name = column.getName();
        this.dataProviderFactory = column.getDataProviderFactory();
        this.watchlist = watchlist;
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IColumn#getName()
     */
    @Override
    public String getName() {
        return name;
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
