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

package net.sourceforge.eclipsetrader.core.ui.wizards;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class SecurityPage extends WizardPage
{
    Text code;
    Text securityDescription;
    Combo currency;
    Security security;

    public SecurityPage()
    {
        this(null);
    }

    public SecurityPage(Security security)
    {
        super(""); //$NON-NLS-1$
        setTitle(Messages.SecurityPage_Title);
        setDescription(Messages.SecurityPage_Description);
        setPageComplete(false);
        this.security = security;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        setControl(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.SecurityPage_CodeColumn);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        code = new Text(composite, SWT.BORDER);
        code.setLayoutData(new GridData(100, SWT.DEFAULT));
        if (security != null)
            code.setText(security.getCode());
        code.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                validatePage();
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.SecurityPage_DescriptionColumn);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        securityDescription = new Text(composite, SWT.BORDER);
        securityDescription.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        if (security != null)
            securityDescription.setText(security.getDescription());
        securityDescription.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                validatePage();
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.SecurityPage_CurrencyColumn);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        currency = new Combo(composite, SWT.SINGLE | SWT.READ_ONLY);
        currency.setVisibleItemCount(10);
        currency.add(""); //$NON-NLS-1$

        List list = CurrencyConverter.getInstance().getCurrencies();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((String)arg0).compareTo((String)arg1);
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            currency.add((String)iter.next());
        if (security != null)
        {
            if (security.getCurrency() != null)
                currency.setText(security.getCurrency().getCurrencyCode());
        }
        else
            currency.setText(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        
        validatePage();
    }
    
    private void validatePage()
    {
        if (code.getText().length() == 0)
            setPageComplete(false);
        else if (securityDescription.getText().length() == 0)
            setPageComplete(false);
        else
            setPageComplete(true);
    }

    public String getCode()
    {
        return code.getText();
    }

    public void setCode(String code)
    {
        this.code.setText(code);
        validatePage();
    }

    public Currency getCurrency()
    {
        if (currency.getText().length() != 0)
            return Currency.getInstance(currency.getText());
        return null;
    }

    public void setCurrency(Currency currency)
    {
        this.currency.setText(currency.getCurrencyCode());
        validatePage();
    }

    public String getSecurityDescription()
    {
        return securityDescription.getText();
    }

    public void setSecurityDescription(String securityDescription)
    {
        this.securityDescription.setText(securityDescription);
        validatePage();
    }
}
