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

package org.eclipsetrader.yahoo.internal.core.connector;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.yahoo.internal.YahooActivator;

public class FeedConnector implements IFeedConnector, IExecutableExtension {

    private String id;
    private String name;

    private SnapshotConnector connector;

    private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (YahooActivator.PREFS_DRIVER.equals(event.getProperty())) {
                onChangeDriver((String) event.getNewValue());
            }
        }
    };

    public FeedConnector() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");

        String className = YahooActivator.getDefault().getPreferenceStore().getString(YahooActivator.PREFS_DRIVER);
        try {
            connector = (SnapshotConnector) Class.forName(className).newInstance();
        } catch (Exception e) {
            YahooActivator.log(new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error loding driver " + className, e));
            connector = new StreamingConnector();
        }
        YahooActivator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public void connect() {
        connector.connect();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    @Override
    public void disconnect() {
        connector.disconnect();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    @Override
    public IFeedSubscription subscribe(IFeedIdentifier identifier) {
        return connector.subscribe(identifier);
    }

    protected void onChangeDriver(String className) {
        System.err.println("Using driver " + className);

        Map<String, FeedSubscription> subscriptions = null;
        if (connector != null) {
            subscriptions = connector.getSymbolSubscriptions();
            connector.disconnect();
        }

        try {
            connector = (SnapshotConnector) Class.forName(className).newInstance();
        } catch (Exception e) {
            YahooActivator.log(new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error loding driver " + className, e));
            connector = new StreamingConnector();
        }

        if (connector != null) {
            if (subscriptions != null) {
                connector.getSymbolSubscriptions().putAll(subscriptions);
            }
            connector.connect();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void addConnectorListener(IConnectorListener listener) {
        connector.addConnectorListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        connector.removeConnectorListener(listener);
    }
}
