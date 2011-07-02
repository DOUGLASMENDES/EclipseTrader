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
 * This interface is implemented by objects that visits view item trees.
 *
 * @since 1.0
 */
public interface IViewItemVisitor {

    /**
     * Visits the given view item.
     *
     * @param viewitem the view item to visit.
     * @return <code>true</code> if the child items should be visited, <code>false</code> otherwise.
     */
    public boolean visit(IViewItem viewItem);
}
