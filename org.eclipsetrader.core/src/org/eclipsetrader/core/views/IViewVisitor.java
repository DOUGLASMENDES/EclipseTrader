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
 * This interface is implemented by objects that visits view trees.
 *
 * @since 1.0
 */
public interface IViewVisitor extends IViewItemVisitor {

    /**
     * Visits the given view.
     *
     * @param view the view to visit.
     * @return <code>true</code> if the view's items should be visited, <code>false</code> otherwise.
     */
    public boolean visit(IView view);
}
