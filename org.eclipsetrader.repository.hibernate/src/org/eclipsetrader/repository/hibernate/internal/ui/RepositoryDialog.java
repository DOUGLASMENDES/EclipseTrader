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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.RepositoryDefinition;

public class RepositoryDialog extends TitleAreaDialog {

    private ComboViewer database;
    private Text description;
    private Text schema;
    private Text url;
    private Text user;
    private Text password;

    private RepositoryDefinition repository;

    public RepositoryDialog(Shell parentShell) {
        super(parentShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite content = (Composite) super.createDialogArea(parent);

        Label label = new Label(content, SWT.NONE);
        label.setText("Database");
        database = new ComboViewer(content, SWT.READ_ONLY | SWT.DROP_DOWN);
        database.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
        database.getCombo().setVisibleItemCount(15);

        label = new Label(content, SWT.NONE);
        label.setText("Label");
        description = new Text(content, SWT.BORDER);
        description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Schema");
        schema = new Text(content, SWT.BORDER);
        schema.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));

        label = new Label(content, SWT.NONE);
        label.setText("Connection URL");
        url = new Text(content, SWT.BORDER);
        url.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) url.getLayoutData()).widthHint = convertWidthInCharsToPixels(50);

        label = new Label(content, SWT.NONE);
        label.setText("User Name");
        user = new Text(content, SWT.BORDER);
        user.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));

        label = new Label(content, SWT.NONE);
        label.setText("Password");
        password = new Text(content, SWT.BORDER);
        password.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.CONNECTIVITY_EXTENSION_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
            DatabaseElement[] elements = new DatabaseElement[configElements.length];
            for (int i = 0; i < configElements.length; i++) {
                elements[i] = new DatabaseElement(configElements[i]);
            }
            database.setInput(elements);
        }

        if (repository != null) {
            DatabaseElement[] elements = (DatabaseElement[]) database.getInput();
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].getDriver().equals(repository.getDatabaseDriver()) && elements[i].getDialect().equals(repository.getDialect())) {
                    database.setSelection(new StructuredSelection(elements[i]));
                    break;
                }
            }
            schema.setText(repository.getSchema());
            description.setText(repository.getLabel());
            url.setText(repository.getUrl());
            user.setText(repository.getUser());
            password.setText(repository.getPassword());
        }

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) database.getSelection();
        DatabaseElement element = (DatabaseElement) selection.getFirstElement();
        repository.setDatabaseDriver(element.getDriver());
        repository.setDialect(element.getDialect());

        repository.setSchema(schema.getText());
        repository.setLabel(description.getText());
        repository.setUrl(url.getText());
        repository.setUser(user.getText());
        repository.setPassword(password.getText());

        super.okPressed();
    }

    public RepositoryDefinition getRepository() {
        return repository;
    }

    public void setRepository(RepositoryDefinition repository) {
        this.repository = repository;
    }
}
