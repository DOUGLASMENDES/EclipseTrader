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

package org.eclipsetrader.ui.internal;

import java.lang.reflect.Field;

import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.internal.repositories.RepositoryService;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.BundleContext;

public class TestUIActivator extends UIActivator {

    public TestUIActivator() throws Exception {
        Field[] fields = UIActivator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals("plugin")) {
                fields[i].setAccessible(true);
                fields[i].set(this, this);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.UIActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.UIActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.UIActivator#getMarketService()
     */
    @Override
    public IMarketService getMarketService() {
        return new MarketService();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.UIActivator#getRepositoryService()
     */
    @Override
    public IRepositoryService getRepositoryService() {
        return new RepositoryService();
    }
}
