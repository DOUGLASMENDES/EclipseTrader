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

/**
 *
 * @since 1.0
 */
public interface IViewItem extends IAdaptable {

    /**
     * Returns the parent view item, or <code>null</code> if the receiver is
     * a root item.
     *
     * @return the parent view item, or <code>null</code>.
     */
    public IViewItem getParent();

    /**
     * Gets the number of child items.
     *
     * @return the number of child items.
     */
    public int getItemCount();

    /**
     * Gets a possibly empty array of child items.
     *
     * @return the child items.
     */
    public IViewItem[] getItems();

    /**
     * Gets a possibly empty array of values associated with the receiver.
     * <p>The array <b>can</b> contain <code>null</code> elements.</p>
     *
     * @return the array of values.
     */
    public IAdaptable[] getValues();

    /**
     * Accepts the given visitor.
     * The visitor's <code>visit</code> method is called with this
     * view item. If the visitor returns <code>true</code>, this method
     * visits this items's child items.
     *
     * @param visitor the visitor.
     */
    public void accept(IViewItemVisitor visitor);
}
