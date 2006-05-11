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

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CommissionsPage extends CommonPreferencePage
{
    private Account account;
    private Text fixedCommission;
    private Text variableCommission;
    private NumberFormat nf = NumberFormat.getInstance();

    public CommissionsPage()
    {
        setTitle("Commissions");
        setDescription("Set the commissions required for the transactions");
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
    }

    public CommissionsPage(Account account)
    {
        this.account = account;
        setTitle("Commissions");
        setDescription("Set the commissions required for the transactions");
        setPageComplete(true);
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Fixed");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        fixedCommission = new Text(content, SWT.BORDER);
        fixedCommission.setLayoutData(new GridData(80, SWT.DEFAULT));
        fixedCommission.setText(nf.format(account != null ? account.getFixedCommissions() : 0));

        label = new Label(content, SWT.NONE);
        label.setText("Variable (%)");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        variableCommission = new Text(content, SWT.BORDER);
        variableCommission.setLayoutData(new GridData(80, SWT.DEFAULT));
        variableCommission.setText(nf.format(account != null ? account.getVariableCommissions() : 0));

        fixedCommission.setFocus();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
        if (account != null)
        {
            account.setVariableCommissions(getVariableCommission());
            account.setFixedCommissions(getFixedCommission());
        }
    }
    
    public double getFixedCommission()
    {
        try {
            return nf.parse(fixedCommission.getText()).doubleValue();
        } catch(Exception e) {
            return 0;
        }
    }
    
    public void setFixedCommission(double value)
    {
        this.fixedCommission.setText(nf.format(value));
    }
    
    public double getVariableCommission()
    {
        try {
            return nf.parse(variableCommission.getText()).doubleValue();
        } catch(Exception e) {
            return 0;
        }
    }
    
    public void setVariableCommission(double value)
    {
        this.variableCommission.setText(nf.format(value));
    }
}
