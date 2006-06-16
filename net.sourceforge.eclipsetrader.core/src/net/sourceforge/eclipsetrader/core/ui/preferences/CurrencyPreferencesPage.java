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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CurrencyPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Table table;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        table = new Table(content, SWT.FULL_SELECTION|SWT.SINGLE|SWT.CHECK);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = 250;
        table.setLayoutData(gridData);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.CurrencyPreferencesPage_Country);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.CurrencyPreferencesPage_Code);

        List available = new ArrayList(Arrays.asList(Locale.getAvailableLocales()));
        Collections.sort(available, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Locale) arg0).getDisplayCountry().compareTo(((Locale) arg1).getDisplayCountry());
            }
        });
        for (int i = available.size() - 1; i >= 1; i--)
        {
            if (((Locale) available.get(i)).getDisplayCountry().equals(((Locale) available.get(i - 1)).getDisplayCountry()) == true)
                available.remove(i);
        }

        List enabled = CurrencyConverter.getInstance().getCurrencies();
        for (int i = 0; i < available.size(); i++)
        {
            try
            {
                Locale locale = (Locale) available.get(i);
                Currency currency = Currency.getInstance(locale);
                if (currency != null)
                {
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(0, locale.getDisplayCountry());
                    tableItem.setText(1, currency.getCurrencyCode());
                    tableItem.setChecked(enabled.contains(currency.getCurrencyCode()));
                    tableItem.setData(currency);
                }
            }
            catch (Exception e) {
            }
        }
        
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        List enabled = new ArrayList();
        
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (items[i].getChecked())
            {
                String code = ((Currency)items[i].getData()).getCurrencyCode();
                if (!enabled.contains(code))
                    enabled.add(code);
            }
        }
        
        CurrencyConverter.getInstance().setCurrencies(enabled);

        Job job = new Job(Messages.CurrencyPreferencesPage_JobName) {
            protected IStatus run(IProgressMonitor monitor)
            {
                return CurrencyConverter.getInstance().updateExchanges(monitor);
            }
        };
        job.setUser(false);
        job.schedule();
        
        return super.performOk();
    }
}
