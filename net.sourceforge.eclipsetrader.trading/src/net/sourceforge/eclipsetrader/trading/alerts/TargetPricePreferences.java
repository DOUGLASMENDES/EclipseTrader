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

package net.sourceforge.eclipsetrader.trading.alerts;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage;

public class TargetPricePreferences extends AlertPluginPreferencePage
{
    private Button last;
    private Button bid;
    private Button ask;
    private Spinner price;
    private Label currentPrice;
    private NumberFormat formatter = NumberFormat.getInstance();
    private Button cross;

    public TargetPricePreferences()
    {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(4);
        formatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Field");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));

        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        
        last = new Button(group, SWT.RADIO);
        last.setText("Last");
        last.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateCurrentPrice();
            }
        });
        bid = new Button(group, SWT.RADIO);
        bid.setText("Bid");
        bid.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateCurrentPrice();
            }
        });
        ask = new Button(group, SWT.RADIO);
        ask.setText("Ask");
        ask.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateCurrentPrice();
            }
        });
        
        label = new Label(content, SWT.NONE);
        label.setText("Price");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        price = new Spinner(group, SWT.BORDER);
        price.setMinimum(0);
        price.setMaximum(999999999);
        price.setDigits(4);
        price.setSelection(1000);
        currentPrice = new Label(group, SWT.NONE);

        label = new Label(content, SWT.NONE);
        cross = new Button(content, SWT.CHECK);
        cross.setText("Cross");

        int field = TargetPrice.LAST;
        String value = (String)getParameters().get("field");
        if (value != null)
            field = Integer.parseInt(value);
        last.setSelection(field == TargetPrice.LAST);
        bid.setSelection(field == TargetPrice.BID);
        ask.setSelection(field == TargetPrice.ASK);
        
        value = (String)getParameters().get("price");
        if (value != null)
            price.setSelection((int)Math.round(Double.parseDouble(value) * Math.pow(10, price.getDigits())));
        else if (getSecurity().getQuote() != null)
        {
            if (last.getSelection())
                price.setSelection((int)Math.round(getSecurity().getQuote().getLast() * Math.pow(10, price.getDigits())));
            else if (bid.getSelection())
                price.setSelection((int)Math.round(getSecurity().getQuote().getBid() * Math.pow(10, price.getDigits())));
            else if (ask.getSelection())
                price.setSelection((int)Math.round(getSecurity().getQuote().getAsk() * Math.pow(10, price.getDigits())));
        }

        value = (String)getParameters().get("cross");
        if (value != null)
            cross.setSelection(new Boolean(value).booleanValue());
        
        updateCurrentPrice();
        
        return content;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#performOk()
     */
    public void performOk()
    {
        Map parameters = new HashMap();

        if (last.getSelection())
            parameters.put("field", String.valueOf(TargetPrice.LAST));
        else if (bid.getSelection())
            parameters.put("field", String.valueOf(TargetPrice.BID));
        else if (ask.getSelection())
            parameters.put("field", String.valueOf(TargetPrice.ASK));
        parameters.put("price", String.valueOf(price.getSelection() / Math.pow(10, price.getDigits())));
        parameters.put("cross", String.valueOf(cross.getSelection()));
        
        setParameters(parameters);
    }
    
    private void updateCurrentPrice()
    {
        if (getSecurity().getQuote() != null)
        {
            if (last.getSelection())
                currentPrice.setText("(current: " + formatter.format(getSecurity().getQuote().getLast()) + ")");
            else if (bid.getSelection())
                currentPrice.setText("(current: " + formatter.format(getSecurity().getQuote().getBid()) + ")");
            else if (ask.getSelection())
                currentPrice.setText("(current: " + formatter.format(getSecurity().getQuote().getAsk()) + ")");
        }
        else
            currentPrice.setText("");
        currentPrice.getParent().layout();
    }
}
