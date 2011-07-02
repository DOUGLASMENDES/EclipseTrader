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

package org.eclipsetrader.core.internal.feed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.CoreActivator;

public class FeedService implements IFeedService {

    private static final String EXTENSION_ID = "connectors"; //$NON-NLS-1$
    private static final String CONNECTOR_ELEMENT = "connector"; //$NON-NLS-1$
    private static final String BACKFILL_ELEMENT = "backfill"; //$NON-NLS-1$
    private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private Map<String, IFeedConnector> connectors = new HashMap<String, IFeedConnector>();
    private Map<String, IBackfillConnector> backfillConnectors = new HashMap<String, IBackfillConnector>();

    public FeedService() {
    }

    public void startUp() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] elements = registry.getConfigurationElementsFor(CoreActivator.PLUGIN_ID, EXTENSION_ID);
        for (IConfigurationElement element : elements) {
            try {
                String id = element.getAttribute(ID_ATTRIBUTE);
                if (element.getName().equals(CONNECTOR_ELEMENT)) {
                    IFeedConnector connector = (IFeedConnector) element.createExecutableExtension(CLASS_ATTRIBUTE);
                    connectors.put(id, connector);
                    if (connector instanceof IBackfillConnector) {
                        backfillConnectors.put(id, (IBackfillConnector) connector);
                    }
                }
                if (element.getName().equals(BACKFILL_ELEMENT)) {
                    IBackfillConnector connector = (IBackfillConnector) element.createExecutableExtension(CLASS_ATTRIBUTE);
                    backfillConnectors.put(id, connector);
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error creating connector " + element.getAttribute(ID_ATTRIBUTE), e); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
            }
        }
    }

    public void shutDown() {
        for (IFeedConnector connector : connectors.values()) {
            connector.disconnect();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedService#getConnector(java.lang.String)
     */
    @Override
    public IFeedConnector getConnector(String id) {
        return connectors.get(id);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedService#getConnectors()
     */
    @Override
    public IFeedConnector[] getConnectors() {
        Collection<IFeedConnector> values = connectors.values();
        return values.toArray(new IFeedConnector[values.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedService#getBackfillConnector(java.lang.String)
     */
    @Override
    public IBackfillConnector getBackfillConnector(String id) {
        return backfillConnectors.get(id);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedService#getBackfillConnectors()
     */
    @Override
    public IBackfillConnector[] getBackfillConnectors() {
        Collection<IBackfillConnector> values = backfillConnectors.values();
        return values.toArray(new IBackfillConnector[values.size()]);
    }
}
