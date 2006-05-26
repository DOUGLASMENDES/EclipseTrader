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

package net.sourceforge.eclipsetrader.trading.wizards.systems;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class BaseParametersPage extends CommonPreferencePage
{
    private Combo account;
    private Combo security;
    private Spinner maxExposure;
    private Spinner maxAmount;
    private Spinner minAmount;

    public BaseParametersPage()
    {
        setTitle("General");
        setDescription("Set the account, security and trading parameters");
        setPageComplete(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Account");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        account = new Combo(content, SWT.READ_ONLY);
        account.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        account.setVisibleItemCount(25);
        account.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (account.getSelectionIndex() != -1 && security.getSelectionIndex() != -1)
                    setPageComplete(true);
                else
                    setPageComplete(false);
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Security");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        security = new Combo(content, SWT.READ_ONLY);
        security.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        security.setVisibleItemCount(25);
        security.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (account.getSelectionIndex() != -1 && security.getSelectionIndex() != -1)
                    setPageComplete(true);
                else
                    setPageComplete(false);
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Max. Exposure");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        maxExposure = new Spinner(content, SWT.BORDER);
        maxExposure.setMinimum(0);
        maxExposure.setMaximum(999999999);
        maxExposure.setDigits(2);
        maxExposure.setIncrement(100);
        maxExposure.setSelection(0);
        maxExposure.setLayoutData(new GridData(60, SWT.DEFAULT));

        label = new Label(content, SWT.NONE);
        label.setText("Min. Trade Amount");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        minAmount = new Spinner(content, SWT.BORDER);
        minAmount.setMinimum(0);
        minAmount.setMaximum(999999999);
        minAmount.setDigits(2);
        minAmount.setIncrement(100);
        minAmount.setSelection(0);
        minAmount.setLayoutData(new GridData(60, SWT.DEFAULT));

        label = new Label(content, SWT.NONE);
        label.setText("Max. Trade Amount");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        maxAmount = new Spinner(content, SWT.BORDER);
        maxAmount.setMinimum(0);
        maxAmount.setMaximum(999999999);
        maxAmount.setDigits(2);
        maxAmount.setIncrement(100);
        maxAmount.setSelection(0);
        maxAmount.setLayoutData(new GridData(60, SWT.DEFAULT));
        
        List list = CorePlugin.getRepository().allAccounts();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Account)arg0).getDescription().compareTo(((Account)arg1).getDescription());
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Account s = (Account)iter.next();
            account.add(s.getDescription());
            account.setData(s.getDescription(), s);
        }
        
        list = CorePlugin.getRepository().allSecurities();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Security)arg0).getDescription().compareTo(((Security)arg1).getDescription());
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Security s = (Security)iter.next();
            security.add(s.getDescription());
            security.setData(s.getDescription(), s);
        }
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
    }

    public Account getAccount()
    {
        if (account == null || account.getSelectionIndex() == -1)
            return null;
        return (Account)account.getData(account.getItem(account.getSelectionIndex()));
    }

    public void setAccount(Account account)
    {
        this.account.setText(account.getDescription());
    }

    public Security getSecurity()
    {
        if (security == null || security.getSelectionIndex() == -1)
            return null;
        return (Security)security.getData(security.getItem(security.getSelectionIndex()));
    }

    public void setSecurity(Security security)
    {
        this.security.setText(security.getDescription());
    }

    public double getMaxExposure()
    {
        return maxExposure.getSelection() / Math.pow(10, maxExposure.getDigits());
    }

    public void setMaxExposure(double maxExposure)
    {
        this.maxExposure.setSelection((int)Math.round(maxExposure * Math.pow(10, this.maxExposure.getDigits())));
    }

    public double getMaxAmount()
    {
        return maxAmount.getSelection() / Math.pow(10, maxAmount.getDigits());
    }

    public void setMaxAmount(double maxAmount)
    {
        this.maxAmount.setSelection((int)Math.round(maxAmount * Math.pow(10, this.maxAmount.getDigits())));
    }

    public double getMinAmount()
    {
        return minAmount.getSelection() / Math.pow(10, minAmount.getDigits());
    }

    public void setMinAmount(double minAmount)
    {
        this.minAmount.setSelection((int)Math.round(minAmount * Math.pow(10, this.minAmount.getDigits())));
    }
}
