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

package org.eclipsetrader.ui.internal.ats.explorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;

public class ExplorerViewModel extends TreeStructureAdvisor implements IObservableFactory {

    private final IRepositoryService repositoryService;
    private final List<ExplorerViewItem> list = new ArrayList<ExplorerViewItem>();
    private final WritableList root = new WritableList(list, ExplorerViewItem.class);

    public static class InstrumentRootItem implements ExplorerViewItem {

        private final StrategyItem parent;
        private final IStrategy strategy;
        private final List<InstrumentItem> list = new ArrayList<InstrumentItem>();
        private final WritableList childs = new WritableList(list, InstrumentItem.class);

        public InstrumentRootItem(StrategyItem parent) {
            this.parent = parent;
            this.strategy = parent.getStrategy();
            for (ISecurity instrument : strategy.getInstruments()) {
                childs.add(new InstrumentItem(this, instrument));
            }
        }

        public IStrategy getStrategy() {
            return strategy;
        }

        public void update() {
            List<ISecurity> currentInstruments = Arrays.asList(strategy.getInstruments());

            List<InstrumentItem> toAdd = new ArrayList<InstrumentItem>();
            for (ISecurity instrument : currentInstruments) {
                if (!contains(instrument)) {
                    toAdd.add(new InstrumentItem(this, instrument));
                }
            }

            List<InstrumentItem> toRemove = new ArrayList<InstrumentItem>();
            for (InstrumentItem item : list) {
                if (!currentInstruments.contains(item.getInstrument())) {
                    toRemove.add(item);
                }
            }

            childs.addAll(toAdd);
            childs.removeAll(toRemove);
        }

        private boolean contains(ISecurity instrument) {
            for (Object o : childs) {
                if (((InstrumentItem) o).getInstrument() == instrument) {
                    return true;
                }
            }
            return false;
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
            return childs.size() != 0;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#getItems()
         */
        @Override
        public ObservableList getItems() {
            return childs;
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
            return "Instruments";
        }
    }

    public static class ScriptRootItem implements ExplorerViewItem {

        private final StrategyItem parent;
        private final IScriptStrategy strategy;
        private final List<ScriptItem> list = new ArrayList<ScriptItem>();
        private final WritableList childs = new WritableList(list, ScriptItem.class);

        public ScriptRootItem(StrategyItem parent) {
            this.parent = parent;
            this.strategy = (IScriptStrategy) parent.getStrategy();
            childs.add(new MainScriptItem(this));
            for (IScript script : strategy.getIncludes()) {
                childs.add(new ScriptItem(this, script));
            }
        }

        public IStrategy getStrategy() {
            return strategy;
        }

        public void update() {
            List<IScript> currentScripts = Arrays.asList(strategy.getIncludes());

            List<ScriptItem> toAdd = new ArrayList<ScriptItem>();
            for (IScript script : currentScripts) {
                if (!contains(script)) {
                    toAdd.add(new ScriptItem(this, script));
                }
            }

            List<ScriptItem> toRemove = new ArrayList<ScriptItem>();
            for (ScriptItem item : list.subList(1, list.size())) {
                if (!currentScripts.contains(item.getScript())) {
                    toRemove.add(item);
                }
            }

            childs.addAll(toAdd);
            childs.removeAll(toRemove);
        }

        private boolean contains(IScript script) {
            for (ScriptItem item : list.subList(1, list.size())) {
                if (item.getScript() == script) {
                    return true;
                }
            }
            return false;
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
            return childs.size() != 0;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.internal.ats.ExplorerViewItem#getItems()
         */
        @Override
        public ObservableList getItems() {
            return childs;
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
            return "Scripts";
        }
    }

    private final IRepositoryChangeListener repositoryChangeListener = new IRepositoryChangeListener() {

        @Override
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
            for (RepositoryResourceDelta delta : event.getDeltas()) {
                if (delta.getResource() instanceof IStrategy) {
                    IStrategy resource = (IStrategy) delta.getResource();
                    if ((delta.getKind() & RepositoryResourceDelta.CHANGED) != 0) {
                        StrategyItem viewItem = (StrategyItem) getViewItemFor(resource);
                        if (viewItem != null) {
                            viewItem.update();
                        }
                    }
                    else if ((delta.getKind() & RepositoryResourceDelta.ADDED) != 0) {
                        root.add(new StrategyItem(resource));
                    }
                    else if ((delta.getKind() & RepositoryResourceDelta.REMOVED) != 0) {
                        ExplorerViewItem viewItem = getViewItemFor(resource);
                        if (viewItem != null) {
                            if (viewItem.getParent() == null) {
                                root.remove(viewItem);
                            }
                            else {
                                viewItem.getParent().getItems().remove(viewItem);
                            }
                        }
                    }
                }
            }
        }
    };

    public ExplorerViewModel(IRepositoryService repositoryService) {
        this.repositoryService = repositoryService;

        for (IStoreObject object : repositoryService.getAllObjects()) {
            if (object instanceof IStrategy) {
                root.add(new StrategyItem((IStrategy) object));
            }
        }

        repositoryService.addRepositoryResourceListener(repositoryChangeListener);
    }

    public WritableList getRoot() {
        return root;
    }

    public void dispose() {
        repositoryService.removeRepositoryResourceListener(repositoryChangeListener);
        root.clear();
        root.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.masterdetail.IObservableFactory#createObservable(java.lang.Object)
     */
    @Override
    public IObservable createObservable(Object target) {
        if (target == this) {
            return Observables.unmodifiableObservableList(root);
        }
        if (target instanceof ExplorerViewItem) {
            ObservableList list = ((ExplorerViewItem) target).getItems();
            if (list != null) {
                return Observables.unmodifiableObservableList(list);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.databinding.viewers.TreeStructureAdvisor#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof ExplorerViewItem) {
            return ((ExplorerViewItem) element).getParent();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.databinding.viewers.TreeStructureAdvisor#hasChildren(java.lang.Object)
     */
    @Override
    public Boolean hasChildren(Object element) {
        if (element == this) {
            return root.size() != 0;
        }
        if (element instanceof ExplorerViewItem) {
            return ((ExplorerViewItem) element).hasChildren();
        }
        return false;
    }

    public ExplorerViewItem getViewItemFor(final Object target) {
        final AtomicReference<ExplorerViewItem> result = new AtomicReference<ExplorerViewItem>();

        final ExplorerViewItemVisitor visitor = new ExplorerViewItemVisitor() {

            @Override
            public void visit(ExplorerViewItem viewItem) {
                if (viewItem.getAdapter(target.getClass()) == target) {
                    result.set(viewItem);
                }
                else if (viewItem.getItems() != null) {
                    for (Object childItem : viewItem.getItems()) {
                        ((ExplorerViewItem) childItem).accept(this);
                        if (result.get() != null) {
                            break;
                        }
                    }
                }
            }
        };

        for (ExplorerViewItem viewItem : list) {
            viewItem.accept(visitor);
            if (result.get() != null) {
                break;
            }
        }

        return result.get();
    }
}
