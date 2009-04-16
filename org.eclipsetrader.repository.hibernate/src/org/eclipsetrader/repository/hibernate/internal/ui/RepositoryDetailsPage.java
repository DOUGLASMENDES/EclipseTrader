/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.repository.hibernate.internal.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.repository.hibernate.internal.Activator;

public class RepositoryDetailsPage extends WizardPage {
	private Text description;
	private Text schema;
	private Text url;
	private Text user;
	private Text password;

	private ModifyListener modifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
    		setPageComplete(isComplete());
        }
	};

	public RepositoryDetailsPage() {
		super("details", "Repository Settings", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/wizban/banner-repository-settings.gif"));
		setDescription("Set the database connection parameters.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		setControl(content);

		initializeDialogUnits(parent);

	    Label label = new Label(content, SWT.NONE);
	    label.setText("Label");
	    description = new Text(content, SWT.BORDER);
	    description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    description.addModifyListener(modifyListener);

	    label = new Label(content, SWT.NONE);
	    label.setText("Schema");
	    schema = new Text(content, SWT.BORDER);
	    schema.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));
	    schema.addModifyListener(modifyListener);

	    label = new Label(content, SWT.NONE);
	    label.setText("Connection URL");
	    url = new Text(content, SWT.BORDER);
	    url.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    ((GridData) url.getLayoutData()).widthHint = convertWidthInCharsToPixels(50);
	    url.addModifyListener(modifyListener);

	    label = new Label(content, SWT.NONE);
	    label.setText("User Name");
	    user = new Text(content, SWT.BORDER);
	    user.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));
	    user.addModifyListener(modifyListener);

	    label = new Label(content, SWT.NONE);
	    label.setText("Password");
	    password = new Text(content, SWT.BORDER | SWT.PASSWORD);
	    password.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));
	    password.addModifyListener(modifyListener);

	    description.setFocus();
		setPageComplete(false);
	}

	protected boolean isComplete() {
		if ("".equals(description.getText()))
			return false;
		if ("".equals(schema.getText()))
			return false;
		if ("".equals(url.getText()))
			return false;
		if (!"".equals(user.getText())) {
			if ("".equals(password.getText()))
				return false;
		}
		return true;
	}

	public String getLabel() {
		return description.getText();
	}

	public String getSchema() {
		return schema.getText();
	}

	public String getUrl() {
		return url.getText();
	}

	public String getUserName() {
		return user.getText();
	}

	public String getPassword() {
		return password.getText();
	}
}
