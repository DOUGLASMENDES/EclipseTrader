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

package org.eclipsetrader.core.internal.ats.repository;

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StrategyAdapter extends XmlAdapter<String, IStrategy> {

    public StrategyAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IStrategy v) throws Exception {
        if (v == null) {
            return null;
        }
        URI uri = (URI) v.getAdapter(URI.class);
        if (uri != null) {
            return uri.toString();
        }
        IStoreObject storeObject = (IStoreObject) v.getAdapter(IStoreObject.class);
        return storeObject != null ? storeObject.getStore().toURI().toString() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public IStrategy unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceRef = context.getServiceReference(IRepositoryService.class);
        try {
            IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceRef);
            IStoreObject storeObject = repositoryService.getObjectFromURI(new URI(v));
            if (storeObject instanceof IStrategy) {
                return (IStrategy) storeObject;
            }
        } finally {
            context.ungetService(serviceRef);
        }

        return null;
    }
}
