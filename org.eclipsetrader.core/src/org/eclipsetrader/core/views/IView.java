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
 * Instances of this interface represents a view on an object.
 *
 * @since 1.0
 */
public interface IView extends IAdaptable {

    /**
     * Adds the listener to the collection of listeners that receive
     * notifications about changes in the view items and structure.
     *
     * @param listener the listener to add.
     */
    public void addViewChangeListener(IViewChangeListener listener);

    /**
     * Removes the listener from the collection of listeners that receive
     * notifications about changes in the view items and structure.
     *
     * @param listener the listener to remove.
     */
    public void removeViewChangeListener(IViewChangeListener listener);

    /**
     * Returns the root view items.
     *
     * @return the view items.
     */
    public IViewItem[] getItems();

    /**
     * Disposes the resources associated with the receiver.
     */
    public void dispose();

    /**
     * Accepts the given visitor.
     * The visitor's <code>visit</code> method is called with this
     * view. If the visitor returns <code>true</code>, this method
     * visits this view's items.
     *
     * @param visitor the visitor.
     */
    public void accept(IViewVisitor visitor);
}
