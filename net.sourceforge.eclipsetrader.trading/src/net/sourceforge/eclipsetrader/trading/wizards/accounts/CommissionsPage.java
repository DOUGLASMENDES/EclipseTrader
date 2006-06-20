/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.wizards.accounts;

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.trading.internal.SimpleAccount;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CommissionsPage extends PreferencePage
{
    Text fixedCommission;
    Text variableCommission;
    Text minimumCommission;
    Text maximumCommission;
    NumberFormat nf = NumberFormat.getInstance();

    public CommissionsPage()
    {
        setValid(true);
        noDefaultAndApplyButton();
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText("Fixed");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        fixedCommission = new Text(content, SWT.BORDER);
        fixedCommission.setLayoutData(new GridData(80, SWT.DEFAULT));
        fixedCommission.setText(nf.format(getPreferenceStore().getDouble(SimpleAccount.PREFS_FIXEDCOMMISSIONS)));

        label = new Label(content, SWT.NONE);
        label.setText("Variable (%)");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        variableCommission = new Text(content, SWT.BORDER);
        variableCommission.setLayoutData(new GridData(80, SWT.DEFAULT));
        variableCommission.setText(nf.format(getPreferenceStore().getDouble(SimpleAccount.PREFS_VARIABLECOMMISSIONS)));

        label = new Label(content, SWT.NONE);
        label.setText("Minimum");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        minimumCommission = new Text(content, SWT.BORDER);
        minimumCommission.setLayoutData(new GridData(80, SWT.DEFAULT));
        minimumCommission.setText(nf.format(getPreferenceStore().getDouble(SimpleAccount.PREFS_MINIMUMCOMMISSION)));

        label = new Label(content, SWT.NONE);
        label.setText("Maximum");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        maximumCommission = new Text(content, SWT.BORDER);
        maximumCommission.setLayoutData(new GridData(80, SWT.DEFAULT));
        maximumCommission.setText(nf.format(getPreferenceStore().getDouble(SimpleAccount.PREFS_MAXIMUMCOMMISSION)));

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (visible)
            fixedCommission.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = getPreferenceStore();
        
        try {
            store.setValue(SimpleAccount.PREFS_FIXEDCOMMISSIONS, nf.parse(fixedCommission.getText()).doubleValue());
        } catch(Exception e) {
        }
        try {
            store.setValue(SimpleAccount.PREFS_VARIABLECOMMISSIONS, nf.parse(variableCommission.getText()).doubleValue());
        } catch(Exception e) {
        }
        try {
            store.setValue(SimpleAccount.PREFS_MINIMUMCOMMISSION, nf.parse(minimumCommission.getText()).doubleValue());
        } catch(Exception e) {
        }
        try {
            store.setValue(SimpleAccount.PREFS_MAXIMUMCOMMISSION, nf.parse(maximumCommission.getText()).doubleValue());
        } catch(Exception e) {
        }

        return super.performOk();
    }
}
