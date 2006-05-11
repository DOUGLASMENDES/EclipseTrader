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
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GeneralPage extends CommonPreferencePage
{
    private Account account;
    private Text text;
    private Combo group;
    private Text balance;
    private Text currentBalance;
    private NumberFormat nf = NumberFormat.getInstance();

    public GeneralPage()
    {
        setTitle("General");
        setDescription("Set the account general settings");
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
    }

    public GeneralPage(Account account)
    {
        this.account = account;
        setTitle("General");
        setDescription("Set the account general settings");
        setPageComplete(true);

        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
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
        label.setText("Name");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        text = new Text(content, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        if (account != null)
            text.setText(account.getDescription());

        label = new Label(content, SWT.NONE);
        label.setText("Group");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        group = new Combo(content, SWT.DROP_DOWN);
        group.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        if (account != null && account.getGroup() != null)
            group.setText(account.getGroup().getDescription());

        label = new Label(content, SWT.NONE);
        label.setText("Initial Balance");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        balance = new Text(content, SWT.BORDER);
        balance.setLayoutData(new GridData(80, SWT.DEFAULT));
        balance.setText(nf.format(account != null ? account.getInitialBalance() : 0));

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, true, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setText("Current Balance");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        currentBalance = new Text(content, SWT.BORDER|SWT.READ_ONLY);
        currentBalance.setLayoutData(new GridData(80, SWT.DEFAULT));
        currentBalance.setText(nf.format(account != null ? account.getBalance() : 0));
        currentBalance.setEnabled(false);

        for (Iterator iter = CorePlugin.getRepository().allAccountGroups().iterator(); iter.hasNext(); )
        {
            AccountGroup ag = (AccountGroup)iter.next();
            group.add(ag.getDescription());
            group.setData(ag.getDescription(), ag);
        }

        text.setFocus();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
        if (account != null)
        {
            account.setDescription(getText());
            account.setInitialBalance(getBalance());
            AccountGroup group = getGroup();
            if (account.getGroup() != group)
            {
                if (account.getGroup() != null)
                {
                    account.getGroup().getAccounts().remove(account);
                    CorePlugin.getRepository().save(account.getGroup());
                }
                account.setGroup(group);
                if (group != null)
                {
                    group.getAccounts().add(account);
                    CorePlugin.getRepository().save(group);
                }
            }
        }
    }
    
    public String getText()
    {
        return text.getText();
    }
    
    public void setText(String text)
    {
        this.text.setText(text);
    }

    public double getBalance()
    {
        try {
            return nf.parse(balance.getText()).doubleValue();
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
        return 0;
    }

    public void setBalance(double balance)
    {
        this.balance.setText(nf.format(balance));
    }
    
    public AccountGroup getGroup()
    {
        AccountGroup ag = (AccountGroup)group.getData(group.getText());
        if (ag == null && group.getText().length() != 0)
        {
            ag = new AccountGroup();
            ag.setDescription(group.getText());
        }
        return ag;
    }
}
