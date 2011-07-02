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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Interface for WatchList type views.
 *
 * @since 1.0
 */
public interface IWatchList extends IAdaptable {

    public static final String NAME = "name";
    public static final String COLUMNS = "columns";
    public static final String HOLDINGS = "holdings";

    /**
     * Returns the name of the watchlist.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Returns the number of columns contained in the watchlist.
     *
     * @return the number of columns.
     */
    public int getColumnCount();

    /**
     * Returns an array of <code>IWatchListColumn</code>s which are the
     * columns in the watchlist. Columns are returned in the order
     * that they were created.
     *
     * @return the columns in the watchlist.
     */
    public IWatchListColumn[] getColumns();

    /**
     * Returns the number of items contained in the watchlist.
     *
     * @return the number of items.
     */
    public int getItemCount();

    /**
     * Returns a (possibly empty) array of <code>IWatchListElement</code>s which
     * are the items in the watchlist.
     *
     * @return the items in the watchlist.
     */
    public IWatchListElement[] getItems();

    /**
     * Returns the item at the given, zero-relative index in the
     * watchlist. Throws an exception if the index is out of range.
     *
     * @param index the index of the item to return.
     * @return the item at the given index.
     *
     * @exception IllegalArgumentException if the index is not between 0 and the number of elements in the list minus 1 (inclusive)
     * </ul>
     */
    public IWatchListElement getItem(int index);

    /**
     * Returns a (possibly empty) array of <code>IWatchListElement</code>s which
     * are the items in the watchlist associated with the given <code>ISecurity</code>
     * object.
     *
     * @param security the security to search.
     * @return the items in the watchlist associated with the security.
     */
    public IWatchListElement[] getItem(ISecurity security);

    /**
     * Accepts the given visitor.
     * The visitor's <code>visit</code> method is called with this
     * watchlist. If the visitor returns <code>true</code>, this method
     * visits this watchlist's members.
     *
     * @param visitor the visitor.
     */
    public void accept(IWatchListVisitor visitor);
}
