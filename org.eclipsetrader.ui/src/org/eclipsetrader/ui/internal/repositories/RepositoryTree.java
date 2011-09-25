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

package org.eclipsetrader.ui.internal.repositories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.IWatchList;

public class RepositoryTree implements IView {

    private IRepositoryService service;
    private RepositoryViewItem root;

    private SecurityContainerObject instrumentsContainer = new SecurityContainerObject(Messages.RepositoryTree_Instruments);
    private WatchListContainerObject watchlistsContainer = new WatchListContainerObject(Messages.RepositoryTree_Watchlists);
    private OthersContainerObject othersContainer = new OthersContainerObject("Others");

    public RepositoryTree(IRepositoryService service) {
        this.service = service;
        this.root = new RepositoryViewItem();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#addListener(org.eclipsetrader.core.views.IViewChangeListener)
     */
    @Override
    public void addViewChangeListener(IViewChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#removeListener(org.eclipsetrader.core.views.IViewChangeListener)
     */
    @Override
    public void removeViewChangeListener(IViewChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#dispose()
     */
    @Override
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#getItems()
     */
    @Override
    public IViewItem[] getItems() {
        return root.getItems();
    }

    public void refresh() {
        for (IRepository repository : service.getRepositories()) {
            RepositoryViewItem element = root.getChild(repository);
            if (element == null) {
                element = root.createChild(repository);
            }
            visit(service, repository, element);
        }
    }

    protected void visit(IRepositoryService service, IRepository repository, RepositoryViewItem parent) {
        List<IStoreObject> storeObjects = new ArrayList<IStoreObject>();
        storeObjects.addAll(Arrays.asList(service.getAllObjects()));

        List<Object> childObjects = new ArrayList<Object>();
        for (Iterator<IStoreObject> iter = storeObjects.iterator(); iter.hasNext();) {
            IStoreObject storeObject = iter.next();
            if (storeObject instanceof ISecurity) {
                childObjects.add(storeObject);
                iter.remove();
            }
        }
        RepositoryViewItem element = parent.getChild(instrumentsContainer);
        if (element == null) {
            element = parent.createChild(instrumentsContainer);
        }
        visit(childObjects, element);

        childObjects = new ArrayList<Object>();
        for (Iterator<IStoreObject> iter = storeObjects.iterator(); iter.hasNext();) {
            IStoreObject storeObject = iter.next();
            if (storeObject instanceof IWatchList) {
                childObjects.add(storeObject);
                iter.remove();
            }
        }
        element = parent.getChild(watchlistsContainer);
        if (element == null) {
            element = parent.createChild(watchlistsContainer);
        }
        visit(childObjects, element);

        element = parent.getChild(othersContainer);
        if (element == null) {
            element = parent.createChild(othersContainer);
        }
        visit(storeObjects, element);
    }

    protected void visit(List<?> objects, RepositoryViewItem parent) {
        for (Object obj : objects) {
            if (!parent.hasChild(obj)) {
                parent.createChild(obj);
            }
        }
        for (Iterator<RepositoryViewItem> iter = parent.iterator(); iter.hasNext();) {
            if (!objects.contains(iter.next().getObject())) {
                iter.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#accept(org.eclipsetrader.core.views.IViewVisitor)
     */
    @Override
    public void accept(IViewVisitor visitor) {
        if (visitor.visit(this)) {
            for (IViewItem viewItem : getItems()) {
                viewItem.accept(visitor);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
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
}
