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

package org.eclipsetrader.ui.internal.navigator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;

public class RepositoryGroup implements INavigatorContentGroup, IExecutableExtension {

    private String id;
    private String name;

    public RepositoryGroup() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.securities.IContentGroup#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.securities.IContentGroup#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.securities.IContentGroup#getGroupedContent(org.eclipse.core.runtime.IAdaptable[])
     */
    @Override
    public IViewItem[] getGroupedContent(IAdaptable[] elements) {
        Set<IViewItem> result = new HashSet<IViewItem>();

        for (IRepository repository : getRepositoryService().getRepositories()) {
            NavigatorViewItem viewItem = new NavigatorViewItem(null, repository);
            for (IAdaptable e : elements) {
                IStoreObject store = (IStoreObject) e.getAdapter(IStoreObject.class);
                if (store != null && store.getStore().getRepository() == repository) {
                    viewItem.createChild(e.getAdapter(Object.class));
                }
            }
            if (viewItem.getItemCount() != 0) {
                result.add(viewItem);
            }
        }

        return result.toArray(new IViewItem[result.size()]);
    }

    protected IRepositoryService getRepositoryService() {
        return UIActivator.getDefault().getRepositoryService();
    }
}
