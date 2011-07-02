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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.RepositoryDefinition;

public class RepositoryProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private ComboViewer database;
    private Text description;
    private Text schema;
    private Text url;
    private Text user;
    private Text password;

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            setValid(isValid());
        }
    };

    public RepositoryProperties() {
        setTitle("General");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        initializeDialogUnits(content);

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
        password = new Text(content, SWT.BORDER | SWT.PASSWORD);
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

        performDefaults();

        description.addModifyListener(modifyListener);
        schema.addModifyListener(modifyListener);
        url.addModifyListener(modifyListener);
        user.addModifyListener(modifyListener);
        password.addModifyListener(modifyListener);

        description.setFocus();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        if ("".equals(description.getText())) {
            return false;
        }
        if ("".equals(schema.getText())) {
            return false;
        }
        if ("".equals(url.getText())) {
            IStructuredSelection selection = (IStructuredSelection) database.getSelection();
            DatabaseElement element = (DatabaseElement) selection.getFirstElement();
            if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(element.getDriver()) && !"org.hsqldb.jdbcDriver".equals(element.getDriver())) {
                return false;
            }
        }
        if (!"".equals(user.getText())) {
            if ("".equals(password.getText())) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        RepositoryDefinition repository = (RepositoryDefinition) getElement().getAdapter(RepositoryDefinition.class);

        DatabaseElement[] elements = (DatabaseElement[]) database.getInput();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getDriver().equals(repository.getDatabaseDriver()) && elements[i].getDialect().equals(repository.getDialect())) {
                database.setSelection(new StructuredSelection(elements[i]));
                break;
            }
        }
        description.setText(repository.getLabel());
        schema.setText(repository.getSchema());
        url.setText(repository.getUrl() != null ? repository.getUrl() : "");
        user.setText(repository.getUser() != null ? repository.getUser() : "");
        password.setText(repository.getPassword() != null ? repository.getPassword() : "");

        super.performDefaults();
    }

    protected void applyChanges() {
        RepositoryDefinition repository = (RepositoryDefinition) getElement().getAdapter(RepositoryDefinition.class);

        IStructuredSelection selection = (IStructuredSelection) database.getSelection();
        DatabaseElement element = (DatabaseElement) selection.getFirstElement();
        repository.setDatabaseDriver(element.getDriver());
        repository.setDialect(element.getDialect());

        repository.setSchema(schema.getText());
        repository.setLabel(description.getText());
        repository.setUrl("".equals(url.getText()) ? null : url.getText());
        repository.setUser("".equals(user.getText()) ? null : user.getText());
        repository.setPassword("".equals(password.getText()) ? null : password.getText());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (getControl() != null) {
            applyChanges();
        }
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        applyChanges();

        Activator.saveRepositoryDefinitions();

        Shell shell = getShell();
        if (MessageDialog.openQuestion(shell, "EclipseTrader", "The workbench must be restarted for the changes to take effect.\r\nRestart the workbench now ?")) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    PlatformUI.getWorkbench().restart();
                }
            });
        }
    }
}
