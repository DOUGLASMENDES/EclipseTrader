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

package net.sourceforge.eclipsetrader.trading.wizards;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Watchlist;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GeneralPage extends CommonPreferencePage
{
    private Watchlist watchlist;
    private Text text;
    private Combo currency;

    public GeneralPage()
    {
        this(null);
    }

    public GeneralPage(Watchlist watchlist)
    {
        setTitle("General");
        setDescription("Set the name of the watchlist");
        this.watchlist = watchlist;
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

        label = new Label(content, SWT.NONE);
        label.setText("Currency");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        currency = new Combo(content, SWT.SINGLE | SWT.READ_ONLY);
        currency.setVisibleItemCount(10);
        currency.add("");

        List list = CurrencyConverter.getInstance().getCurrencies();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((String)arg0).compareTo((String)arg1);
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            currency.add((String)iter.next());
        if (watchlist != null)
        {
            if (watchlist.getCurrency() != null)
                currency.setText(watchlist.getCurrency().getCurrencyCode());
        }
        else
            currency.setText(Currency.getInstance(Locale.getDefault()).getCurrencyCode());

        text.setFocus();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
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
        if (currency.getText().length() != 0)
            return Currency.getInstance(currency.getText());
        return null;
    }
}
