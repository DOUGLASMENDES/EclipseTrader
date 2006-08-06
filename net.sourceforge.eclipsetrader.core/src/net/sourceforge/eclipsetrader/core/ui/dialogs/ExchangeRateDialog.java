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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

public class ExchangeRateDialog extends Dialog
{
    Text text;
    Combo from;
    Combo to;
    Spinner ratio;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    SimpleDateFormat dateParse = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$

    public ExchangeRateDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Exchange Rate");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(6, false);
        gridLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        gridLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText("Date");
        label.setLayoutData(new GridData(60, SWT.DEFAULT));

        text = new Text(content, SWT.BORDER);
        text.setSize(80, SWT.DEFAULT);
        GridData gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 5, 1);
        gridData.widthHint = 80;
        text.setLayoutData(gridData);
        text.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                try {
                    Date date = dateParse.parse(text.getText());
                    text.setText(dateFormat.format(date));
                } catch(Exception e1) {
                    text.setText(dateFormat.format(Calendar.getInstance().getTime()));
                }
                update();
            }
        });
        text.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.ARROW_UP)
                {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dateParse.parse(text.getText()));
                        calendar.add(Calendar.DATE, 1);
                        text.setText(dateFormat.format(calendar.getTime()));
                    } catch(Exception e1) {
                        text.setText(dateFormat.format(Calendar.getInstance().getTime()));
                    }
                    update();
                }
                else if (e.keyCode == SWT.ARROW_DOWN)
                {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dateParse.parse(text.getText()));
                        calendar.add(Calendar.DATE, -1);
                        text.setText(dateFormat.format(calendar.getTime()));
                    } catch(Exception e1) {
                        text.setText(dateFormat.format(Calendar.getInstance().getTime()));
                    }
                    update();
                }
            }
        });
        
        label = new Label(content, SWT.NONE);

        from = new Combo(content, SWT.READ_ONLY);
        from.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
        from.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                update();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("to");
        
        to = new Combo(content, SWT.READ_ONLY);
        to.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
        to.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                update();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("=");
        
        ratio = new Spinner(content, SWT.BORDER);
        ratio.setMinimum(1);
        ratio.setMaximum(999999999);
        ratio.setDigits(4);
        ratio.setIncrement(100);

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

        text.setText(dateFormat.format(Calendar.getInstance().getTime()));
        String locale = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        from.setText(locale.equals("EUR") ? "USD" : "EUR");
        to.setText(locale.equals("EUR") ? "EUR" : "USD");
        update();

        return content;
    }
    
    private void update()
    {
        try {
            Date date = dateParse.parse(text.getText());
            Double r = CurrencyConverter.getInstance().getExchangeRatio(date, from.getText(), to.getText());
            if (r == null)
            {
                r = CurrencyConverter.getInstance().getExchangeRatio(date, to.getText(), from.getText());
                if (r != null)
                    r = new Double(1 / r.doubleValue());
            }
            if (r == null)
                r = CurrencyConverter.getInstance().getExchangeRatio(from.getText(), to.getText());
            if (r != null)
                ratio.setSelection((int)(r.doubleValue() * Math.pow(10, ratio.getDigits())));
            else
                ratio.setSelection((int)(1 * Math.pow(10, ratio.getDigits())));
        } catch(Exception e) {
            ratio.setSelection((int)(1 * Math.pow(10, ratio.getDigits())));
            Logger.getLogger(getClass()).warn(e);
        }
        if (getButton(IDialogConstants.OK_ID) != null)
            getButton(IDialogConstants.OK_ID).setEnabled(!from.getText().equals(to.getText()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        try {
            Date date = dateParse.parse(text.getText());
            double r = (double)ratio.getSelection() / Math.pow(10, ratio.getDigits());
            CurrencyConverter.getInstance().setExchangeRatio(date, from.getText(), to.getText(), r);
        } catch(Exception e) {
            Logger.getLogger(getClass()).warn(e);
        }
        super.okPressed();
    }
}
