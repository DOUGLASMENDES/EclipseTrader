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

package org.eclipsetrader.directa.internal;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.directa.internal.core.OrderMonitor;
import org.eclipsetrader.directa.internal.core.WebConnector;
import org.eclipsetrader.directa.internal.core.repository.IdentifiersList;
import org.eclipsetrader.directa.internal.ui.StatusLineContributionItem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipsetrader.directa"; //$NON-NLS-1$
    public static final String REPOSITORY_FILE = "identifiers.xml"; //$NON-NLS-1$

    public static final String PROP_CODE = "org.eclipsetrader.borsaitalia.code"; //$NON-NLS-1$
    public static final String PROP_ISIN = "org.eclipsetrader.borsaitalia.isin"; //$NON-NLS-1$

    public static final String PREFS_USERNAME = "USERNAME"; //$NON-NLS-1$
    public static final String PREFS_PASSWORD = "PASSWORD"; //$NON-NLS-1$
    public static final String PREFS_CONNECTION_METHOD = "CONNECTION_METHOD"; //$NON-NLS-1$
    public static final String PREFS_TRADING_HOST = "TRADING_HOST"; //$NON-NLS-1$
    public static final String PREFS_USE_SECURE_PREFERENCE_STORE = "USE_SECURE_PREFERENCE_STORE"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private IdentifiersList identifiersList;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        startupRepository(getStateLocation().append(REPOSITORY_FILE).toFile());

        IAdapterManager adapterManager = Platform.getAdapterManager();
        adapterManager.registerAdapters(new IAdapterFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Object adaptableObject, Class adapterType) {
                if (adaptableObject instanceof OrderMonitor) {
                    if (adapterType.isAssignableFrom(PropertyChangeSupport.class)) {
                        return ((OrderMonitor) adaptableObject).getPropertyChangeSupport();
                    }
                }
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class[] getAdapterList() {
                return new Class[] {
                    PropertyChangeSupport.class,
                };
            }
        }, OrderMonitor.class);

        WebConnector.getInstance();

        ServiceReference serviceReference = context.getServiceReference(IStatusLineManager.class.getName());
        if (serviceReference != null) {
            IStatusLineManager statusLine = (IStatusLineManager) context.getService(serviceReference);

            StatusLineContributionItem item = new StatusLineContributionItem(PLUGIN_ID);
            item.setImage(imageDescriptorFromPlugin(PLUGIN_ID, "icons/d-small.gif").createImage()); //$NON-NLS-1$
            statusLine.add(item);

            context.ungetService(serviceReference);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        shutdownRepository(getStateLocation().append(REPOSITORY_FILE).toFile());

        WebConnector.getInstance().getAccount().save();

        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public static void log(IStatus status) {
        if (plugin != null) {
            plugin.getLog().log(status);
        }
        else {
            System.err.println(status);
        }
    }

    public static void log(String message, Throwable throwable) {
        Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
        if (plugin != null) {
            plugin.getLog().log(status);
        }
        else {
            System.err.println(status);
        }
    }

    public void startupRepository(File file) {
        if (file.exists() == true) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(IdentifiersList.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        getLog().log(status);
                        return true;
                    }
                });
                identifiersList = (IdentifiersList) unmarshaller.unmarshal(file);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error loading repository", e); //$NON-NLS-1$
                log(status);
            }
        }

        // Fail safe, create an empty repository
        if (identifiersList == null) {
            identifiersList = new IdentifiersList();
        }
    }

    public void shutdownRepository(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(IdentifiersList.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    getLog().log(status);
                    return true;
                }
            });
            marshaller.marshal(identifiersList, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error saving repository", e); //$NON-NLS-1$
            log(status);
        }
    }
}
