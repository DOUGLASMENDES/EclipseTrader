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

package org.eclipsetrader.repository.hibernate.internal.ui;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.RepositoryDefinition;

public class RepositoryWizard extends Wizard implements INewWizard {

    private Shell shell;

    private DatabaseSelectionPage databaseSelectionPage;
    private RepositoryDetailsPage repositoryDetailsPage;

    public RepositoryWizard() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        shell = workbench.getActiveWorkbenchWindow().getShell();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        setWindowTitle("Hibernate Repository");
        addPage(databaseSelectionPage = new DatabaseSelectionPage());
        addPage(repositoryDetailsPage = new RepositoryDetailsPage() {

            @Override
            protected void doSettingsValidation() {
                RepositoryDefinition repository = getDefinition();

                HibernateRepository hibernateRepository = new HibernateRepository(repository);
                try {
                    hibernateRepository.startUp(new NullProgressMonitor());
                    MessageDialog.openInformation(getShell(), "EclipseTrader", "Database created or updated successfully!");
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error validating repository", e);
                    Activator.log(status);
                    ErrorDialog.openError(getShell(), "EclipseTrader", "Database not created or update failed.", status);
                }
                hibernateRepository.shutDown(new NullProgressMonitor());
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == databaseSelectionPage) {
            DatabaseElement element = databaseSelectionPage.getSelection();
            repositoryDetailsPage.setDefaultName(element.getLabel());
            repositoryDetailsPage.setDefaultSchema(element.getSchema());
            repositoryDetailsPage.setDefaultUrl(element.getUrl());
        }
        return super.getNextPage(page);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        List<RepositoryDefinition> list = new ArrayList<RepositoryDefinition>();

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
                list.addAll(Arrays.asList(element.getValue()));
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository configuration", e); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }

        RepositoryDefinition repository = getDefinition();
        list.add(repository);

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

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (MessageDialog.openQuestion(shell, "EclipseTrader", "The workbench must be restarted for the changes to take effect.\r\nRestart the workbench now ?")) {
                    PlatformUI.getWorkbench().restart();
                }
            }
        });

        return true;
    }

    RepositoryDefinition getDefinition() {
        RepositoryDefinition repository = new RepositoryDefinition();

        repository.setDatabaseDriver(databaseSelectionPage.getDriver());
        repository.setDialect(databaseSelectionPage.getDialect());

        repository.setSchema(repositoryDetailsPage.getSchema());
        repository.setLabel(repositoryDetailsPage.getLabel());
        repository.setUrl(repositoryDetailsPage.getUrl());
        repository.setUser(repositoryDetailsPage.getUserName());
        repository.setPassword(repositoryDetailsPage.getPassword());

        return repository;
    }
}
