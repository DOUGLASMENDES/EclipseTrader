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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.repository.hibernate.internal.Activator;

public class DatabaseSelectionPage extends WizardPage {

    private TableViewer database;

    public DatabaseSelectionPage() {
        super("database", "Select a database type", AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/wizban/banner-repository.gif"));
        setDescription("You can connect to a repository using one of the installed databases.");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        setControl(content);

        initializeDialogUnits(parent);

        database = new TableViewer(content, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        database.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        database.getTable().setHeaderVisible(false);
        database.getTable().setLinesVisible(false);
        database.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((DatabaseElement) element).getLabel();
            }

            @Override
            public Image getImage(Object element) {
                return ((DatabaseElement) element).getIcon();
            }
        });
        database.setContentProvider(new ArrayContentProvider());
        database.setSorter(new ViewerSorter());
        database.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(!event.getSelection().isEmpty());
            }
        });

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.CONNECTIVITY_EXTENSION_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
            DatabaseElement[] elements = new DatabaseElement[configElements.length];
            for (int i = 0; i < configElements.length; i++) {
                elements[i] = new DatabaseElement(configElements[i]);
            }
            database.setInput(elements);
        }

        setPageComplete(false);
    }

    DatabaseElement getSelection() {
        IStructuredSelection selection = (IStructuredSelection) database.getSelection();
        DatabaseElement element = (DatabaseElement) selection.getFirstElement();
        return element;
    }

    public String getDriver() {
        IStructuredSelection selection = (IStructuredSelection) database.getSelection();
        DatabaseElement element = (DatabaseElement) selection.getFirstElement();
        return element.getDriver();
    }

    public String getDialect() {
        IStructuredSelection selection = (IStructuredSelection) database.getSelection();
        DatabaseElement element = (DatabaseElement) selection.getFirstElement();
        return element.getDialect();
    }
}
