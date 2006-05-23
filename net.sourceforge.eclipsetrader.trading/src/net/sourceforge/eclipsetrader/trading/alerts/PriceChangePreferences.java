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

public class PriceChangePreferences extends AlertPluginPreferencePage
{
    private Button last;
    private Button bid;
    private Button ask;
    private Spinner change;
    private Spinner reference;
    private Label currentPrice;
    private NumberFormat formatter = NumberFormat.getInstance();

    public PriceChangePreferences()
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
        label.setText("Change (%)");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        change = new Spinner(content, SWT.BORDER);
        change.setMinimum(0);
        change.setMaximum(99999);
        change.setDigits(2);
        
        label = new Label(content, SWT.NONE);
        label.setText("Reference Price");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        reference = new Spinner(group, SWT.BORDER);
        reference.setMinimum(0);
        reference.setMaximum(99999999);
        reference.setDigits(4);
        currentPrice = new Label(group, SWT.NONE);

        int field = TargetPrice.LAST;
        String value = (String)getParameters().get("field");
        if (value != null)
            field = Integer.parseInt(value);
        last.setSelection(field == TargetPrice.LAST);
        bid.setSelection(field == TargetPrice.BID);
        ask.setSelection(field == TargetPrice.ASK);
        
        value = (String)getParameters().get("change");
        if (value != null)
            change.setSelection((int)Math.round(Double.parseDouble(value) * Math.pow(10, change.getDigits())));
        value = (String)getParameters().get("reference");
        if (value != null)
            reference.setSelection((int)Math.round(Double.parseDouble(value) * Math.pow(10, reference.getDigits())));
        
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
        parameters.put("change", String.valueOf(change.getSelection() / Math.pow(10, change.getDigits())));
        parameters.put("reference", String.valueOf(reference.getSelection() / Math.pow(10, reference.getDigits())));
        
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
