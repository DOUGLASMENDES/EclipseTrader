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
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;

public class MarketGroup implements INavigatorContentGroup, IExecutableExtension {

    private String id;
    private String name;

    public MarketGroup() {
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

        Set<ISecurity> set = new HashSet<ISecurity>();
        for (IAdaptable e : elements) {
            ISecurity security = (ISecurity) e.getAdapter(ISecurity.class);
            if (security != null) {
                set.add(security);
            }
        }

        for (IMarket market : getMarketService().getMarkets()) {
            NavigatorViewItem viewItem = new NavigatorViewItem(null, market);
            for (ISecurity s : market.getMembers()) {
                if (set.contains(s)) {
                    viewItem.createChild(s);
                }
            }
            if (viewItem.getItemCount() != 0) {
                result.add(viewItem);
            }
        }

        return result.toArray(new IViewItem[result.size()]);
    }

    protected IMarketService getMarketService() {
        return UIActivator.getDefault().getMarketService();
    }
}
