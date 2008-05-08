/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class InstrumentTypeGroup implements INavigatorContentGroup, IExecutableExtension {
	private String id;
	private String name;

	public InstrumentTypeGroup() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.securities.IContentGroup#getId()
     */
    public String getId() {
	    return id;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.securities.IContentGroup#getName()
     */
    public String getName() {
	    return name;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.securities.IContentGroup#getGroupedContent(org.eclipse.core.runtime.IAdaptable[])
     */
    public IViewItem[] getGroupedContent(IAdaptable[] elements) {
    	Set<IViewItem> result = new HashSet<IViewItem>();

		NavigatorViewItem viewItem = new NavigatorViewItem(null, "Securities");
    	for (IAdaptable e : elements) {
    		ISecurity reference = (ISecurity) e.getAdapter(ISecurity.class);
    		if (reference != null)
				viewItem.createChild(reference);
    	}
		if (viewItem.getItemCount() != 0)
			result.add(viewItem);

		viewItem = new NavigatorViewItem(null, "WatchLists");
    	for (IAdaptable e : elements) {
    		IWatchList reference = (IWatchList) e.getAdapter(IWatchList.class);
    		if (reference != null)
				viewItem.createChild(reference);
    	}
		if (viewItem.getItemCount() != 0)
			result.add(viewItem);

    	return result.toArray(new IViewItem[result.size()]);
    }

    protected MarketService getMarketService() {
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(MarketService.class.getName());
    		MarketService service = (MarketService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading market service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
    }
}
