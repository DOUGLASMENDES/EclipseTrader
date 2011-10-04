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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.ui.internal.ats.ExplorerViewModel.InstrumentRootItem;
import org.eclipsetrader.ui.internal.ats.ExplorerViewModel.ScriptRootItem;

public class StrategyItem implements ExplorerViewItem {

    private final IStrategy strategy;

    private InstrumentRootItem instrumentsRoot;
    private ScriptRootItem scriptsRoot;

    private final List<ExplorerViewItem> list = new ArrayList<ExplorerViewItem>();
    private final WritableList observableChilds = new WritableList(list, ScriptRootItem.class);

    public StrategyItem(IStrategy strategy) {
        this.strategy = strategy;

        observableChilds.add(instrumentsRoot = new InstrumentRootItem(this));
        if (strategy instanceof IScriptStrategy) {
            observableChilds.add(scriptsRoot = new ScriptRootItem(this));
        }
    }

    public IStrategy getStrategy() {
        return strategy;
    }

    public void update() {
        instrumentsRoot.update();
        if (scriptsRoot != null) {
            scriptsRoot.update();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getParent()
     */
    @Override
    public ExplorerViewItem getParent() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#hasChilds()
     */
    @Override
    public boolean hasChildren() {
        return observableChilds.size() != 0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#getItems()
     */
    @Override
    public ObservableList getItems() {
        return observableChilds;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#accept(org.eclipsetrader.ui.internal.ats.ViewItemVisitor)
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
        if (adapter.isAssignableFrom(strategy.getClass())) {
            return strategy;
        }
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
        return strategy.getName();
    }
}