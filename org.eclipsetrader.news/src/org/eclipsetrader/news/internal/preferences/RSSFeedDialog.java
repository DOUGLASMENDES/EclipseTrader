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

package org.eclipsetrader.news.internal.preferences;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RSSFeedDialog extends Dialog {
	private Text sourceControl;
	private Text urlControl;
	private String source = "", url = ""; //$NON-NLS-1$ //$NON-NLS-2$

	public RSSFeedDialog(Shell shell) {
		super(shell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
    protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.RSSFeedDialog_Title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected Control createDialogArea(Composite parent) {
		Composite content = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label label = new Label(content, SWT.NONE);
		label.setText(Messages.RSSFeedDialog_Source);
		label.setLayoutData(new GridData(75, SWT.DEFAULT));
		sourceControl = new Text(content, SWT.BORDER);
		sourceControl.setLayoutData(new GridData(140, SWT.DEFAULT));
		sourceControl.setText(source);

		label = new Label(content, SWT.NONE);
		label.setText(Messages.RSSFeedDialog_URL);
		label.setLayoutData(new GridData(75, SWT.DEFAULT));
		urlControl = new Text(content, SWT.BORDER);
		urlControl.setLayoutData(new GridData(280, SWT.DEFAULT));
		urlControl.setText(url);
		urlControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getButton(IDialogConstants.OK_ID).setEnabled(urlControl.getText().length() != 0);
			}
		});

		return content;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(urlControl.getText().length() != 0);
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
    protected void okPressed() {
		source = sourceControl.getText();
		url = urlControl.getText();
		super.okPressed();
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
