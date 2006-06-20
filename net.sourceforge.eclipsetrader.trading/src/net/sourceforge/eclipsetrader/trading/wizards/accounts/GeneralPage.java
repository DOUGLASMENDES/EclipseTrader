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
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Account;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GeneralPage extends PreferencePage
{
    Account account;
    Text text;
    Combo currency;
    Text balance;
    Text currentBalance;
    NumberFormat nf = NumberFormat.getInstance();

    public GeneralPage()
    {
        setTitle("General");
        setDescription("Set the account general settings");
        noDefaultAndApplyButton();
        setValid(false);
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    public GeneralPage(Account account)
    {
        this.account = account;
        setTitle("General");
        noDefaultAndApplyButton();
        setValid(true);

        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
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
        label.setText("Name");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        text = new Text(content, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                setValid(text.getText().length() != 0);
            }
        });
        if (account != null)
            text.setText(account.getDescription());

        label = new Label(content, SWT.NONE);
        label.setText("Currency");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        currency = new Combo(content, SWT.SINGLE | SWT.READ_ONLY);
        currency.setVisibleItemCount(10);
        currency.add("");

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

        List list = CurrencyConverter.getInstance().getCurrencies();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((String)arg0).compareTo((String)arg1);
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            currency.add((String)iter.next());
        if (account != null)
        {
            if (account.getCurrency() != null)
                currency.setText(account.getCurrency().getCurrencyCode());
        }
        else
            currency.setText(Currency.getInstance(Locale.getDefault()).getCurrencyCode());

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (visible)
            text.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (account != null)
        {
            account.setDescription(getText());
            account.setCurrency(currency.getText().length() != 0 ? Currency.getInstance(currency.getText()) : null);
            account.setInitialBalance(getBalance());
        }
        return super.performOk();
    }
    
    public String getText()
    {
        return text.getText();
    }
    
    public void setText(String text)
    {
        this.text.setText(text);
    }
    
    public Currency getCurrency()
    {
        return currency.getText().length() != 0 ? Currency.getInstance(currency.getText()) : null;
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
}
