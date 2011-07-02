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

package org.eclipsetrader.repository.hibernate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryProvider;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.RepositoryDefinition;

public class HibernateRepositoryProvider implements IRepositoryProvider {

    private List<HibernateRepository> list;

    public HibernateRepositoryProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryProvider#getRepositories(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IRepository[] getRepositories(IProgressMonitor monitor) {
        if (list == null) {
            list = new ArrayList<HibernateRepository>();

            File file = Activator.getDefault().getStateLocation().append(Activator.REPOSITORIES_FILE).toFile();
            try {
                if (file.exists()) {
                    JAXBContext jaxbContext = JAXBContext.newInstance(RepositoryDefinition[].class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    unmarshaller.setEventHandler(new ValidationEventHandler() {

                        @Override
                        public boolean handleEvent(ValidationEvent event) {
                            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                            Activator.getDefault().getLog().log(status);
                            return true;
                        }
                    });
                    JAXBElement<RepositoryDefinition[]> element = unmarshaller.unmarshal(new StreamSource(file), RepositoryDefinition[].class);
                    for (RepositoryDefinition repository : element.getValue()) {
                        HibernateRepository hibernateRepository = new HibernateRepository(repository);
                        try {
                            hibernateRepository.startUp(new NullProgressMonitor());
                            list.add(hibernateRepository);
                            Activator.getDefault().getRepositories().add(hibernateRepository);
                        } catch (Exception e) {
                            String message = NLS.bind("Error loading repository '{1}' ({0})", new Object[] {
                                    repository.getSchema(),
                                    repository.getLabel()
                            });
                            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, message, e);
                            Activator.log(status);
                        }
                    }
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository configuration", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            }
        }
        return list.toArray(new IRepository[list.size()]);
    }
}
