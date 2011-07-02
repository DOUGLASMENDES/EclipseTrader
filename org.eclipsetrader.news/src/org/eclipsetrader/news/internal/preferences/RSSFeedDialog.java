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
import org.eclipsetrader.news.internal.connectors.FeedSource;

public class RSSFeedDialog extends Dialog {

    private Text name;
    private Text url;

    private FeedSource feedSource;

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
        name = new Text(content, SWT.BORDER);
        name.setLayoutData(new GridData(140, SWT.DEFAULT));
        name.setText(feedSource != null ? feedSource.getName() : "");

        label = new Label(content, SWT.NONE);
        label.setText(Messages.RSSFeedDialog_URL);
        label.setLayoutData(new GridData(75, SWT.DEFAULT));
        url = new Text(content, SWT.BORDER);
        url.setLayoutData(new GridData(280, SWT.DEFAULT));
        url.setText(feedSource != null ? feedSource.getUrl() : "");
        url.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getButton(IDialogConstants.OK_ID).setEnabled(url.getText().length() != 0);
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
        getButton(IDialogConstants.OK_ID).setEnabled(url.getText().length() != 0);
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        feedSource = new FeedSource(name.getText(), url.getText());
        super.okPressed();
    }

    public FeedSource getFeedSource() {
        return feedSource;
    }

    public void setFeedSource(FeedSource feedSource) {
        this.feedSource = feedSource;
    }
}
