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

package net.sourceforge.eclipsetrader.core.ui.dialogs;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class CurrencyConversionDialog extends Dialog
{
    private Spinner amount;
    private Combo from;
    private Combo to;
    private Text result;
    private NumberFormat nf = NumberFormat.getInstance();
    private String defaultFrom;
    private String defaultTo;

    public CurrencyConversionDialog(Shell parentShell)
    {
        this(parentShell, null, null);
    }

    public CurrencyConversionDialog(Shell parentShell, String defaultFrom, String defaultTo)
    {
        super(parentShell);
        this.defaultFrom = defaultFrom;
        this.defaultTo = defaultTo;

        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.CurrencyConversionDialog_Title);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        gridLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.CurrencyConversionDialog_Convert);
        label.setLayoutData(new GridData(60, SWT.DEFAULT));
        
        amount = new Spinner(content, SWT.BORDER);
        amount.setMinimum(1);
        amount.setMaximum(999999999);
        amount.setDigits(2);
        amount.setIncrement(100);
        amount.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                update();
            }
        });
        
        from = new Combo(content, SWT.READ_ONLY);
        from.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
        from.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                update();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

        label = new Label(content, SWT.NONE);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.CurrencyConversionDialog_To);
        label.setLayoutData(new GridData(60, SWT.DEFAULT));
        
        to = new Combo(content, SWT.READ_ONLY);
        to.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
        to.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                update();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.CurrencyConversionDialog_Equal);
        
        result = new Text(content, SWT.BORDER|SWT.READ_ONLY);
        result.setLayoutData(new GridData(80, SWT.DEFAULT));

        List list = CurrencyConverter.getInstance().getCurrencies();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((String)arg0).compareTo((String)arg1);
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            String symbol = (String)iter.next();
            from.add(symbol);
            to.add(symbol);
        }
        
        String locale = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        if (defaultFrom == null)
            defaultFrom = locale.equals("EUR") ? "USD" : "EUR"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (defaultTo == null)
            defaultTo = locale;
        
        from.setText(defaultFrom);
        to.setText(defaultTo);
        
        amount.setSelection(100);
        update();

        return content;
    }
    
    private void update()
    {
        try {
            double value = amount.getSelection() / Math.pow(10, amount.getDigits());
            result.setText(nf.format(CurrencyConverter.getInstance().convert(value, from.getText(), to.getText())));
        } catch(Exception e) {
            to.setText(""); //$NON-NLS-1$
        }
    }
}
