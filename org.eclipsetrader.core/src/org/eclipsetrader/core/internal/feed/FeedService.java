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

package org.eclipsetrader.core.internal.feed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.CoreActivator;

public class FeedService implements IFeedService {
	private static final String EXTENSION_ID = "connectors"; //$NON-NLS-1$
	private static final String CONNECTOR_ELEMENT = "connector"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private Map<String,IFeedConnector> connectors = new HashMap<String,IFeedConnector>();

	public FeedService() {
	}

	public void startUp() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(CoreActivator.PLUGIN_ID, EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			if (!element.getName().equals(CONNECTOR_ELEMENT))
				continue;
			try {
				String id = element.getAttribute(ID_ATTRIBUTE);
				IFeedConnector connector = (IFeedConnector) element.createExecutableExtension(CLASS_ATTRIBUTE);
				connectors.put(id, connector);
			} catch (Exception e) {
				Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error creating connector " + element.getAttribute(ID_ATTRIBUTE), e); //$NON-NLS-1$
				CoreActivator.getDefault().getLog().log(status);
			}
		}
	}

	public void shutDown() {
		for (IFeedConnector connector : connectors.values())
			connector.disconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedService#getConnector(java.lang.String)
	 */
	public IFeedConnector getConnector(String id) {
		return connectors.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedService#getConnectors()
	 */
	public IFeedConnector[] getConnectors() {
		Collection<IFeedConnector> values = connectors.values();
		return values.toArray(new IFeedConnector[values.size()]);
	}
}
