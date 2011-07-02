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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;

public class InstrumentTypeGroup implements INavigatorContentGroup, IExecutableExtension {

    private String id;
    private String name;

    public InstrumentTypeGroup() {
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
        Set<IAdaptable> elementsSet = new HashSet<IAdaptable>(Arrays.asList(elements));
        List<IViewItem> result = new ArrayList<IViewItem>();

        NavigatorViewItem viewItem = new NavigatorViewItem(null, "Stocks");
        for (Iterator<IAdaptable> iter = elementsSet.iterator(); iter.hasNext();) {
            IAdaptable e = iter.next();
            Object reference = e.getAdapter(IStock.class);
            if (reference != null) {
                viewItem.createChild(reference);
                iter.remove();
            }
        }
        if (viewItem.getItemCount() != 0) {
            result.add(viewItem);
        }

        viewItem = new NavigatorViewItem(null, "Currencies");
        for (Iterator<IAdaptable> iter = elementsSet.iterator(); iter.hasNext();) {
            IAdaptable e = iter.next();
            Object reference = e.getAdapter(ICurrencyExchange.class);
            if (reference != null) {
                viewItem.createChild(reference);
                iter.remove();
            }
        }
        if (viewItem.getItemCount() != 0) {
            result.add(viewItem);
        }

        viewItem = new NavigatorViewItem(null, "WatchLists");
        for (Iterator<IAdaptable> iter = elementsSet.iterator(); iter.hasNext();) {
            IAdaptable e = iter.next();
            Object reference = e.getAdapter(IWatchList.class);
            if (reference != null) {
                viewItem.createChild(reference);
                iter.remove();
            }
        }
        if (viewItem.getItemCount() != 0) {
            result.add(viewItem);
        }

        return result.toArray(new IViewItem[result.size()]);
    }
}
