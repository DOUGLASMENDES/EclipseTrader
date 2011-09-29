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

package org.eclipsetrader.repository.hibernate.internal;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipsetrader.repository.hibernate";
    public static final String CONNECTIVITY_EXTENSION_ID = "org.eclipsetrader.repository.hibernate.connectivity";

    public static final String REPOSITORIES_FILE = "repositories.xml"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private List<HibernateRepository> repositories = new ArrayList<HibernateRepository>();

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
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        for (HibernateRepository repository : repositories) {
            repository.shutDown(null);
        }

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
        if (plugin == null) {
            if (status.getException() != null) {
                status.getException().printStackTrace();
            }
        }
        else {
            plugin.getLog().log(status);
        }
    }

    public List<HibernateRepository> getRepositories() {
        return repositories;
    }

    public static void saveRepositoryDefinitions() {
        List<RepositoryDefinition> list = new ArrayList<RepositoryDefinition>();
        for (HibernateRepository repository : getDefault().getRepositories()) {
            RepositoryDefinition definition = (RepositoryDefinition) repository.getAdapter(RepositoryDefinition.class);
            if (definition != null) {
                list.add(definition);
            }
        }

        File file = Activator.getDefault().getStateLocation().append(Activator.REPOSITORIES_FILE).toFile();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RepositoryDefinition[].class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.getDefault().getLog().log(status);
                    return true;
                }
            });
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            JAXBElement<RepositoryDefinition[]> element = new JAXBElement<RepositoryDefinition[]>(new QName("list"), RepositoryDefinition[].class, list.toArray(new RepositoryDefinition[list.size()]));
            marshaller.marshal(element, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error writing repository configuration", e); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
    }
}
