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

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.ui.internal.ats.ExplorerViewModel.ScriptRootItem;

public class MainScriptItem implements ExplorerViewItem {

    private final ScriptRootItem parent;

    public MainScriptItem(ScriptRootItem parent) {
        this.parent = parent;
    }

    public IStrategy getStrategy() {
        return parent.getStrategy();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#getParent()
     */
    @Override
    public ExplorerViewItem getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#getItems()
     */
    @Override
    public ObservableList getItems() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#accept(org.eclipsetrader.ui.internal.ats.ViewItemVisitor)
     */
    @Override
    public void accept(ExplorerViewItemVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Main";
    }
}